//
//  Platform.m
//  Game Completionlist
//
//  Created by Kevin Schildhorn on 3/8/15.
//  Copyright (c) 2015 Kevin Schildhorn. All rights reserved.
//

#import "Platform.h"

@implementation Platform

#define PLATFORM_STEAM  1
#define PLATFORM_GOG    2

-(id) init
{
    self = [super init];
    if(self)
    {
        //do something
    }
    return self;
}

-(id) initWith:(int) incomingID type:(int)type login:(NSString *)log pass:(NSString *) pass{
    self = [super init];
    if(self)
    {
        // initialize
        platformId = incomingID;
        login = log;
        typeID = type;
        games = [[NSMutableArray alloc] init];
        // initialize based on platform type
        switch (typeID) {
            case PLATFORM_STEAM:
            default:
                name = @"Steam";
                APIkey = @"B6D54D6EBCF3A1A320644C485ACD1A6F";
                break;
                
            case PLATFORM_GOG:
                name = @"GoG";
                password = pass;
                break;
        }
    }
    return self;
}

-(id) initWith:(int)incomingID name:(NSString *)incomingName type:(int)incomingType login:(NSString *)incomingLogin pass:(NSString *)incomingPassword key:(NSString *)incomingAPIkey games:(NSMutableArray *)gameArray{
    
    self = [super init];
    if(self)
    {
        // initialize
        platformId = incomingID;
        name = incomingName;
        login = incomingLogin;
        password = incomingPassword;
        typeID = incomingType;
        APIkey = incomingAPIkey;
        
        games = gameArray;
    }
    return self;
}


-(void) addInformationFromServer:(HTTPRequestHandler*) requestHandler sql:(SQLiteHelper*) db{
    try {
        name = name + "_" + login;
        switch(typeID) {
            case R.integer.steam:
            default:
                login = requestHandler.requestSteamID(this);
                break;
            case R.integer.gog:
                break;
                
        }
        updateGamesList(requestHandler,db);
    } catch (JSONException e) {
        e.printStackTrace();
    } catch (ExecutionException e) {
        e.printStackTrace();
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
}

-(NSMutableArray *)updateGamesList:(HTTPRequestHandler*) requestHandler sql:(SQLiteHelper*) db{
    switch(typeID){
        case R.integer.steam:
        default:
            try {
                JSONObject gameListJSON = requestHandler.requestGameList(this);
                return parseAndAddGameList(gameListJSON,db);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            break;
            
        case R.integer.gog:
            //return getGoGGames(db);
            break;
            
    }
    
    return new NSMutableArray();
}
/*
 - NSMutableArray getGoGGames(SQLiteHelper db){
 GogApi api = new GogApi();
 api.login(login, password);
 List<GogGame> gogGames = api.listGames();
 
 NSMutableArray games = new NSMutableArray();
 DetailedGogGame gameDetails;
 Game tempGame;
 for(int i=0;i<gogGames.size();i++){
 gameDetails = api.loadDetails(gogGames.get(i));
 tempGame = new Game(gameDetails.getId(),gameDetails.getTitle(),gogGames.get(i).getCoverUrl());
 games.add(tempGame);
 db.addGame(tempGame);
 }
 db.addPlatform(this);
 return games;
 }
 */

-(void) updateGameAchievementAtIndex:(int) index request:(HTTPRequestHandler*) requestHandler sql:(SQLiteHelper*) db{
    requestHandler.requestGameAchievements(this,index);
    db.setGameAchievements(games[index]);
}
-(void) updateGameLogoAtIndex:(int) index request:(HTTPRequestHandler*) requestHandler sql:(SQLiteHelper*) db{
    requestHandler.requestGameLogo(this,index);
    db.setGameLogo(games[index]);
    if (games[index].getLogo() != null && !games[index].getLogo().isRecycled()) {
        games[index].getLogo().recycle();
        games[index].setLogo(null);
    }
}

-(void) updateGameControllerSupportAtIndex:(int) index request:(HTTPRequestHandler*) requestHandler sql:(SQLiteHelper*) db{
    requestHandler.requestGameControllerSupport(this,index);
    db.setGameControllerSupport(games[index]);
    if (games[index].getLogo() != null && !games[index].getLogo().isRecycled()) {
        games[index].getLogo().recycle();
        games[index].setLogo(null);
    }
}



// Getters

// Checks if name already exists in database and if so returns a custom one
- (NSMutableArray *) getGames  {return games;}
- (Game *) getGameAtIndex:(int) index{return [games objectAtIndex:index];}
- (NSString *) getName{return name;}
- (int) getID{return id;}
- (NSString *) getLogin{return  login;}
- (NSString *) getPassword {return login; }
- (int) getTypeID{return  typeID;}
- (NSString *)getAPIkey{return  APIkey;}

- (void) setName:(NSString *)incomingName {name = incomingName;}
// Modify Games

- (Platform *) sortPlatformGames:(int) sortType{
    SQLiteHelper db = SQLiteHelper.getInstance(cont);
    NSMutableArray gameList = db.getGames(id,sortType,1,true);
    games = gameList.toArray(new Game[gameList.size()]);
    return self;
}
- (Platform *) filterPlatformGames:(int) completionType{
    SQLiteHelper db = SQLiteHelper.getInstance(cont);
    NSMutableArray gameList = db.getGames(id,completionType,1,true);
    games = gameList.toArray(new Game[gameList.size()]);
    return self;
}


// Returns the new games
- (NSMutableArray *) parseAndAddGameList:(JSONObject) gameList sql:(SQLiteHelper*)db {
    int currentGameCount = games.length;
    int gameCount = gameList.getInt("game_count");
    
    if(currentGameCount != gameCount) {
        Game[] oldGames = games;
        NSMutableArray updatedGames = new NSMutableArray();
        
        games = new Game[gameCount];
        
        // Get game information
        JSONArray gameInfoArray = gameList.getJSONArray("games");
        JSONObject gameInfoTemp;
        
        Game gameTemp;
        int recent;
        
        // Pull data from JSON
        for (int i = 0; i < gameCount; i++) {
            gameInfoTemp = gameInfoArray.getJSONObject(i);
            recent = -1;
            
            try {
                if(gameInfoTemp.has("playtime_2weeks")) {
                    recent = gameInfoTemp.getInt("playtime_2weeks");
                }
            } catch (JSONException e) {
                //e.printStackTrace();
            }
            
            gameTemp = new Game(gameInfoTemp.getInt("appid"),               // ID
                                "",
                                gameInfoTemp.getString("name"),             // Name
                                id,                                    // platformID
                                gameInfoTemp.getString("img_logo_url"),     // LogoURL
                                null,                                       // Logo
                                gameInfoTemp.getInt("playtime_forever"),    // HoursPlayed
                                recent,                                     // lastTimePlayed
                                -1,                                         // AchievementsFinishedCount
                                -1,                                         // AchievementsTotalCount
                                0,                                          // CompletionStatus
                                -1,                                         // CustomSortIndex
                                0);                                         // ControllerSupport
            
            for (int j=0;j<oldGames.length;j++){
                if(gameTemp.getName().equals(oldGames[j].getName()) == false){
                    updatedGames.add(gameTemp);
                }
            }
            games[i] = gameTemp;
            db.addGame(gameTemp);
        }
        
        db.addPlatform(this);
        return updatedGames;
    }
    return new NSMutableArray();
}

@end

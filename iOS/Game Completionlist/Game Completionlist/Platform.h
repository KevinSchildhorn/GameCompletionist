//
//  Platform.h
//  Game Completionlist
//
//  Created by Kevin Schildhorn on 3/8/15.
//  Copyright (c) 2015 Kevin Schildhorn. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "Game.h"
#import "SQLiteHelper.h"
#import "HTTPRequestHandler.h"

@interface Platform : NSObject {
    int platformId;
    NSString * name;
    NSString * login;
    NSString * password;
    int typeID;
    NSString * APIkey;
    NSMutableArray *games;
}

-(id) initWith:(int) incomingID type:(int)type login:(NSString *)log pass:(NSString *) pass;
-(id) initWith:(int)incomingID name:(NSString *)name type:(int)type login:(NSString *)log pass:(NSString *) pass key:(NSString *)apiKey games:(NSMutableArray *)gameArray;
-(void) addInformationFromServer:(HTTPRequestHandler*) requestHandler sql:(SQLiteHelper*) db;
-(NSMutableArray *)updateGamesList:(HTTPRequestHandler*) requestHandler sql:(SQLiteHelper*) db;
-(void) updateGameAchievementAtIndex:(int) index request:(HTTPRequestHandler*) requestHandler sql:(SQLiteHelper*) db;
-(void) updateGameLogoAtIndex:(int) index request:(HTTPRequestHandler*) requestHandler sql:(SQLiteHelper*) db;
-(void) updateGameControllerSupportAtIndex:(int) index request:(HTTPRequestHandler*) requestHandler sql:(SQLiteHelper*) db;
- (NSMutableArray *) getGames;
- (Game *) getGameAtIndex:(int) index;
- (NSString *) getName;
- (int) getID;
- (NSString *) getLogin;
- (NSString *) getPassword;
- (int) getTypeID;
- (NSString *)getAPIkey;
- (void) setName:(NSString *)incomingName;
- (Platform *) sortPlatformGames:(int) sortType;
- (Platform *) filterPlatformGames:(int) completionType;


// Returns the new games
- (NSMutableArray *) parseAndAddGameList:(JSONObject) gameList sql:(SQLiteHelper*)db;

@end

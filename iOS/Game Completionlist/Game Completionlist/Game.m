//
//  Game.m
//  Game Completionlist
//
//  Created by Kevin Schildhorn on 3/8/15.
//  Copyright (c) 2015 Kevin Schildhorn. All rights reserved.
//

#import "Game.h"

@implementation Game 

-(id) init
{
    self = [super init];
    if(self)
    {
        //do something
    }
    return self;
}

-(id) initWith:(int)idNew idstring:(NSString *)idStringNew name:(NSString *)nameNew plaformID:(int)platformIDNew logoUrl:(NSString *)logoUrlNew logo:(NSData *)logoNew minPlayed:(int) minPlayed recMinPlayed:(int)recentMinPlayed achievFinCount:(int)achievFinCount achievTotalCount:(int)achievTotalCount complStatus:(int)complStatus sortIdx:(int) sortIdx contSupport:(int)contSupport{
    self = [super init];
    if(self){
        // initialize
        gameId = idNew;
        idString = idStringNew;
        name = nameNew;
        if([name hasPrefix:@"The "]){
            name = [NSString stringWithFormat:@"%@, The",[name substringFromIndex:4]];
        }
        platformID = platformIDNew;
        if(logoUrlNew == nil) {
            logoURL = @"";
        }
    
        if ([logoURL hasPrefix:@"http://"]) {
            logoURL = logoUrlNew;
        } else if (self->logoURL.length != 0) {
            logoURL = [NSString stringWithFormat:@"http://media.steampowered.com/steamcommunity/public/images/apps/%d/%@.jpg",gameId,logoURL];
        }
    
        if(logoNew != nil) {
            //logo = BitmapFactory.decodeByteArray(logo, 0, logo.length);
        }
        minutesPlayed = minPlayed;
        recentMinutesPlayed = recentMinPlayed;
        if(recentMinutesPlayed == -1) {
            recentMinutesPlayed = 0;
        }
        achievementsFinishedCount = achievFinCount;
        if(achievementsFinishedCount == -1) {
            achievementsFinishedCount = 0;
        }
        achievementsTotalCount = achievTotalCount;
        if(achievementsTotalCount == -1) {
            achievementsTotalCount = 0;
        }
        completionStatus = complStatus;
        customSortTypeIndex = sortIdx;
        controllerSupport = contSupport;
    }
    return self;
}

-(id) initWith:(NSString *) idNew title:(NSString *) title logoURL:(NSString *) logoURLNew{
    self = [super init];
    if(self){
        idString = idNew;
        name = title;
        logoURL = logoURLNew;
    }
    return self;
}
// Getters

- (int) getID{
    return gameId;
}
- (NSString *) getIdString{return idString;}
- (NSString *) getName{return name;}
- (int) getPlatformID{return platformID;}
- (NSString *) getLogoURL{return logoURL;}
- (UIImage *) getLogo{return logo;}
- (NSData *) getLogoInBytes{
    NSData *data;
    if(logo != nil) {
        //ByteArrayOutputStream stream = new ByteArrayOutputStream();
        //logo.compress(Bitmap.CompressFormat.PNG, 100, stream);
        //return stream.toByteArray();
    }
    else{
        //return new byte[0];
    }
    return data;
}
- (int) getMinutesPlayed{
    return minutesPlayed;
}
- (int) getRecentMinutesPlayed{
    return recentMinutesPlayed;
}
- (int) getAchievementsFinishedCount{
    return achievementsFinishedCount;
}
- (int) getAchievementsTotalCount{
    return achievementsTotalCount;
}
- (int) getControllerSupport{
    return controllerSupport;
}
/*
- (void) setAchievements:(JSONArray) achievements {
    achievementsFinishedCount = 0;
    achievementsTotalCount = 0;
    
    JSONObject *achievement;
    for(int i=0;i<achievements.length();i++){
        achievement = (JSONObject) achievements.get(i);
        if(achievement.getInt("achieved") == 1){
            achievementsFinishedCount++;
        }
        
        achievementsTotalCount++;
    }
    
    //Log.e("", achievementsFinishedCount + "/" + achievementsTotalCount + " Achievements");
}
 */
- (void) setLogo:(UIImage *) logoNew{
    logo = logoNew;
}
- (void) setControllerSupport:(int) contSupport{
    controllerSupport = contSupport;
}

- (int) getCompletionStatus{return completionStatus;}
- (int) getCustomSortTypeIndex{return customSortTypeIndex;}
- (void)setCompletionStatus:(int)status{completionStatus = status;}
- (void)setCustomSortTypeIndex:(int)index{customSortTypeIndex = index;}

@end

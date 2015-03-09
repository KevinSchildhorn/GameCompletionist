//
//  Game.h
//  Game Completionlist
//
//  Created by Kevin Schildhorn on 3/8/15.
//  Copyright (c) 2015 Kevin Schildhorn. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h> 

@interface Game : NSObject {
    @private
    int gameId;                     // DONE - requestGameInfo
     NSString *idString;
     NSString *name;                // DONE - requestGameInfo
     int platformID;                // DONE - parent
     NSString *logoURL;             // DONE - requestGameInfo
     UIImage *logo;
     int minutesPlayed;             // DONE - requestGameInfo
     int recentMinutesPlayed;       // DONE - requestGameInfo/requestRecentGames
     int achievementsFinishedCount; // DONE - requestAchievements
     int achievementsTotalCount;    // DONE - requestAchievements
     int controllerSupport;
    
    @public
    int completionStatus;
    int customSortTypeIndex;
}

-(id) initWith:(int)idNew idstring:(NSString *)idStringNew name:(NSString *)nameNew plaformID:(int)platformIDNew logoUrl:(NSString *)logoUrlNew logo:(NSData *)logoNew minPlayed:(int) minPlayed recMinPlayed:(int)recentMinPlayed achievFinCount:(int)achievFinCount achievTotalCount:(int)achievTotalCount complStatus:(int)complStatus sortIdx:(int) sortIdx contSupport:(int)contSupport;
-(id) initWith:(NSString *) idNew title:(NSString *) title logoURL:(NSString *) logoURLNew;
- (int) getID;
- (NSString *) getIdString;
- (NSString *) getName;
- (int) getPlatformID;
- (NSString *) getLogoURL;
- (UIImage *) getLogo;
- (NSData *) getLogoInBytes;
- (int) getMinutesPlayed;
- (int) getRecentMinutesPlayed;
- (int) getAchievementsFinishedCount;
- (int) getAchievementsTotalCount;
- (int) getControllerSupport;

- (int) getCompletionStatus;
- (int) getCustomSortTypeIndex;
- (void)setCompletionStatus:(int)status;
- (void)setCustomSortTypeIndex:(int)index;

//- (void) setAchievements:(JSONArray) achievements;
- (void) setLogo:(UIImage *) logoNew;
- (void) setControllerSupport:(int) contSupport;


@end

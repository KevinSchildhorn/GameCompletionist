//
//  SQLiteHelper.h
//  Game Completionlist
//
//  Created by Kevin Schildhorn on 3/8/15.
//  Copyright (c) 2015 Kevin Schildhorn. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <sqlite3.h>

@interface SQLiteHelper : NSObject{
    sqlite3 *_database;
}
+ (SQLiteHelper*)database;
- (NSArray *)platforms;

@end

//
//  SQLiteHelper.m
//  Game Completionlist
//
//  Created by Kevin Schildhorn on 3/8/15.
//  Copyright (c) 2015 Kevin Schildhorn. All rights reserved.
//

#import "SQLiteHelper.h"
#import "Platform.h"
#import "Game.h"

@implementation SQLiteHelper

static SQLiteHelper *_database;

+ (SQLiteHelper*)database {
    if (_database == nil) {
        _database = [[SQLiteHelper alloc] init];
    }
    return _database;
}
- (id)init {
    if ((self = [super init])) {
        NSString *sqLiteDb = [[NSBundle mainBundle] pathForResource:@"banklist"
                                                             ofType:@"sqlite3"];
        
        [self GenerateDatabase];
        if (sqlite3_open([sqLiteDb UTF8String], &_database) != SQLITE_OK) {
            NSLog(@"Failed to open database!");
        }
    }
    return self;
}   // DONE
-(void)GenerateDatabase{
    NSString *docsDir;
    NSArray *dirPaths;
    
    // Get the documents directory
    dirPaths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    docsDir = [dirPaths objectAtIndex:0];
    
    // Build the path to the database file
    NSString *databasePath = [[NSString alloc] initWithString: [docsDir stringByAppendingPathComponent: @"platforms.db"]];
    
    NSFileManager *filemgr = [NSFileManager defaultManager];
    
    if ([filemgr fileExistsAtPath: databasePath ] == NO) {
        const char *dbpath = [databasePath UTF8String];
        
        if (sqlite3_open(dbpath, &_database) == SQLITE_OK) {
            char *errMsg;
            const char *sql_stmt = "CREATE TABLE IF NOT EXISTS PLATFORMS (ID INTEGER PRIMARY KEY AUTOINCREMENT,\
            NAME TEXT,\
            LOGIN TEXT,\
            PASSWORD TEXT,\
            TYPE INTEGER,\
            KEY TEXT,)";
            
            if (sqlite3_exec(_database, sql_stmt, NULL, NULL, &errMsg) != SQLITE_OK) {
                //status.text = @"Failed to create table";
            }
            
            const char *sql_stmt2 = "CREATE TABLE IF NOT EXISTS GAMES (ID INTEGER PRIMARY KEY AUTOINCREMENT,\
            IDSTRING TEXT,\
            NAME TEXT,\
            PLATFORMID INTEGER,\
            LOGOURL TEXT,\
            LOGO BLOB,\
            MINPLAYED INTEGER,\
            RECMINPLAYED INTEGER\
            ACHIEVFIN INTEGER,\
            ACHIEVTOTAL INTEGER,\
            CONTSUPPORT INTEGER,\
            COMPLSTATUS INTEGER,\
            CUSTOMSORTIDX INTEGER)";
            
            if (sqlite3_exec(_database, sql_stmt2, NULL, NULL, &errMsg) != SQLITE_OK) {
                //status.text = @"Failed to create table";
            }
            
            sqlite3_close(_database);
        }
        else {
            //status.text = @"Failed to open/create database";
        }
    }
}   // DONE

// Platforms

- (void) addPlatform:(Platform *) platform {
    NSString *query = [NSString stringWithFormat:@"INSERT INTO PLATFORMS (id, \
    NAME, \
    LOGIN, \
    PASSWORD, \
    TYPE, \
    KEY) VALUES (%d,'%@','%@','%@',%d,'%@')",platform.getID,platform.getName,platform.getLogin,platform.getPassword,platform.getTypeID,platform.getAPIkey];
                       
    sqlite3_stmt *statement;
    if (sqlite3_prepare_v2(_database, [query UTF8String], -1, &statement, nil) == SQLITE_OK) {
        sqlite3_finalize(statement);
    }
}     // DONE
- (int) getPlatformCount{
    int count = 0;
    const char* sqlStatement = "SELECT COUNT(*) FROM PLATFORMS";
    sqlite3_stmt *statement;
        
    if( sqlite3_prepare_v2(_database, sqlStatement, -1, &statement, NULL) == SQLITE_OK ){
        //Loop through all the returned rows (should be just one)
        while( sqlite3_step(statement) == SQLITE_ROW ){
            count = sqlite3_column_int(statement, 0);
        }
    }
    else{
        NSLog( @"Failed from sqlite3_prepare_v2. Error is:  %s", sqlite3_errmsg(_database) );
    }
    // Finalize and close database.
    sqlite3_finalize(statement);
    
    return count;
}                        // DONE
- (NSMutableArray *) getPlatforms{
    NSMutableArray *retval = [[NSMutableArray alloc] init];
    NSString *query = @"SELECT id, \
    NAME, \
    LOGIN, \
    PASSWORD, \
    TYPE, \
    KEY, \
    FROM PLATFORMS";
    sqlite3_stmt *statement;
    if (sqlite3_prepare_v2(_database, [query UTF8String], -1, &statement, nil) == SQLITE_OK) {
        while (sqlite3_step(statement) == SQLITE_ROW) {
            int uniqueId = sqlite3_column_int(statement, 0);
            char *nameChars = (char *) sqlite3_column_text(statement, 1);
            char *loginChars = (char *) sqlite3_column_text(statement, 2);
            char *passwordChars = (char *) sqlite3_column_text(statement, 3);
            NSString *name = [[NSString alloc] initWithUTF8String:nameChars];
            NSString *login = [[NSString alloc] initWithUTF8String:loginChars];
            NSString *password = [[NSString alloc] initWithUTF8String:passwordChars];
            int type = sqlite3_column_int(statement, 4);
            char *keyChars = (char *) sqlite3_column_text(statement, 5);
            NSString *key = [[NSString alloc] initWithUTF8String:keyChars];
            Platform *info = [[Platform alloc] initWith:uniqueId name:name type:type login:login pass:password key:key games:nil];
            [retval addObject:info];
        }
        sqlite3_finalize(statement);
    }
    return retval;
}               // DONE
- (Platform *) getPlatform:(int) ID sort:(int)sortType filter:(int)filterType asc:(BOOL)sortAscending{
    Platform *info = [[Platform alloc] init];
    NSString *query =[NSString stringWithFormat:@"SELECT id, \
    NAME, \
    LOGIN, \
    PASSWORD, \
    TYPE, \
    KEY, \
    FROM PLATFORMS WHERE \
    ID=%d",ID];
    
    sqlite3_stmt *statement;
    if (sqlite3_prepare_v2(_database, [query UTF8String], -1, &statement, nil)
        == SQLITE_OK) {
        while (sqlite3_step(statement) == SQLITE_ROW) {
            int uniqueId = sqlite3_column_int(statement, 0);
            char *nameChars = (char *) sqlite3_column_text(statement, 1);
            char *loginChars = (char *) sqlite3_column_text(statement, 2);
            char *passwordChars = (char *) sqlite3_column_text(statement, 3);
            NSString *name = [[NSString alloc] initWithUTF8String:nameChars];
            NSString *login = [[NSString alloc] initWithUTF8String:loginChars];
            NSString *password = [[NSString alloc] initWithUTF8String:passwordChars];
            int type = sqlite3_column_int(statement, 4);
            char *keyChars = (char *) sqlite3_column_text(statement, 5);
            NSString *key = [[NSString alloc] initWithUTF8String:keyChars];
            info = [[Platform alloc] initWith:uniqueId name:name type:type login:login pass:password key:key games:nil];
        }
        sqlite3_finalize(statement);
    }
    return info;
}   // DONE
- (void) setPlatform:(Platform*) platform{
    NSString *query = [NSString stringWithFormat:@"UPDATE PLATFORMS SET id, \
    NAME=%@, \
    LOGIN=%@, \
    PASSWORD=%@, \
    TYPE=%d, \
    KEY WHERE id=%d", platform.getName, platform.getLogin, platform.getPassword, platform.getTypeID ,platform.getID];
    
    sqlite3_stmt *statement;
    if (sqlite3_prepare_v2(_database, [query UTF8String], -1, &statement, nil) == SQLITE_OK) {
        sqlite3_finalize(statement);
    }
}   // DONE


// Games

- (void) addGame:(Game*) game {
    NSString *query = [NSString stringWithFormat:@"INSERT INTO GAMES (id, \
                       IDSTRING,\
                       NAME,\
                       PLATFORMID,\
                       LOGOURL,\
                       LOGO,\
                       MINPLAYED,\
                       RECMINPLAYED,\
                       ACHIEVFIN,\
                       ACHIEVTOTAL,\
                       CONTSUPPORT,\
                       COMPLSTATUS,\
                       CUSTOMSORTIDX) \
                       VALUES (%d,'%@','%@',%d,'%@',??,%d,%d,%d,%d)",
                       game.getID, game.getIdString, game.getName, game.getPlatformID, game.getLogoURL, game.getLogoInBytes,
                       game.getMinutesPlayed,game.getRecentMinutesPlayed,game.getAchievementsFinishedCount,game.getAchievementsTotalCount,
                       game.getControllerSupport, game.getCompletionStatus,game.getCustomSortTypeIndex];
    
    sqlite3_stmt *statement;
    if (sqlite3_prepare_v2(_database, [query UTF8String], -1, &statement, nil) == SQLITE_OK) {
        sqlite3_finalize(statement);
    }
}                   // DONE
- (NSMutableArray *) getGames:(int) platformID filter:(int) filterType sort:(int) sortType asc:(BOOL) sortAscending{
    
    
    
    
    
    
    
    
    SQLiteDatabase db = this.getReadableDatabase();
    
    String[] columns = {
        KEY_ID,
        KEY_ID_STRING,
        KEY_NAME,
        KEY_PLATFORMID,
        KEY_LOGOURL,
        KEY_LOGO,
        KEY_MINUTESPLAYED,
        KEY_RECENTMINUTESPLAYED,
        KEY_ACHIEVEMENTSFINISHEDCOUNT,
        KEY_ACHIEVEMENTTOTALCOUNT,
        KEY_COMPLETIONSTATUS,
        KEY_CUSTOMSORTINDEX,
        KEY_CONTROLLERSUPPORT
    };
    
    String selection = "";
    if(platformID != -1) {
        selection = KEY_PLATFORMID + "=" + platformID;
    }
    if(filterType != -1) {
        if(selection != ""){
            selection = selection + " AND ";
        }
        selection = selection + KEY_COMPLETIONSTATUS + "=" + (filterType - 1);
    }
    String sortTypeText;
    switch (sortType){
        case 0:
        default:
            sortTypeText = KEY_NAME;
            break;
        case 1:
            sortTypeText = KEY_MINUTESPLAYED;
            break;
        case 2:
            sortTypeText = KEY_RECENTMINUTESPLAYED;
            break;
        case 3:
            sortTypeText = KEY_ACHIEVEMENTSFINISHEDCOUNT;
            break;
        case 4:
            sortTypeText = KEY_CUSTOMSORTINDEX;
            break;
    }
    if(sortAscending){
        sortTypeText +=" ASC";
    }
    else{
        sortTypeText +=" DESC";
    }
    
    
    Cursor curTemp = db.query(
                              TABLE_GAMES,  // The table to query
                              columns,                                 // The columns to return
                              selection,                               // The columns for the WHERE clause
                              null,                           // The values for the WHERE clause
                              null,                                    // don't group the rows
                              null,                                    // don't filter by row groups
                              sortTypeText                                // The sort order
                              );
    
    
    Game gameTemp;
    ArrayList gameArray = new ArrayList();
    curTemp.moveToFirst();
    for (int i = 0; i < curTemp.getCount(); i++) {
        
        
        gameTemp = new Game(curTemp.getInt(curTemp.getColumnIndexOrThrow(SQLiteHelper.KEY_ID)),
                            curTemp.getString(curTemp.getColumnIndexOrThrow(SQLiteHelper.KEY_ID_STRING)),
                            curTemp.getString(curTemp.getColumnIndexOrThrow(SQLiteHelper.KEY_NAME)),
                            curTemp.getInt(curTemp.getColumnIndexOrThrow(SQLiteHelper.KEY_PLATFORMID)),
                            curTemp.getString(curTemp.getColumnIndexOrThrow(SQLiteHelper.KEY_LOGOURL)),
                            curTemp.getBlob(curTemp.getColumnIndexOrThrow(SQLiteHelper.KEY_LOGO)),
                            curTemp.getInt(curTemp.getColumnIndexOrThrow(SQLiteHelper.KEY_MINUTESPLAYED)),
                            curTemp.getInt(curTemp.getColumnIndexOrThrow(SQLiteHelper.KEY_RECENTMINUTESPLAYED)),
                            curTemp.getInt(curTemp.getColumnIndexOrThrow(SQLiteHelper.KEY_ACHIEVEMENTSFINISHEDCOUNT)),
                            curTemp.getInt(curTemp.getColumnIndexOrThrow(SQLiteHelper.KEY_ACHIEVEMENTTOTALCOUNT)),
                            curTemp.getInt(curTemp.getColumnIndexOrThrow(SQLiteHelper.KEY_COMPLETIONSTATUS)),
                            curTemp.getInt(curTemp.getColumnIndexOrThrow(SQLiteHelper.KEY_CUSTOMSORTINDEX)),
                            curTemp.getInt(curTemp.getColumnIndexOrThrow(SQLiteHelper.KEY_CONTROLLERSUPPORT)));
        
        gameArray.add(gameTemp);
        
        curTemp.moveToNext();
    }
    return gameArray;
}
- (void) setGame:(Game*) game{
    NSString *query = [NSString stringWithFormat:@"UPDATE GAMES SET id, \
                       IDSTRING=%@,\
                       NAME=%@,\
                       PLATFORMID=%@,\
                       LOGOURL=%@,\
                       LOGO=??,\
                       MINPLAYED=%d,\
                       RECMINPLAYED=%d,\
                       ACHIEVFIN=%d,\
                       ACHIEVTOTAL=%d,\
                       CONTSUPPORT=%d,\
                       COMPLSTATUS=%d,\
                       CUSTOMSORTIDX=%d) \
                       KEY WHERE id=%d",
                       game.getID,game.getIdString,game.getName,game.getPlatformID,game.getLogoURL,game.getLogoInBytes,
                       game.getMinutesPlayed,game.getRecentMinutesPlayed,game.getAchievementsFinishedCount,game.getAchievementsTotalCount,
                       game.getControllerSupport,game.getCompletionStatus,game.getCustomSortTypeIndex,game.getID];
    
    sqlite3_stmt *statement;
    if (sqlite3_prepare_v2(_database, [query UTF8String], -1, &statement, nil) == SQLITE_OK) {
        sqlite3_finalize(statement);
    }
}                    // DONE
- (void) setGameAchievements:(Game*) game{
    SQLiteDatabase db = this.getReadableDatabase();
    
    // New value for one column
    ContentValues values = new ContentValues();
    values.put(KEY_ACHIEVEMENTSFINISHEDCOUNT, game.getAchievementsFinishedCount());
    values.put(KEY_ACHIEVEMENTTOTALCOUNT, game.getAchievementsTotalCount());
    
    
    // Which row to update, based on the ID
    String selection = KEY_NAME + "=" + '"' + game.getName() + '"' + " AND " + KEY_PLATFORMID + "=" + game.getPlatformID();
    
    int count = db.update(
                          TABLE_GAMES,
                          values,
                          selection,
                          null);
    
    if(count == 0){
        // Log.e("","ERROR SAVING ACHIEVEMENTS");
    }
}
- (void) setGameLogo:(Game*) game{
    SQLiteDatabase db = this.getReadableDatabase();
    
    // New value for one column
    ContentValues values = new ContentValues();
    values.put(KEY_LOGO, game.getLogoInBytes());
    
    // Which row to update, based on the ID
    String selection = KEY_NAME + "=" + '"' + game.getName() + '"' + " AND " + KEY_PLATFORMID + "=" + game.getPlatformID();
    
    //TODO
    int count = db.update(
                          TABLE_GAMES,
                          values,
                          selection,
                          null);
    
    if(count == 0){
        // Log.e("","ERROR SAVING LOGO");
    }
}
- (void) setGameControllerSupport:(Game*) game{
    SQLiteDatabase db = this.getReadableDatabase();
    
    // New value for one column
    ContentValues values = new ContentValues();
    values.put(KEY_CONTROLLERSUPPORT, game.getControllerSupport());
    
    // Which row to update, based on the ID
    String selection = KEY_NAME + "=" + '"' + game.getName() + '"' + " AND " + KEY_PLATFORMID + "=" + game.getPlatformID();
    
    int count = db.update(
                          TABLE_GAMES,
                          values,
                          selection,
                          null);
    
    if(count == 0){
        // Log.e("","ERROR SAVING ACHIEVEMENTS");
    }
}
- (void) setGameCustomSortTypeIndex:(Game*) game{
    SQLiteDatabase db = this.getReadableDatabase();
    
    // New value for one column
    ContentValues values = new ContentValues();
    values.put(KEY_CUSTOMSORTINDEX, game.customSortTypeIndex);
    
    // Which row to update, based on the ID
    String selection = KEY_NAME + "=" + '"' + game.getName() + '"' + " AND " + KEY_PLATFORMID + "=" + game.getPlatformID();
    
    //TODO
    int count = db.update(
                          TABLE_GAMES,
                          values,
                          selection,
                          null);
    
    if(count == 0){
        Log.e("","ERROR SAVING Custom Sort");
    }
}
- (int) getGameCount:(NSString *) platformName sort:(int)sortType{
    int count = 0;
    const char* sqlStatement = "SELECT COUNT(*) FROM GAMES";
    sqlite3_stmt *statement;
    
    if( sqlite3_prepare_v2(_database, sqlStatement, -1, &statement, NULL) == SQLITE_OK ){
        //Loop through all the returned rows (should be just one)
        while( sqlite3_step(statement) == SQLITE_ROW ){
            count = sqlite3_column_int(statement, 0);
        }
    }
    else{
        NSLog( @"Failed from sqlite3_prepare_v2. Error is:  %s", sqlite3_errmsg(_database) );
    }
    // Finalize and close database.
    sqlite3_finalize(statement);
    
    return count;
    
    
    
    
    SQLiteDatabase db;
    String countQuery;
    Cursor cursor;
    int id = 0;
    if(!platformName.equals("All")) {
        countQuery = "SELECT " + KEY_ID + " FROM " + TABLE_PLATFORM + " WHERE " + KEY_NAME + " = '" + platformName + "'";
        db = this.getReadableDatabase();
        cursor = db.rawQuery(countQuery, null);
        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            id = cursor.getInt(cursor.getColumnIndexOrThrow(SQLiteHelper.KEY_ID));
        }
    }
    
    countQuery = "SELECT  * FROM " + TABLE_GAMES;
    
    if(id != 0){
        countQuery += " WHERE " + KEY_PLATFORMID + " =" + id;
        if(sortType != 0){
            countQuery += " AND " + KEY_COMPLETIONSTATUS + " =" + sortType;
        }
    }
    else if(sortType != 0){
        countQuery += " WHERE " + KEY_COMPLETIONSTATUS + " =" + sortType;
    }
    db = this.getReadableDatabase();
    cursor = db.rawQuery(countQuery, null);
    int cnt = cursor.getCount();
    cursor.close();
    return cnt;
}   // DONE
- (NSData *) getGameLogo:(Game*) game{
    SQLiteDatabase db = this.getReadableDatabase();
    
    String[] columns = {
        KEY_LOGO
    };
    
    String selection = KEY_NAME + "=" + '"' + game.getName() + '"' + " AND " + KEY_PLATFORMID + "=" + '"' + game.getPlatformID() + '"';
    
    Cursor curTemp = db.query(
                              TABLE_GAMES,  // The table to query
                              columns,                                 // The columns to return
                              selection,                               // The columns for the WHERE clause
                              null,                           // The values for the WHERE clause
                              null,                                    // don't group the rows
                              null,                                    // don't filter by row groups
                              null                                // The sort order
                              );
    
    
    Game gameTemp;
    ArrayList gameArray = new ArrayList();
    curTemp.moveToFirst();
    byte[] logo = curTemp.getBlob(curTemp.getColumnIndexOrThrow(SQLiteHelper.KEY_LOGO));
    
    return logo;
}

- (void)dealloc {
    sqlite3_close(_database);
}

@end

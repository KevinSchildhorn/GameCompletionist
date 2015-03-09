//
//  PlatformViewController.h
//  Game Completionlist
//
//  Created by Kevin Schildhorn on 3/8/15.
//  Copyright (c) 2015 Kevin Schildhorn. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface PlatformViewController : UIViewController <UITableViewDataSource, UITableViewDelegate>

@property (nonatomic,retain) IBOutlet UITableView *tableView;

@property (nonatomic,retain) IBOutlet NSMutableArray *tableData;

@end

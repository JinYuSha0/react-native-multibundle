#import <React/RCTBridgeDelegate.h>
#import <UIKit/UIKit.h>
#import <MultiBundle/MultiBundle-Bridging-Header.h>

@interface AppDelegate : UIResponder <UIApplicationDelegate, RCTBridgeDelegate>

@property (nonatomic, strong) UIWindow *window;

@end

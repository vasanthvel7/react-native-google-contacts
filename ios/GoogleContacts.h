#import <React/RCTBridgeModule.h>
#import <AppAuth/AppAuth.h>
#import <GTMAppAuth/GTMAppAuth.h>

@interface GoogleContacts : NSObject <RCTBridgeModule>
@property(nonatomic, nullable)
    id<OIDExternalUserAgentSession> currentAuthorizationFlow;
@property(nonatomic, nullable) GTMAppAuthFetcherAuthorization *authorization;
@end

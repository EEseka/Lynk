import SwiftUI
import GoogleSignIn
import ComposeApp

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate

    init() {
        InitKoinKt.doInitKoin()
    }

   var body: some Scene {
      WindowGroup {
            ContentView().onOpenURL(perform: { url in
                // lynk.com.ng links only reach here once Associated Domains (applinks:lynk.com.ng) is on, which needs the paid Apple account
                if url.scheme == "lynk" || url.host == "lynk.com.ng" {
                    ExternalUriHandlerBridge.shared.onNewUri(uri: url.absoluteString)
                } else {
                    GIDSignIn.sharedInstance.handle(url)
                }
            })
      }
   }
}
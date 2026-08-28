import SwiftUI
import GoogleSignIn
import ComposeApp

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate

   var body: some Scene {
      WindowGroup {
            ContentView().onOpenURL(perform: { url in
                if url.scheme == "lynk" {
                    ExternalUriHandlerBridge.shared.onNewUri(uri: url.absoluteString)
                } else {
                    GIDSignIn.sharedInstance.handle(url)
                }
            })
      }
   }
}
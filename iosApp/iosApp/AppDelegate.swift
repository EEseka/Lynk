import Foundation
import UIKit
import GoogleSignIn
// Push notifications — uncomment together with the blocks below once the paid Apple
// Developer account exists. Add FirebaseCore + FirebaseMessaging as Swift Package
// dependencies on the iosApp target at the same time.
// import ComposeApp
// import UserNotifications
// import FirebaseCore
// import FirebaseMessaging

// Everything commented out below is the iOS half of push notifications. It is written
// and reviewed, but it cannot be switched on yet: iOS push needs the Push Notifications
// capability on the app target and an APNs auth key uploaded to Firebase, and both of
// those need the $99/year Apple Developer Program. Android is unaffected.
//
// When the account is ready, in order:
//   1. Set TEAM_ID in iosApp/Configuration/Config.xcconfig — this changes the bundle id.
//   2. Register the iOS app in Firebase project lynk-492118 under that final bundle id
//      and drop GoogleService-Info.plist into iosApp/iosApp/ (already gitignored).
//   3. Create an APNs auth key in the Apple Developer portal and upload the .p8 to
//      Firebase → Project settings → Cloud Messaging.
//   4. Add the Push Notifications capability to the iosApp target.
//   5. Uncomment the imports above and every block below.
//
// class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate, MessagingDelegate {
class AppDelegate: NSObject, UIApplicationDelegate {

    // func application(
    //   _ application: UIApplication,
    //   didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    // ) -> Bool {
    //   FirebaseApp.configure()
    //
    //   UNUserNotificationCenter.current().delegate = self
    //   Messaging.messaging().delegate = self
    //
    //   return true
    // }

    func application(
      _ app: UIApplication,
      open url: URL, options: [UIApplication.OpenURLOptionsKey : Any] = [:]
    ) -> Bool {
      var handled: Bool

      handled = GIDSignIn.sharedInstance.handle(url)
      if handled {
        return true
      }

      // Handle other custom URL types.

      // If not handled by this app, return false.
      return false
    }

    // APNs hands us the device token; Firebase swaps it for an FCM token.
    // func application(
    //   _ application: UIApplication,
    //   didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data
    // ) {
    //   Messaging.messaging().apnsToken = deviceToken
    //   refreshToken()
    // }
    //
    // func application(
    //   _ application: UIApplication,
    //   didFailToRegisterForRemoteNotificationsWithError error: Error
    // ) {
    //   print("iOS: Failed to register for push notifications: \(error.localizedDescription)")
    // }
    //
    // The FCM token arrives here, and again whenever Firebase rotates it. Storing it in
    // UserDefaults is what FirebasePushNotificationService.ios.kt reads on a cold start.
    // func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
    //   guard let token = fcmToken, !token.isEmpty else {
    //     refreshToken()
    //     return
    //   }
    //
    //   UserDefaults.standard.set(token, forKey: "FCM_TOKEN")
    //   IosDeviceTokenHolderBridge.shared.updateToken(token: token)
    // }
    //
    // func application(
    //   _ application: UIApplication,
    //   didReceiveRemoteNotification userInfo: [AnyHashable : Any],
    //   fetchCompletionHandler completionHandler: @escaping (UIBackgroundFetchResult) -> Void
    // ) {
    //   Messaging.messaging().appDidReceiveMessage(userInfo)
    //   completionHandler(.newData)
    // }
    //
    // Show the banner even while the app is open.
    // func userNotificationCenter(
    //   _ center: UNUserNotificationCenter,
    //   willPresent notification: UNNotification,
    //   withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    // ) {
    //   completionHandler([.banner])
    // }
    //
    // A tapped banner becomes a deep link, the same one MainActivity builds on Android.
    // ExternalUriHandler lives in :shared, which the ComposeApp framework does not export,
    // so Swift goes through the bridge object in composeApp/iosMain instead.
    // func userNotificationCenter(
    //   _ center: UNUserNotificationCenter,
    //   didReceive response: UNNotificationResponse,
    //   withCompletionHandler completionHandler: @escaping () -> Void
    // ) {
    //   let userInfo = response.notification.request.content.userInfo
    //
    //   if let hangoutId = userInfo["hangoutId"] as? String {
    //     switch userInfo["type"] as? String {
    //     case "PARTICIPANT_INVITED":
    //       ExternalUriHandlerBridge.shared.onNewUri(uri: "lynk://notifications/\(hangoutId)")
    //     // The user is out of this hangout by the time these arrive, so the detail screen
    //     // would only answer 403. Same rule the inbox row follows: open the app and go
    //     // nowhere in particular.
    //     case "INVITE_CANCELLED", "REMOVED_FOR_NON_PAYMENT":
    //       break
    //     default:
    //       ExternalUriHandlerBridge.shared.onNewUri(uri: "lynk://hangout_detail/\(hangoutId)")
    //     }
    //   }
    //
    //   completionHandler()
    // }
    //
    // func refreshToken() {
    //   Task {
    //     do {
    //       let fcmToken = try await Messaging.messaging().token()
    //
    //       UserDefaults.standard.set(fcmToken, forKey: "FCM_TOKEN")
    //       IosDeviceTokenHolderBridge.shared.updateToken(token: fcmToken)
    //     } catch {
    //       print("iOS: Error getting FCM token: \(error.localizedDescription)")
    //     }
    //   }
    // }
}

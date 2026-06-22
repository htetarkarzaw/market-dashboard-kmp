import SwiftUI
import SharedLogic

@main
struct iOSApp: App {
    init() {
        if ProcessInfo.processInfo.environment["XCODE_RUNNING_FOR_PREVIEWS"] != "1" {
            KoinHelperKt.doInitKoin()
        }
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
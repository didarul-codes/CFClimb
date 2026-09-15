//
//  iosAppApp.swift
//  iosApp
//
//  Created by Md Didarul Islam on 21/10/2025.
//

import SwiftUI
import WidgetKit
import ComposeApp

@main
struct iosAppApp: App {
    init() {
        // iOS requires background task handlers to be registered before launch finishes.
        BackgroundTasksKt.registerBackgroundTasks()
        // WidgetCenter has no Objective-C API, so Kotlin asks for widget reloads through Swift.
        HomeWidget_iosKt.setWidgetReloader {
            WidgetCenter.shared.reloadAllTimelines()
        }
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}

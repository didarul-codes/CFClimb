# CFClimb

**CFClimb – Codeforces Trainer** for Android and iOS. It follows the weekly cycle of a competitive
programmer: get reminded, compete, see the result, and look back at your progress.

Unofficial. Not affiliated with Codeforces. Data comes from the public
[Codeforces API](https://codeforces.com/apiHelp).

## Features

- **Contests:** upcoming, live and past rounds, with running rounds pinned on top, a countdown to
  the next one, pull-to-refresh and add-to-calendar.
- **Reminders:** notifications 1 day, 1 hour or 10 minutes before the rounds you pick, rescheduled
  when Codeforces moves a round.
- **Rating change alerts:** a background check for your saved handle that notifies you when a new
  rating is published.
- **Home-screen widget:** next round and your rating (Glance on Android, WidgetKit on iOS).
- **Profiles:** rating history, solved problems by difficulty and tag, verdicts, languages and an
  activity heatmap.
- **Compare:** two handles side by side.
- **Shared links:** Codeforces profile and contest links open in the app ("Share → CFClimb" on
  Android, `cfclimb://` links on both platforms).
- **Offline first:** everything you have opened is saved, so the app works without a connection.

## Tech

- Kotlin Multiplatform with Compose Multiplatform for the UI on both platforms
- Room (KMP) database as the source of truth, Ktor for the API with a request throttle that
  respects the Codeforces rate limit
- Koin, kotlinx-datetime, DataStore
- WorkManager and `BGTaskScheduler` for background checks, Glance and WidgetKit for widgets
- Tests: kotlin.test, Turbine, Ktor `MockEngine` contract tests, Robolectric and Roborazzi screenshot
  tests; GitHub Actions builds and tests Android and iOS

Modules: `composeApp` (UI and platform code), `shared` (data and domain), `iosApp` (Xcode project
and widget extension).

## Build and test

```bash
./gradlew :composeApp:installDebug
```

```bash
./gradlew :shared:testAndroidHostTest :composeApp:testDebugUnitTest :composeApp:verifyRoborazziDebug
```

```bash
./gradlew :shared:iosSimulatorArm64Test :composeApp:iosSimulatorArm64Test
```

Open `iosApp/iosApp.xcodeproj` in Xcode to run the iOS app.

## Roadmap

See [ROADMAP.md](ROADMAP.md) for what is done and what comes next.

## Screenshots

These are from an earlier version and will be replaced.

<p align="center">
  <img src="https://user-images.githubusercontent.com/27812028/162456852-72d8871e-f71f-4ed2-bfa9-d5cca7257aaf.png" width="220" height="400">
  <img src="https://user-images.githubusercontent.com/27812028/162458461-1965b54c-cf98-4a49-940e-b9ff01acdab2.png" width="220" height="400">
  <img src="https://user-images.githubusercontent.com/27812028/162458749-d94fe820-95e4-4382-b584-cf1087d17f4b.png" width="220" height="400">
  <img src="https://user-images.githubusercontent.com/27812028/162458771-deae3350-a413-49b1-bc84-12afce9da048.png" width="220" height="400">
  <img src="https://user-images.githubusercontent.com/27812028/162458778-4644542d-66cd-4804-b84b-c9f52d2649aa.png" width="220" height="400">
</p>

## Download

[Google Play](https://play.google.com/store/apps/details?id=com.codeforcesvisualizer)

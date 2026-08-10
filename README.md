# Tasks

A modern todo app for Android and iOS, built with Kotlin Multiplatform and a single Compose
Multiplatform UI shared by both platforms.

| Setting | Value |
| --- | --- |
| **App name** | Tasks |
| **Package / bundle id** | `com.toufikforyou.mytodoapp` |
| **Min Android** | API 28 (Android 9) |
| **Shared UI** | Compose Multiplatform 1.11 |
| **Storage** | Room (KMP) on bundled SQLite, on-device only |

## Features

- **Tasks** — create, edit, complete and delete, with notes, swipe-to-delete and undo.
- **Due dates and priorities** — date and time pickers, four priority levels, overdue highlighting.
- **Lists** — user-created categories with colours, used for both filing and filtering.
- **Search, filter and sort** — live search, status filters (All / Active / Today / Upcoming /
  Overdue / Completed), and sorting by due date, priority, recency or name.
- **Reminders** — local notifications scheduled ahead of a task's due time, with an Android
  notification action to mark a task done without opening the app.
- **Material 3 Expressive** — light and dark themes, dynamic colour on Android 12+, spring
  animations throughout.
- **Offline by design** — everything is stored locally in SQLite. Nothing leaves the device.

## Architecture

The shared module holds essentially the whole app; the two platform modules are thin entry points.

```
shared/src/commonMain/          Shared across Android and iOS
  domain/                       Models plus pure search/filter/sort/group logic (TodoQuery)
  data/local/                   Room entities, DAOs and the database definition
  data/repository/              Repository implementations, entity <-> domain mapping
  notification/                 ReminderScheduler contract
  ui/                           Compose UI: theme, components, screens, view models
  AppContainer.kt               Hand-rolled dependency graph
  App.kt                        Root composable and navigation host

shared/src/androidMain/         Room builder (needs Context), AlarmManager reminders,
                                broadcast receivers, dynamic colour
shared/src/iosMain/             Room builder (Documents directory),
                                UNUserNotificationCenter reminders

androidApp/                     Application, MainActivity, manifest, permission prompt
iosApp/                         SwiftUI entry point hosting the Compose view controller
```

A few decisions worth knowing about:

- **Filtering happens in Kotlin, not SQL.** `TodoQuery` is a pure function over the full task list,
  which keeps the whole pipeline unit-testable without a database and makes live search cheap. The
  trade-off is that every task is held in memory — fine for a personal list, not for tens of
  thousands of rows.
- **Platform pieces are injected, not `expect`/`actual`.** Each entry point builds an
  `AppContainer` with its own Room builder and `ReminderScheduler`. `expect`/`actual` is reserved
  for the few things that genuinely need it (dynamic colour, the IO dispatcher, Room's generated
  database constructor).
- **Icons are defined in code.** Compose Multiplatform has no maintained Material icon artifact, so
  `ui/icons/AppIcons.kt` builds the set the app needs from path data.
- **Reminders survive the app.** Android schedules through `AlarmManager` and posts from a
  broadcast receiver, and re-arms pending alarms after a reboot; iOS hands the request to
  `UNUserNotificationCenter`.

## Running the apps

**Android**

```sh
./gradlew :androidApp:assembleDebug
adb install -r androidApp/build/outputs/apk/debug/androidApp-debug.apk
adb shell am start -n com.toufikforyou.mytodoapp/.MainActivity
```

Or just use the run widget in Android Studio.

**iOS**

Open [/iosApp](./iosApp) in Xcode and run — the Kotlin framework is compiled as a build phase. From
the command line, for a simulator:

```sh
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug \
  -destination 'platform=iOS Simulator,name=iPhone 17' build
```

To run on a **physical iPhone** you must first set your Apple Developer team id in
[iosApp/Configuration/Config.xcconfig](./iosApp/Configuration/Config.xcconfig):

```
TEAM_ID=YOURTEAMID
```

It is currently empty, which is fine for the simulator but will fail code signing on a real device.
Note that `TEAM_ID` is appended to the bundle identifier, so the installed bundle id becomes
`com.toufikforyou.mytodoappYOURTEAMID`.

## Running tests

```sh
./gradlew :shared:testAndroidHostTest      # shared logic on the JVM
./gradlew :shared:iosSimulatorArm64Test    # the same suite on iOS
```

19 tests covering search, filtering, sorting, section grouping, date formatting and the
overdue/reminder rules.

## Permissions

Android requests `POST_NOTIFICATIONS` the first time you switch a reminder on, and declares
`RECEIVE_BOOT_COMPLETED` so reminders survive a restart. Exact alarms are used when the system
allows them and inexact alarms otherwise, so the app never needs special-access permissions. iOS
asks for notification authorisation at the same point in the flow.

## Renaming

If you fork this and want your own identifiers, the package name appears in:

- every Kotlin file's `package` / `import` lines, and the directory layout under each source root
- `androidApp/build.gradle.kts` — `namespace` and `applicationId`
- `shared/build.gradle.kts` — `android.namespace`
- `androidApp/src/main/AndroidManifest.xml` — the three `<receiver>` class names
- `iosApp/Configuration/Config.xcconfig` — `PRODUCT_BUNDLE_IDENTIFIER`

The display name lives in `androidApp/src/main/res/values/strings.xml` (`app_name`) and in
`Config.xcconfig` / `Info.plist` (`PRODUCT_NAME`, `CFBundleDisplayName`) for iOS. The Gradle
`rootProject.name` in `settings.gradle.kts` is still `Test2TodoApp`; it is a build-level identifier
only and is not visible anywhere in either app.

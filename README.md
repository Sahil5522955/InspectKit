# InspectKit

InspectKit is a standalone Android debug inspection library: think LeakCanary, but for live app internals. Keep it as a separate library repository, add it to apps with `debugImplementation`, wire in a small overlay, and developers can inspect network calls, response payloads, app databases, and runtime signals from inside the running app.

The current repository contains:

- `inspectkit`: the reusable Android library module.
- `app`: a demo app that shows the inspector with sample data.

## Pitch

Android Studio App Inspection is powerful, but it lives outside the app and is tied to a desktop workflow. InspectKit brings the most useful inspection workflows directly into debug builds:

- Live network timeline with method, URL, status, duration, headers, request body, and response body.
- Beautiful payload viewer with one-tap copy to clipboard.
- Basic secret redaction for headers and JSON-like keys.
- Live database browser through registered SQLite or Room database sources.
- Copyable database results as CSV.
- Runtime panel for memory and future performance signals.
- Modern responsive UI with classic neutral surfaces, navy accents, clear status colors, wrapping controls, and copy-first payload blocks.

## Current API

### Option A: Consume As Local Maven Library

From this library repo:

```bash
./gradlew :inspectkit:publishReleasePublicationToMavenLocal
```

In your app's `settings.gradle.kts` or repository config:

```kotlin
repositories {
    mavenLocal()
    google()
    mavenCentral()
}
```

In your app module:

```kotlin
dependencies {
    debugImplementation("dev.inspectkit:inspectkit:0.1.2")
    // Optional: LeakCanary-style separate launcher icon for InspectKit panel
    debugImplementation("dev.inspectkit:inspectkit-debug:0.1.2")
}
```

### Option B: Consume As Composite Build

Keep this repo outside your app repo, then in your app's `settings.gradle.kts`:

```kotlin
includeBuild("../InspectKit") {
    dependencySubstitution {
        substitute(module("dev.inspectkit:inspectkit")).using(project(":inspectkit"))
    }
}
```

Then depend on it the same way:

```kotlin
debugImplementation("dev.inspectkit:inspectkit:0.1.2")
```

### Option C: Temporary Direct Module Include

This is useful for quick development, but less clean long-term:

```kotlin
include(":inspectkit")
project(":inspectkit").projectDir = file("../InspectKit/inspectkit")
```

```kotlin
debugImplementation(project(":inspectkit"))
```

Install once from your debug `Application`:

```kotlin
InspectKit.install()
```

Add the panel to a debug-only screen, drawer, shake menu, or floating overlay:

```kotlin
InspectKitPanel()
```

Capture OkHttp traffic:

```kotlin
val client = OkHttpClient.Builder()
    .addInterceptor(OkHttpInspectInterceptor())
    .build()
```

Register a SQLite database:

```kotlin
InspectKit.registerDatabase(database.openHelper.writableDatabase.asInspectKitSource("AppDatabase"))
```

## Advanced Product Roadmap

These are the high-impact features that would make this genuinely useful for native Android teams:

- Network inspector: cURL export, HAR export, GraphQL operation grouping, image preview, JSON tree view, replay request, pin important calls, latency histogram.
- Database inspector: editable rows in debug builds, SQL console, Room entity labels, query history, table diff after actions, export query results.
- UI inspector: view hierarchy tree, Compose semantics tree, selected-node bounds overlay, typography/color extraction, accessibility warnings.
- Storage inspector: SharedPreferences/DataStore viewer, file explorer for app-private storage, encrypted preference redaction.
- Runtime inspector: memory graph, thread list, slow frame timeline, startup milestones, lifecycle event stream.
- Crash and log inspector: structured logs, breadcrumbs, last network calls before crash, shareable debug report bundle.
- Plugin architecture: app teams can register custom panels like feature flags, auth state, experiments, cache state, and push token status.
- Security model: debug-only artifact, no-op release artifact, configurable redaction, screenshot blocking for sensitive panels.

## Why This Beats A Separate Utility App

A normal Android helper app cannot see another app's private database, in-memory state, network objects, Room instances, or Compose tree without invasive platform permissions or root-level tricks. A debug library runs inside the target app process, which means it can inspect the real objects developers care about.

That is the key product bet: **install it like LeakCanary, inspect the app like Android Studio, share findings like a polished internal tool.**

## Run It

1. Open this folder in Android Studio.
2. Let Gradle sync.
3. Run the `app` configuration.

This shell currently does not expose Java, Gradle, Git, or the Android SDK, so the project was scaffolded and statically checked here but not compiled locally.

## License

InspectKit is available under the Apache License 2.0. See [LICENSE](LICENSE).

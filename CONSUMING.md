# Using InspectKit In Your App

Keep InspectKit in its own repository. Your app should consume it as a debug-only dependency.

## Best Development Setup

Use a Gradle composite build while actively editing InspectKit:

```kotlin
// settings.gradle.kts in your app repo
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

includeBuild("../InspectKit") {
    dependencySubstitution {
        substitute(module("dev.inspectkit:inspectkit")).using(project(":inspectkit"))
    }
}
```

```kotlin
// app/build.gradle.kts
dependencies {
    debugImplementation("dev.inspectkit:inspectkit:0.1.6")
}
```

## App Wiring

```kotlin
if (BuildConfig.DEBUG) {
    InspectKit.install()
}
```

```kotlin
val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(OkHttpInspectInterceptor())
    .build()
```

```kotlin
InspectKit.registerDatabase(
    appDatabase.openHelper.writableDatabase.asInspectKitSource("AppDatabase")
)
```

```kotlin
if (BuildConfig.DEBUG) {
    InspectKitPanel()
}
```

## Important Release Rule

Do not reference InspectKit from release-only code unless you also provide a no-op release artifact with the same API. The clean production setup is:

```kotlin
debugImplementation("dev.inspectkit:inspectkit:0.1.6")
releaseImplementation("dev.inspectkit:inspectkit-noop:0.1.6")
```

The no-op artifact is a future module to add once the public API stabilizes.

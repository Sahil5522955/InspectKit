# InspectKit Library

InspectKit is a standalone Android library module. Keep this module in its own repository and consume it from apps as a debug-only dependency.

## Recommended Usage

Publish locally while developing:

```bash
./gradlew :inspectkit:publishReleasePublicationToMavenLocal
```

Then in a consuming app:

```kotlin
repositories {
    mavenLocal()
    google()
    mavenCentral()
}

dependencies {
    debugImplementation("dev.inspectkit:inspectkit:0.1.3")
}
```

Use a no-op release artifact later if production code references InspectKit symbols directly.

# Changelog

## 0.1.6 - 2026-05-19

- Fixed InspectKit launcher icon not appearing when consumed as a published AAR (launcher activity is now always enabled when the library is present).

## 0.1.7 - 2026-05-19

- Fixed launcher behavior: InspectKit launcher now uses an `activity-alias` with a separate task affinity, so tapping the InspectKit icon opens the InspectKit UI instead of resuming the host app task.

## 0.1.8 - 2026-05-19

- Fixed launcher alias manifest attributes (removed unsupported attributes from `<activity-alias>`) and set `InspectKitActivity` to `singleTask` to reduce cases where launchers resume the host app instead.

## 0.1.5 - 2026-05-19

- Version bump only (0.1.5).

## 0.1.3 - 2026-05-19

- Fixed launcher icon distribution: the InspectKit launcher activity now ships in the main artifact and is enabled only for debug builds.

## 0.1.2 - 2026-05-19

- Published `dev.inspectkit:inspectkit-debug` which includes the debug launcher activity, so the InspectKit icon appears in app launchers when used as a dependency. (Deprecated by 0.1.3)

## 0.1.1 - 2026-05-19

- Added debug-only InspectKit launcher activity (LeakCanary-style) for opening `InspectKitPanel`.
- Added "Copy cURL" export for captured network events.
- Added database result XML export via the system document picker.

## 0.1.0 - 2026-05-16

Initial public release of InspectKit.

- Added standalone Android library module published as `dev.inspectkit:inspectkit`.
- Added in-app Compose inspector panel with Network, Database, and Runtime tabs.
- Added OkHttp interceptor for request/response capture.
- Added copyable network payloads with basic secret redaction.
- Added SQLite/Room database source helper and CSV export.
- Added demo Android app and consumer integration guide.

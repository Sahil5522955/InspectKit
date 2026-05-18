# Changelog

## 0.1.2 - 2026-05-19

- Published `dev.inspectkit:inspectkit-debug` which includes the debug launcher activity, so the InspectKit icon appears in app launchers when used as a dependency.

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

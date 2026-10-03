# Observed testing and limitations

Testing occurred on one BYD Android 11 / API 30 infotainment unit on 3 October 2026. The exact vehicle model and full firmware build were not recorded for this release; do not infer fleet-wide compatibility.

## Observed during setup

- Both generated APKs passed Android APK signature verification.
- The helper was initially installed through ADB.
- File access was granted through the normal on-screen permission dialog.
- Downloads browsing and the selected APK review dialog worked.
- The permission-free test app was installed through the helper/system installer, not through `adb install`.
- Android's installed-package record identified `com.gbox.android` as the test app's installer.

ADB was connected for setup, screen interaction and diagnostic readback during that test. The helper source contains no ADB or shell-install integration.

## Subsequently reported by the owner

- Successful installation of an already-downloaded VLC APK through the helper.
- After restarting the media centre, ADB was unavailable; the owner subsequently confirmed success of the suggested helper self-update route to version 1.0.1. This is an owner-reported result, not an independently captured post-restart package record.

Version 1.0.1 changes the Downloads wording to avoid implying that Firefox is required. APK sources are unrestricted; the built-in browser covers Downloads and its subfolders.

## Not established

Compatibility across other models, regions or firmware builds; installation after an OTA; optional file-manager Open-with integration; exhaustive malformed-file, lifecycle/concurrency and low-storage handling. Split bundles are unsupported. Metadata display is not a security scan.

Only the helper and generated test app were installed during the controlled diagnostic test. No vendor configuration, rooting or firmware changes were made. The owner later installed VLC themselves.

# BYD Local APK Installer

An unofficial, small Android app for installing local, single APK files on the BYD firmware tested by this project. Select a file in Downloads, review its name/version/package, then confirm in the car's system installer.

## Trust comes first

**Don't just install something because it sounds cute or useful — including this installer. Inspect it, understand what it does, and confirm that it is suitable for you and your vehicle.**

A familiar app name, an APK's displayed label, or a successful installation is not evidence that software is trustworthy. Review the source and permissions, check where your APK came from, and verify its signing identity. Build from source if you prefer. The displayed app details are not a malware check or a suitability assessment. A matching download hash confirms file integrity, not safety.

Use only on a vehicle you own or are authorised to manage, while parked. You are responsible for selecting and testing software appropriate to your system. There is no guarantee of compatibility, continued operation, or survival of a firmware update.

## What it does

- Lists APKs and subfolders within Downloads. Files may come from any source, including USB: copy them into Downloads first.
- Copies the selected APK into private cache, displays its metadata, and supplies a read-only content URI to the system installer.
- Requires a review step and the system's installation confirmation.
- Requests file-reading and installation-request permissions. It has no Internet permission, ADB client, privileged installation permission, or background service.

It supports single `.apk` files, not `.apks`, `.apkm`, `.xapk` or split bundles. It does not certify, scan or sandbox the apps you install. The file-manager Open-with integration is optional and has not been device-tested.

## Important: package identity and firmware dependency

The application ID is **`com.gbox.android`**. This project is **not GBox**, contains no GBox code, and is not affiliated with GBox, BYD, Huawei or Google. Its own signing certificate is different from genuine GBox.

On the inspected BYD Android 11 firmware, the system installer checks the requesting package name against an allowlist without checking its certificate at that gate. This helper uses an allowlisted name to reach normal installation confirmation. It does not change the vendor configuration or firmware.

The helper and genuine GBox cannot coexist under this package ID. Do not remove genuine GBox without understanding the consequences. Android rejects an update signed with a different certificate. Future BYD firmware can close this route by changing the allowlist, enforcing certificates, or changing installer/storage behaviour. **This is not an OTA-proof installation solution.**

## Install and use

Download `APK-Installer.apk` from [Releases](../../releases), review the source and release checksums, and install it while you still have a working installation route. With ADB already available:

```text
adb install -r APK-Installer.apk
```

1. Save or copy a single APK into Downloads.
2. Open APK Installer and allow file access when requested.
3. Select the APK; use Refresh after copying new files.
4. Review the displayed details, tap Continue, and confirm on the system installer screen.

Installing the helper initially requires an existing installation route. It cannot bootstrap itself after every route has been removed. Once installed, it has no ADB dependency.

The optional `Installation-Test.apk` has no requested permissions and simply displays a success message. See [TESTING.md](TESTING.md) for observed results and limits.

## Build and verify

See [BUILDING.md](BUILDING.md). The repository contains the original source, not decompiled BYD software. Private signing keys, vendor APKs, device logs and screenshots are excluded.

Release 1.0.1 signing certificate SHA-256:

```text
29756ee8128c2c6abc0fde130a6107c29d2223bcfe4b99738f41b7d2972d1b66
```

Keep your own signing key private if building a fork. A fork signed with another key cannot update an existing release installation in place. Independent builds are not promised to be byte-identical to the release APK.

## Licence

Original project source is provided under the [MIT License](LICENSE), including its warranty and liability disclaimer. The software is supplied as is. The licence is not a guarantee of legal immunity; applicable law may limit disclaimers. Android/Java build tools and third-party APKs have their own licences and are not relicensed by this project.

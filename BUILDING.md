# Building from source

Requirements: Python 3, JDK 17, Android SDK platform 30 and Build Tools 34.0.0. Install the tools from their official distributions and accept their applicable terms. No Gradle dependencies are used.

Set `JAVA_HOME` and `ANDROID_SDK_ROOT` to your local installations. From this repository:

```text
python build.py
```

Unsigned, aligned APKs are written to `dist/`. Sign them yourself before installation, using the SDK's `apksigner`. For example, create a private key (keytool prompts for a password):

```text
keytool -genkeypair -keystore my-private-installer.p12 -storetype PKCS12 -alias installer -keyalg RSA -keysize 2048 -validity 10000
apksigner sign --ks my-private-installer.p12 --ks-key-alias installer --out dist/APK-Installer.apk dist/APK-Installer-unsigned.apk
apksigner verify --verbose --print-certs dist/APK-Installer.apk
```

Invoke these executables from your JDK/SDK directories if they are not on PATH. Sign the test APK in the same way if desired. Keep passwords out of shell history and never commit a private key. Repository ignore rules are an additional precaution, not a replacement for checking uploads.

Own-key builds cannot update the published APK in place. Retain the same key for future updates to your own build, and increment the manifest versionCode when producing an update. Uninstalling an existing helper to replace its signing identity may leave you unable to reinstall it if ADB or another installation route is gone.

The app intentionally targets API 28 for legacy Downloads access on the tested Android 11 firmware. Raising the target SDK requires redesign and testing of storage access. The explicit system-installer component is also firmware-dependent.

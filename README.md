# Clumsy Android app

Clumsy is packaged for Android with Capacitor. The existing `index.html` remains
the app's web source; `npm run build` copies it to the generated `www/` folder
and bundles the Android file-sharing bridge.

## Requirements

- Node.js 22 or newer and npm
- Android Studio with the Android SDK and platform tools installed
- A JDK supported by the installed Android Gradle Plugin (Android Studio's
  bundled JDK is recommended)

## Create and open the Android project

```powershell
npm install
npm run android:add
npm run android:open
```

`android:add` creates the native `android/` project once. Commit that generated
folder so the Android project is available on other machines. For later web app
changes, run:

```powershell
npm run android:sync
```

Then build a debug APK either in Android Studio (**Build > Build Bundle(s) /
APK(s) > Build APK(s)**) or from PowerShell:

```powershell
npm run android:debug
```

The debug APK is written to
`android/app/build/outputs/apk/debug/app-debug.apk`. For distribution, create
a signed release APK in Android Studio (**Build > Generate Signed Bundle /
APK**).

## Data and backups

Financial records are encrypted before they are stored on-device or included
in JSON backups. On first launch, choose an encryption password of at least
10 characters. Existing plain local records are encrypted when you set that
password; legacy unencrypted JSON backups remain importable. New JSON backups
are encrypted, and Clumsy cannot recover forgotten passwords. CSV exports are
intended for spreadsheet use and are not encrypted.

Google Drive backups are encrypted before upload and update automatically when
you save app changes. If you are offline or authorization expires, the latest
encrypted copy remains on-device and Clumsy retries after reconnection. After
reinstalling, choose Google Drive during setup, authorize the app, and enter
the password used for the backup to recover it. The device-only copy is still
removed on uninstall.

### People and money shared with others

Add people under **More > People**, then choose them when recording money lent,
money borrowed, repayments, or investments. Each person shows their outstanding
balances and activity history. Investments are tracked separately from loans;
add a transaction note to record the reason or terms. Existing debt history is
kept and matched to people when the app is upgraded.

### Configure Google Drive on Android

1. Enable the Google Drive API in your Google Cloud project.
2. Create an **Android OAuth client** for application ID
   `com.clumsy.finance` and the SHA-1 signing-certificate fingerprint used to
   sign your APK. Android obtains Drive authorization through Google's native
   authorization API; no web client secret belongs in the app.
3. If you also use the browser version, create a separate **Web OAuth client**
   and enter its client ID in Settings.

For a locally built debug APK, obtain its fingerprint with
`cd android; .\gradlew.bat signingReport` in PowerShell. Google OAuth verifies
both the package name and signing certificate. A debug APK downloaded from
GitHub Actions may use a different debug certificate than your local build;
register the fingerprint of the APK you install, or use a consistently signed
build for ongoing Android Drive access.

## App identity

- Name: Clumsy
- Android application ID: `com.clumsy.finance`

# Clumsy Android app

Clumsy is packaged for Android with Capacitor. The existing `index.html` remains
the app's web source; `npm run build` copies it to the generated `www/` folder
and bundles the Android file-sharing bridge.

## Requirements

- Node.js 20 or newer and npm
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

Android stores local records in the app's private WebView storage. This storage
is separate from browser data and is removed when the app is uninstalled.
Export a Clumsy JSON backup from the browser app and restore it from the
Android app's setup screen to move existing records. In Android, backup export
opens the system share sheet so the file can be saved or sent somewhere safe.
CSV export uses the same share sheet.

Google Drive sign-in is hidden in the Android app because Google's web OAuth
sign-in is not supported inside embedded WebViews. Use local storage and
regular backup exports in the APK.

## App identity

- Name: Clumsy
- Android application ID: `com.clumsy.finance`

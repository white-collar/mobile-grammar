# mobile-grammar
This is open source of "Mobile grammar: English" https://play.google.com/store/apps/details?id=org.mobile.grammar

This program is available in Play Market right now.

## Building

Requires JDK 21 and the Android SDK (platform 36). Android Studio sets both up.
Unit tests (`./gradlew test`) run the screens with Robolectric, without a device.

Google Play expects an Android App Bundle (`.aab`), not an APK:

    ./gradlew bundleFreeRelease   # -> app/build/outputs/bundle/freeRelease/app-free-release.aab
    ./gradlew bundleProRelease    # -> app/build/outputs/bundle/proRelease/app-pro-release.aab

Both flavors share the application id `org.mobile.grammar`, so only one of them can be published to that Play listing.
The bundle must be signed with the app's upload key before uploading (Android Studio: *Build → Generate Signed Bundle / APK*).
Raise `versionCode` in `app/build.gradle` for every upload; Play rejects codes it has already seen.

For a debug build to install on a device: `./gradlew assembleFreeDebug`.

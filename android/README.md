# abl_link~ for Android

While this solution is about using the abl_link~ external with Pd for Android, it should contain enough information to allow developers to integrate Link into Android apps that don't use Pd.

## How to build abl_link~ for Android

* Build and run the AblLinkSample app, just to make sure that your development setup is sound.
* Add `<uses-permission android:name="android.permission.INTERNET" />` to your manifest. Without it, there won't be any obvious failures, but abl_link~ will not connect to other Link instances.
* Link gradle to `CMakeLists.txt` in this directory following these instructions: https://developer.android.com/studio/projects/add-native-code.html#link-gradle
* Add `ndk { abiFilters 'x86', 'armeabi', 'armeabi-v7a' }` to the `defaultConfig` section of the `build.gradle` file of your module.
* Now the abl_link~ external should build within your project. You can use it like any other locally built external. If you use libpd by way of PdService, then the external will already be on Pd's search path, and so patches can use it without further ado.

## What's going on here?

* Link uses ifaddrs, which is not part of the stable Android APIs. So, I looked around and decided to borrow the implementation of ifaddrs that comes with the Android version of Chromium, which is BSD-licensed and IPv6 aware. It's included here, in `external/android-ifaddrs`.
* Other than that, it just took a few straightforward compiler flags to make this work. They're in `CMakeLists.txt`.
* For purposes other than libpd, the build file should be straightforward to adjust. Good luck!


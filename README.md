# abl_link~

Ableton Link integration for Pd. Make sure to clone this repository recursively so you'll have all submodules.

## Desktop version

Users:

* Simply install `abl_link~` from deken and check out the help patch.

Developers:

* Build the external by saying `make` in the `external` directory and install it like any other external.
* Run the metronome patch in `external` to check the timing of the external against the LinkHut sample app.

## Android version

In order to build and run the sample app, you need to be set up for native
Android development. If you'd like to add Link to your own app, check out the
README in the `android` directory.

* Set up your build environment following these instructions: https://developer.android.com/studio/projects/add-native-code.html
* Clone Pd for Android and set it up to build from source (just adding a dependency via jcenter is not enough): https://github.com/libpd/pd-for-android#using-android-studio
* Clone this repository into the same parent directory as Pd for Android. (You can put it somewhere else, but then you'll have to adjust `android/CMakeLists.txt` accordingly.)
* Open AblLinkSample in Android Studio and add PdCore as an AAR package: File > New Module > Import .JAR/.AAR Package. The location of the AAR package should look like `pd-for-android/PdCore/build/outputs/aar/pd-core-1.0.3-SNAPSHOT.aar`.
* Now you should be able to build and run the sample app.

## iOS version

The iOS version is here: https://github.com/libpd/pd-for-ios

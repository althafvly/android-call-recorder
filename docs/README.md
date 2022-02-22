# Calls recording

Here are few ways to record calls:

1. By default (your device works from the box, no additional configuration required)

2. Installing magisk module (https://github.com/topjohnwu/Magisk)

3. Using Accesibility service and 'Voice Recognition' as source.

Few more options:

* Some devices may require adb command to Call Recorder app to work:

    adb shell pm grant com.github.axet.callrecorder android.permission.CAPTURE_AUDIO_OUTPUT

* android.permission.CAPTURE_AUDIO_OUTPUT system permission, some devices may require app to be signed with system keys and build within system image (/system/app folder). Using Magisk module giving the same result.

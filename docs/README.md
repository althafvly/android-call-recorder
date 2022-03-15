# Calls recording

Here are few ways to record calls:

1. By default (your device works from the box, no additional configuration required)

2. Installing magisk module (https://github.com/topjohnwu/Magisk)

3. Using Accesibility service and 'Voice Recognition' as source.

4. Enabling System Mixer

Few more options:

* Some devices may require adb command to Call Recorder app to work:

    adb shell pm grant com.github.axet.callrecorder android.permission.CAPTURE_AUDIO_OUTPUT

Some devices need Call Recorder by be signed with system keys and build within system image.

* android.permission.CAPTURE_AUDIO_OUTPUT system permission, some devices may require app to be signed with system keys and build within system image (/system/app folder). Using Magisk module giving the same result.

# Filenames

You can change the default file name for the new records by choosing one of
the predefined ones or by setting your own format:

- `%c` **contact name** or nothing when no contact name is available
- `%p` **phone number** or nothing if phone number is unavailable
- `%T` **UNIX time**, like "1512340435"
- `%s` **Simple date**, like "2018-02-20 20.32.15"
- `%I` **ISO 8601 date**, like "20181111T201723"
- `%i` **Call direction**, either `↓` or `↑`

Don't bother with withe spaces when information can't be found, _all_ double
spaces are replaced by a single one.


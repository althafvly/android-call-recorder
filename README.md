# Call Recorder

Android friendly!

Call Recorder with custom recording folder.

MOST PHONES DOES NOT SUPPORT CALL RECORDING. Blame Google or your phone manufacturer not me!

If you have any audio issues (no opponent voice, your voice muted or any else). Try Encoder / ogg + all sources, then Encoder / aac / Media Recorder + all sources, if you still experince audio issues - then your phone do not support call recording.

If it fails with high quality sound recording (voice line) this app will switch back to the MIC recording, no ads, open-source, GPLv3.

# Manual install

    gradle installDebug

# Translate

If you want to translate 'Call Recorder' to your language  please read this:

  * [HOWTO-Translate.md](/docs/HOWTO-Translate.md)

# Screenshots

![shot](/docs/shot.png)

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

# User survey's

Check user surveys at project home page here:

  * [User survey's](https://axet.gitlab.io/android-call-recorder/)

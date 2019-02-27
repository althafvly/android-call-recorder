package com.github.axet.callrecorder.services;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.graphics.drawable.Icon;
import android.preference.PreferenceManager;
import android.service.quicksettings.Tile;

import com.github.axet.callrecorder.R;
import com.github.axet.callrecorder.activities.MainActivity;
import com.github.axet.callrecorder.app.CallApplication;
import com.github.axet.callrecorder.app.Storage;

@TargetApi(24)
public class TileService extends android.service.quicksettings.TileService {
    SharedPreferences.OnSharedPreferenceChangeListener receiver = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(CallApplication.PREFERENCE_CALL)) {
                updateTile();
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(this);
        shared.registerOnSharedPreferenceChangeListener(receiver);
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        updateTile();
    }

    void updateTile() {
        Tile tile = getQsTile();
        if (RecordingService.isEnabled(this)) {
            tile.setLabel(getString(R.string.recording_enabled));
            tile.setState(Tile.STATE_ACTIVE);
        } else {
            tile.setLabel(getString(R.string.recording_disabled));
            tile.setState(Tile.STATE_INACTIVE);
        }
        tile.updateTile();
    }

    @Override
    public void onStopListening() {
        super.onStopListening();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(this);
        shared.unregisterOnSharedPreferenceChangeListener(receiver);
    }

    @Override
    public void onClick() {
        super.onClick();
        boolean b = !RecordingService.isEnabled(this);
        if(b && !Storage.permitted(this, MainActivity.PERMISSIONS)){
            MainActivity.startActivity(this, true);
            return;
        }
        RecordingService.setEnabled(this, b);
    }
}

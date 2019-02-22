package com.github.axet.callrecorder.services;

import android.content.Context;
import android.content.Intent;

public class OnExternalReceiver extends com.github.axet.androidlibrary.services.OnExternalReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!isExternal(context))
            return;
        RecordingService.startIfEnabled(context);
    }
}

package com.github.axet.callrecorder.widgets;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.content.res.TypedArray;
import android.media.MediaRecorder;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.github.axet.callrecorder.R;
import com.github.axet.callrecorder.services.VoiceRecognitionAccessibilityService;

public class RecordingSourcePreferenceCompat extends ListPreference {
    public static final String TAG = RecordingSourcePreferenceCompat.class.getSimpleName();

    public static boolean isEnabled(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        String enabledNotificationListeners = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        String packageName = context.getPackageName();
        return enabledNotificationListeners != null && enabledNotificationListeners.contains(packageName);
    }

    public static void show(Context context) {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        context.startActivity(intent);
    }

    public static boolean findService(Context context, Class c) {
        try {
            String s = c.getCanonicalName();
            PackageManager pm = context.getPackageManager();
            PackageInfo info = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_SERVICES);
            for (ServiceInfo i : info.services) {
                if (i.name.equals(s))
                    return true;
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "unable to find service", e);
        }
        return false;
    }

    public RecordingSourcePreferenceCompat(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        create();
    }

    public RecordingSourcePreferenceCompat(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        create();
    }

    public RecordingSourcePreferenceCompat(Context context, AttributeSet attrs) {
        super(context, attrs);
        create();
    }

    public RecordingSourcePreferenceCompat(Context context) {
        super(context);
        create();
    }

    public void create() {
        setWidgetLayoutResource(R.layout.accessibilityservice);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        // https://android.googlesource.com/platform/frameworks/base/+/master/core/res/res/layout/preference.xml
        View view = holder.findViewById(android.R.id.widget_frame);
        String v = getValue();
        int source = Integer.parseInt(v);
        if (Build.VERSION.SDK_INT >= 29 && findService(getContext(), VoiceRecognitionAccessibilityService.class) && source == MediaRecorder.AudioSource.VOICE_RECOGNITION) {
            view.setVisibility(View.VISIBLE);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    show(getContext());
                }
            });
            TextView enabled = (TextView) holder.findViewById(R.id.enabled);
            enabled.setText(isEnabled(getContext()) ? R.string.enabled : R.string.disabled);
        } else {
            view.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean callChangeListener(Object newValue) {
        update(newValue);
        return super.callChangeListener(newValue);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        Object def = super.onGetDefaultValue(a, index);
        update(def);
        return def;
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        super.onSetInitialValue(restoreValue, defaultValue);
        update(getValue()); // defaultValue null after defaults set
    }

    public void update(Object value) {
        String v = (String) value;
        setSummary(v);
    }

    public void onResume() {
        callChangeListener(getValue());
    }
}

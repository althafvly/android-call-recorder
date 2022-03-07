package com.github.axet.callrecorder.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceCategory;
import android.util.AttributeSet;

import com.github.axet.audiolibrary.encoders.Factory;
import com.github.axet.callrecorder.app.Storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;

public class EncodingsPreferenceCompat extends com.github.axet.audiolibrary.widgets.EncodingsPreferenceCompat {

    ArrayList<String> sencoders = new ArrayList<>(Arrays.asList(Storage.ENCODERS));

    public EncodingsPreferenceCompat(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public EncodingsPreferenceCompat(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public EncodingsPreferenceCompat(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EncodingsPreferenceCompat(Context context) {
        super(context);
    }

    @Override
    public boolean isEncoderSupported(String v) {
        if (v.equals(Storage.EXT_3GP))
            return true; // MediaRecorder AMRNB 8kHz
//        if (Build.VERSION.SDK_INT >= 10 && v.equals(Storage.EXT_AMR))
//            return true; // MediaRecorder AMRWB 16kHz
        if (Build.VERSION.SDK_INT >= 10 && v.equals(Storage.EXT_AAC))
            return true; // MediaRecorder MPEG_4 / AAC
//        if (Build.VERSION.SDK_INT >= 16 && v.equals(Storage.EXT_AACHE))
//            return true; // MediaRecorder AACHE;
//        if (Build.VERSION.SDK_INT >= 16 && v.equals(Storage.EXT_AACELD))
//            return true; // MediaRecorder AACELD;
//        if (Build.VERSION.SDK_INT >= 21 && v.equals(Storage.EXT_WEBM))
//            return true; // MediaRecorder WEBM
        return sencoders.contains(v) && Factory.isEncoderSupported(getContext(), v);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        super.onSetInitialValue(restoreValue, defaultValue);
        LinkedHashMap<String, String> mm = getSources();
        mm = filter3gp(mm);
        for (int i = 0; i < Storage.ENCODERS.length; i++) {
            String v = Storage.ENCODERS[i];
            String t = "." + v;
            if (!mm.containsKey(v) && !Storage.isMediaRecorder(v))
                continue;
            mm.put(v, t);
        }
        mm = filter(mm);
        String v = getValue();
        if (mm.size() > 1) {
            setEntryValues(mm.keySet().toArray(new String[0]));
            setEntries(mm.values().toArray(new String[0]));
            int i = findIndexOfValue(v);
            if (i == -1)
                setValueIndex(0);
            else
                setValueIndex(i);
        } else {
            setVisible(false);
        }
        update(v); // defaultValue null after defaults set
    }

    public LinkedHashMap<String, String> filter3gp(LinkedHashMap<String, String> mm) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        for (String v : mm.keySet()) {
            String t = mm.get(v);
            if (Storage.isMediaRecorder(v))
                continue;
            map.put(v, t);
        }
        return map;
    }

    public LinkedHashMap<String, String> filter(LinkedHashMap<String, String> mm) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        for (String v : mm.keySet()) {
            String t = mm.get(v);
            if (Storage.isMediaRecorder(v))
                t += " (MediaRecorder)";
            if (!isEncoderSupported(v))
                continue;
            map.put(v, t);
        }
        return map;
    }

    public void update(Object value) {
        PreferenceCategory filters = (PreferenceCategory) findPreferenceInHierarchy("filters");
        if (filters != null)
            onResumeVol(filters, (String) value);
        String v = (String) value;
        setSummary("." + v);
    }

    void onResumeVol(PreferenceCategory vol, String encoder) {
        boolean b;
        b = !Storage.isMediaRecorder(encoder);
        for (int i = 0; i < vol.getPreferenceCount(); i++)
            vol.getPreference(i).setVisible(b);
        vol.setVisible(b);
    }

    public void onResume() {
        update(getValue());
    }
}

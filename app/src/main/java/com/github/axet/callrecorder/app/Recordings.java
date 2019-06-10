package com.github.axet.callrecorder.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.axet.androidlibrary.widgets.ErrorDialog;
import com.github.axet.callrecorder.R;

import java.util.ArrayList;
import java.util.TreeSet;

public class Recordings extends com.github.axet.audiolibrary.app.Recordings {
    protected View toolbar_i;
    protected View toolbar_o;
    View refresh;
    public TextView progressText;
    public View progressEmpty;

    boolean toolbarFilterIn;
    boolean toolbarFilterOut;

    public static class RecordingHolder extends com.github.axet.audiolibrary.app.Recordings.RecordingHolder {
        LinearLayout s;
        ImageView i;

        public RecordingHolder(View v) {
            super(v);
            s = (LinearLayout) v.findViewById(R.id.recording_status);
            i = (ImageView) v.findViewById(R.id.recording_call);
        }
    }

    public Recordings(Context context, RecyclerView list) {
        super(context, list);
    }

    public void setEmptyView(View empty) {
        this.empty.setEmptyView(empty);
        refresh = empty.findViewById(R.id.refresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                load(false, null);
            }
        });
        progressText = (TextView) empty.findViewById(android.R.id.text1);
        progressEmpty = empty.findViewById(R.id.progress_empty);
    }

    @Override
    public void load(Uri mount, boolean clean, Runnable done) {
        progressText.setText(R.string.recording_list_is_empty);
        refresh.setVisibility(View.GONE);
        if (!com.github.axet.audiolibrary.app.Storage.exists(context, mount)) { // folder may not exist, do not show error
            scan(new ArrayList<com.github.axet.audiolibrary.app.Storage.Node>(), clean, done);
            return;
        }
        try {
            super.load(mount, clean, done);
        } catch (RuntimeException e) {
            Log.e(TAG, "unable to load", e);
            refresh.setVisibility(View.VISIBLE);
            progressText.setText(ErrorDialog.toMessage(e));
            scan(new ArrayList<com.github.axet.audiolibrary.app.Storage.Node>(), clean, done);
        }
    }

    @Override
    public String[] getEncodingValues() {
        return Storage.getEncodingValues(context);
    }

    @Override
    public void cleanDelete(TreeSet<String> delete, Uri f) {
        super.cleanDelete(delete, f);
        String p = CallApplication.getFilePref(f);
        delete.remove(p + CallApplication.PREFERENCE_DETAILS_CONTACT);
        delete.remove(p + CallApplication.PREFERENCE_DETAILS_CALL);
    }

    @Override
    public RecordingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View convertView = inflate(inflater, R.layout.recording, parent);
        return new RecordingHolder(convertView);
    }

    @Override
    public void onBindViewHolder(com.github.axet.audiolibrary.app.Recordings.RecordingHolder hh, int position) {
        super.onBindViewHolder(hh, position);
        RecordingHolder h = (RecordingHolder) hh;
        Storage.RecordingUri u = getItem(position);
        String call = CallApplication.getCall(context, u.uri);
        if (call == null || call.isEmpty()) {
            h.i.setVisibility(View.GONE);
        } else {
            switch (call) {
                case CallApplication.CALL_IN:
                    h.i.setVisibility(View.VISIBLE);
                    h.i.setImageResource(R.drawable.ic_call_received_black_24dp);
                    break;
                case CallApplication.CALL_OUT:
                    h.i.setVisibility(View.VISIBLE);
                    h.i.setImageResource(R.drawable.ic_call_made_black_24dp);
                    break;
            }
        }
    }

    @Override
    protected boolean filter(Storage.RecordingUri f) {
        boolean include = super.filter(f);
        if (include) {
            if (!toolbarFilterIn && !toolbarFilterOut)
                return true;
            String call = CallApplication.getCall(context, f.uri);
            if (call == null || call.isEmpty())
                return false;
            if (toolbarFilterIn)
                return call.equals(CallApplication.CALL_IN);
            if (toolbarFilterOut)
                return call.equals(CallApplication.CALL_OUT);
        }
        return include;
    }

    public void setToolbar(ViewGroup v) {
        toolbar_i = v.findViewById(R.id.toolbar_in);
        toolbar_i.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toolbarFilterIn = !toolbarFilterIn;
                if (toolbarFilterIn)
                    toolbarFilterOut = false;
                selectToolbar();
                load(false, null);
                save();
            }
        });
        toolbar_o = v.findViewById(R.id.toolbar_out);
        toolbar_o.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toolbarFilterOut = !toolbarFilterOut;
                if (toolbarFilterOut)
                    toolbarFilterIn = false;
                selectToolbar();
                load(false, null);
                save();
            }
        });
        super.setToolbar(v);
    }

    protected void selectToolbar() {
        super.selectToolbar();
        selectToolbar(toolbar_i, toolbarFilterIn);
        selectToolbar(toolbar_o, toolbarFilterOut);
    }

    protected void save() {
        super.save();
        final SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = shared.edit();
        edit.putBoolean(CallApplication.PREFERENCE_FILTER_IN, toolbarFilterIn);
        edit.putBoolean(CallApplication.PREFERENCE_FILTER_OUT, toolbarFilterOut);
        edit.commit();
    }

    protected void load() {
        super.load();
        final SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(context);
        toolbarFilterIn = shared.getBoolean(CallApplication.PREFERENCE_FILTER_IN, false);
        toolbarFilterOut = shared.getBoolean(CallApplication.PREFERENCE_FILTER_OUT, false);
    }
}

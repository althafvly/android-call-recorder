package com.github.axet.callrecorder.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.axet.androidlibrary.activities.AppCompatThemeActivity;
import com.github.axet.androidlibrary.app.SuperUser;
import com.github.axet.androidlibrary.preferences.OptimizationPreferenceCompat;
import com.github.axet.androidlibrary.services.StorageProvider;
import com.github.axet.androidlibrary.widgets.ErrorDialog;
import com.github.axet.audiolibrary.app.Sound;
import com.github.axet.callrecorder.R;
import com.github.axet.callrecorder.app.CallApplication;
import com.github.axet.callrecorder.app.Recordings;
import com.github.axet.callrecorder.app.Storage;
import com.github.axet.callrecorder.services.RecordingService;

public class MainActivity extends AppCompatThemeActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    public final static String TAG = MainActivity.class.getSimpleName();

    public static String SHOW_PROGRESS = MainActivity.class.getCanonicalName() + ".SHOW_PROGRESS";
    public static String SET_PROGRESS = MainActivity.class.getCanonicalName() + ".SET_PROGRESS";
    public static String SHOW_LAST = MainActivity.class.getCanonicalName() + ".SHOW_LAST";
    public static String ENABLE = MainActivity.class.getCanonicalName() + ".ENABLE";

    public static final int RESULT_CALL = 1;

    public static final String[] MUST = new String[]{
            Manifest.permission.RECORD_AUDIO,
    };

    public static final String[] PERMISSIONS = SuperUser.concat(MUST, new String[]{
            Manifest.permission.PROCESS_OUTGOING_CALLS,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_CONTACTS, // get contact name by phone number
            Manifest.permission.READ_PHONE_STATE, // read outgoing going calls information
    });

    FloatingActionButton fab;
    FloatingActionButton fab_stop;
    View fab_panel;
    TextView status;
    boolean show;
    Boolean recording;
    int encoding;
    String phone;
    long sec;

    MenuItem resumeCall;

    Recordings recordings;
    Storage storage;
    RecyclerView list;
    Handler handler = new Handler();

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String a = intent.getAction();
            if (a.equals(SHOW_PROGRESS)) {
                encoding = -1;
                show = intent.getBooleanExtra("show", false);
                recording = (Boolean) intent.getExtras().get("recording");
                sec = intent.getLongExtra("sec", 0);
                phone = intent.getStringExtra("phone");
                updatePanel();
            }
            if (a.equals(SET_PROGRESS)) {
                encoding = intent.getIntExtra("set", 0);
                updatePanel();
            }
            if (a.equals(SHOW_LAST)) {
                last();
            }
        }
    };

    public static void showProgress(Context context, boolean show, String phone, long sec, Boolean recording) {
        Intent intent = new Intent(SHOW_PROGRESS);
        intent.putExtra("show", show);
        intent.putExtra("recording", recording);
        intent.putExtra("sec", sec);
        intent.putExtra("phone", phone);
        context.sendBroadcast(intent);
    }

    public static void setProgress(Context context, int p) {
        Intent intent = new Intent(SET_PROGRESS);
        intent.putExtra("set", p);
        context.sendBroadcast(intent);
    }

    public static void last(Context context) {
        Intent intent = new Intent(SHOW_LAST);
        context.sendBroadcast(intent);
    }

    public static void startActivity(Context context) {
        Intent i = new Intent(context, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(i);
    }

    public static void startActivity(Context context, boolean enable) {
        Intent i = new Intent(context, MainActivity.class);
        i.setAction(ENABLE);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(i);
    }

    public static void setSolid(Drawable background, int color) {
        if (background instanceof ShapeDrawable) {
            ShapeDrawable shapeDrawable = (ShapeDrawable) background;
            shapeDrawable.getPaint().setColor(color);
        } else if (background instanceof GradientDrawable) {
            GradientDrawable gradientDrawable = (GradientDrawable) background;
            gradientDrawable.setColor(color);
        } else if (background instanceof ColorDrawable) {
            ColorDrawable colorDrawable = (ColorDrawable) background;
            if (Build.VERSION.SDK_INT >= 11)
                colorDrawable.setColor(color);
        }
    }

    public static String join(String... args) {
        StringBuilder bb = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (bb.length() != 0)
                bb.append(args[0]);
            bb.append(args[i]);
        }
        return bb.toString();
    }

    @Override
    public int getAppTheme() {
        return CallApplication.getTheme(this, R.style.RecThemeLight_NoActionBar, R.style.RecThemeDark_NoActionBar);
    }

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        list = (RecyclerView) findViewById(R.id.list);

        storage = new Storage(this);

        IntentFilter ff = new IntentFilter();
        ff.addAction(SHOW_PROGRESS);
        ff.addAction(SET_PROGRESS);
        ff.addAction(SHOW_LAST);
        registerReceiver(receiver, ff);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab_panel = findViewById(R.id.fab_panel);
        status = (TextView) fab_panel.findViewById(R.id.status);

        fab_stop = (FloatingActionButton) findViewById(R.id.fab_stop);
        fab_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecordingService.stopButton(MainActivity.this);
            }
        });

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RecordingService.stopButton(MainActivity.this);
            }
        });

        updatePanel();

        View empty = findViewById(R.id.empty_list);
        recordings = new Recordings(this, list);
        recordings.setEmptyView(empty);
        list.setAdapter(recordings.empty);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recordings.setToolbar((ViewGroup) findViewById(R.id.recording_toolbar));

        final SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(this);
        if (shared.getString(CallApplication.PREFERENCE_SOURCE, null) == null) {
            String source = "-1";
            if (Build.VERSION.SDK_INT >= 31) {
                source = String.valueOf(MediaRecorder.AudioSource.VOICE_RECOGNITION);
            }
            shared.edit().putString(CallApplication.PREFERENCE_SOURCE, source).apply();
        }

        if (OptimizationPreferenceCompat.needKillWarning(this, CallApplication.PREFERENCE_NEXT))
            OptimizationPreferenceCompat.buildKilledWarning(new ContextThemeWrapper(this, getAppTheme()), true, CallApplication.PREFERENCE_OPTIMIZATION, RecordingService.class).show();
        else if (OptimizationPreferenceCompat.needBootWarning(this, CallApplication.PREFERENCE_BOOT))
            OptimizationPreferenceCompat.buildBootWarning(this, CallApplication.PREFERENCE_BOOT).show();

        RecordingService.startIfEnabled(this);

        Intent intent = getIntent();
        openIntent(intent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        openIntent(intent);
    }

    @SuppressLint("RestrictedApi")
    void openIntent(Intent intent) {
        String a = intent.getAction();
        if (a != null && a.equals(ENABLE)) {
            MenuBuilder m = new MenuBuilder(this);
            MenuItem item = m.add(Menu.NONE, R.id.action_call, Menu.NONE, "");
            item.setEnabled(RecordingService.isEnabled(this));
            onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem call = menu.findItem(R.id.action_call);
        boolean b = RecordingService.isEnabled(this);
        call.setChecked(b);

        MenuItem show = menu.findItem(R.id.action_show_folder);
        Intent ii = StorageProvider.openFolderIntent(this, storage.getStoragePath());
        show.setIntent(ii);
        if (!StorageProvider.isFolderCallable(this, ii, StorageProvider.getProvider().getAuthority()))
            show.setVisible(false);

        MenuItem search = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(search);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                recordings.search(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                recordings.searchClose();
                return true;
            }
        });

        recordings.onCreateOptionsMenu(menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(this);
        if (recordings.onOptionsItemSelected(this, item))
            return true;
        switch (item.getItemId()) {
            case R.id.sort_contact_ask:
            case R.id.sort_contact_desc:
                recordings.onSortOptionSelected(this, item.getItemId());
                return true;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.action_call:
                item.setChecked(!item.isChecked());
                if (item.isChecked() && !Storage.permitted(MainActivity.this, PERMISSIONS, RESULT_CALL)) {
                    resumeCall = item;
                    return true;
                }
                RecordingService.setEnabled(this, item.isChecked());
                return true;
            case R.id.action_show_folder:
                Intent intent = item.getIntent();
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        invalidateOptionsMenu();

        try {
            storage.migrateLocalStorage();
        } catch (RuntimeException e) {
            ErrorDialog.Error(this, e);
        }

        Runnable done = new Runnable() {
            @Override
            public void run() {
                recordings.progressText.setVisibility(View.VISIBLE);
                recordings.progressEmpty.setVisibility(View.GONE);
            }
        };
        recordings.progressText.setVisibility(View.GONE);
        recordings.progressEmpty.setVisibility(View.VISIBLE);

        recordings.load(false, done);

        updateHeader();

        fab.setClickable(true);
    }

    void last() {
        Runnable done = new Runnable() {
            @Override
            public void run() {
                final int selected = getLastRecording();
                recordings.progressText.setVisibility(View.VISIBLE);
                recordings.progressEmpty.setVisibility(View.GONE);
                if (selected != -1) {
                    recordings.select(selected);
                    list.smoothScrollToPosition(selected);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            list.scrollToPosition(selected);
                        }
                    });
                }
            }
        };
        recordings.progressText.setVisibility(View.GONE);
        recordings.progressEmpty.setVisibility(View.VISIBLE);
        recordings.load(false, done);
    }

    int getLastRecording() {
        final SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(this);
        String last = shared.getString(CallApplication.PREFERENCE_LAST, "");
        last = last.toLowerCase();
        for (int i = 0; i < recordings.getItemCount(); i++) {
            Storage.RecordingUri f = recordings.getItem(i);
            String n = Storage.getName(this, f.uri).toLowerCase();
            if (n.equals(last)) {
                SharedPreferences.Editor edit = shared.edit();
                edit.putString(CallApplication.PREFERENCE_LAST, "");
                edit.commit();
                return i;
            }
        }
        return -1;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case RESULT_CALL:
                if (Storage.permitted(this, MUST)) {
                    try {
                        storage.migrateLocalStorage();
                    } catch (RuntimeException e) {
                        ErrorDialog.Error(this, e);
                    }
                    recordings.load(false, null);
                    if (resumeCall != null) {
                        RecordingService.setEnabled(this, resumeCall.isChecked());
                        resumeCall = null;
                    }
                } else {
                    Toast.makeText(this, R.string.not_permitted, Toast.LENGTH_SHORT).show();
                    if (!Storage.permitted(this, MUST)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Permissions");
                        builder.setMessage("Call permissions must be enabled manually");
                        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Storage.showPermissions(MainActivity.this);
                            }
                        });
                        builder.show();
                        resumeCall = null;
                    }
                }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        recordings.close();
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    void updatePanel() {
        fab_panel.setVisibility(show ? View.VISIBLE : View.GONE);
        if (encoding >= 0) {
            status.setText(getString(R.string.encoding_title) + encoding + "%");
            fab.setVisibility(View.GONE);
            fab_stop.setVisibility(View.INVISIBLE);
        } else {
            String text = phone;
            if (!text.isEmpty())
                text += " - ";
            text += CallApplication.formatDuration(this, sec * 1000);
            text = text.trim();
            status.setText(text);
            fab.setVisibility(show ? View.VISIBLE : View.GONE);
            fab_stop.setVisibility(View.INVISIBLE);
        }
        if (recording == null) {
            fab.setVisibility(View.GONE);
        } else if (recording) {
            fab.setImageResource(R.drawable.ic_stop_black_24dp);
        } else {
            fab.setImageResource(R.drawable.ic_play_arrow_black_24dp);
        }
    }

    void updateHeader() {
        Uri f = storage.getStoragePath();
        long free = Storage.getFree(this, f);
        long sec = Storage.average(this, Sound.DEFAULT_AUDIOFORMAT, free);
        TextView text = (TextView) findViewById(R.id.space_left);
        text.setText(CallApplication.formatFree(this, free, sec));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(CallApplication.PREFERENCE_STORAGE))
            recordings.load(true, null);
    }
}

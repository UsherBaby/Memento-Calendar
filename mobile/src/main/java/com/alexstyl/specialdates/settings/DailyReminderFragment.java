package com.alexstyl.specialdates.settings;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;

import com.alexstyl.specialdates.R;
import com.alexstyl.specialdates.analytics.Action;
import com.alexstyl.specialdates.analytics.ActionWithParameters;
import com.alexstyl.specialdates.analytics.Analytics;
import com.alexstyl.specialdates.analytics.AnalyticsProvider;
import com.alexstyl.specialdates.service.DailyReminderService;
import com.alexstyl.specialdates.ui.base.MementoPreferenceFragment;
import com.alexstyl.specialdates.ui.widget.TimePreference;
import com.alexstyl.specialdates.util.Utils;

import java.util.Calendar;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

public class DailyReminderFragment extends MementoPreferenceFragment {

    private static final int EXTERNAL_STORAGE_REQUEST_CODE = 15;
    private CheckBoxPreference enablePreference;
    private RingtonePreference ringtonePreference;
    private TimePreference timePreference;
    private Analytics analytics;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        analytics = AnalyticsProvider.getAnalytics(getActivity());
        setHasOptionsMenu(true);
        addPreferencesFromResource(R.xml.preference_dailyreminder);

        enablePreference = findPreference(R.string.key_daily_reminder);
        enablePreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Context context = getActivity();
                boolean isChecked = (boolean) newValue;
                MainPreferenceActivity.setDailyReminder(context, isChecked);
                ActionWithParameters event = new ActionWithParameters(Action.DAILY_REMINDER, "enabled", isChecked);
                analytics.trackAction(event);
                if (isChecked) {
                    DailyReminderService.setup(context);
                } else {
                    DailyReminderService.cancel(context);
                }
                return true;
            }

        });

        ringtonePreference = findPreference(R.string.key_daily_reminder_ringtone);
        ringtonePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (isExternalStoragePermissionPressent()) {
                    // the permission exists. Let the system handle the event
                    return false;
                } else {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_REQUEST_CODE);
                    return true;
                }
            }

            public boolean isExternalStoragePermissionPressent() {
                return ActivityCompat.checkSelfPermission(getActivity(), READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
            }
        });
        ringtonePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                updateRingtoneSummary((String) newValue);
                return true;
            }
        });

        timePreference = findPreference(R.string.key_daily_reminder_time);
        timePreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int[] time = (int[]) newValue;
                updateTimeSet(time);
                MainPreferenceActivity.setDailyReminderTime(getActivity(), time);
                DailyReminderService.setup(getActivity());
                return true;
            }
        });

        hideVibratorSettingIfNotPresent();
    }

    private void hideVibratorSettingIfNotPresent() {
        Preference vibratePreference = findPreference(getString(R.string.key_daily_reminder_vibrate_enabled));
        if (!Utils.hasVibrator(getActivity())) {
            // hide the vibrator preference if the device doesn't support
            // vibration
            getPreferenceScreen().removePreference(vibratePreference);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent up = new Intent(getActivity(), MainPreferenceActivity.class);
                up.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(up);
                getActivity().finish();
                return true;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        enablePreference.setChecked(MainPreferenceActivity.isDailyReminderSet(getActivity()));
        updateRingtoneSummary(MainPreferenceActivity.getDailyReminderRingtone(getActivity()));
        updateTimeSet(MainPreferenceActivity.getDailyReminderTime(getActivity()));
    }

    private void updateTimeSet(int[] time) {
        String timeString = getStringHour(time);
        String summary = String.format(getString(R.string.daily_reminder_time_summary), timeString);
        timePreference.setSummary(summary);

    }

    private String getStringHour(int[] time) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, time[0]);
        cal.set(Calendar.MINUTE, time[1]);
        return getHour(getActivity(), cal).toString();
    }

    // Char sequence for a 12 hour format.
    private static final CharSequence DEFAULT_FORMAT_12_HOUR = "hh:mm a";
    // Char sequence for a 24 hour format.
    private static final CharSequence DEFAULT_FORMAT_24_HOUR = "kk:mm";

    public static CharSequence getHour(Context context, Calendar cal) {
        boolean is24Hour = DateFormat.is24HourFormat(context);
        if (is24Hour) {
            return DateFormat.format(DEFAULT_FORMAT_24_HOUR, cal);
        } else {
            return DateFormat.format(DEFAULT_FORMAT_12_HOUR, cal);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EXTERNAL_STORAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            ringtonePreference.onClick();
        }
    }

    private void updateRingtoneSummary(String uri) {
        String name = null;
        if (!TextUtils.isEmpty(uri)) {
            Uri ringtoneUri = Uri.parse(uri);
            Ringtone ringtone = RingtoneManager.getRingtone(getActivity(), ringtoneUri);

            if (ringtone != null) {
                name = ringtone.getTitle(getActivity());
            }
        } else {
            name = getString(R.string.no_sound);
        }
        ringtonePreference.setSummary(name);
    }
}

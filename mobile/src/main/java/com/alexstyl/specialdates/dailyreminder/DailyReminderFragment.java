package com.alexstyl.specialdates.dailyreminder;

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
import android.os.Vibrator;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.support.v4.app.ActivityCompat;
import android.view.MenuItem;

import com.alexstyl.specialdates.R;
import com.alexstyl.specialdates.analytics.Action;
import com.alexstyl.specialdates.analytics.ActionWithParameters;
import com.alexstyl.specialdates.analytics.Analytics;
import com.alexstyl.specialdates.analytics.AnalyticsProvider;
import com.alexstyl.specialdates.settings.MainPreferenceActivity;
import com.alexstyl.specialdates.ui.base.MementoPreferenceFragment;
import com.alexstyl.specialdates.ui.widget.TimePreference;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

public class DailyReminderFragment extends MementoPreferenceFragment {

    private static final int EXTERNAL_STORAGE_REQUEST_CODE = 15;
    private DailyReminderPreferences preferences;
    private CheckBoxPreference enablePreference;
    private RingtonePreference ringtonePreference;
    private TimePreference timePreference;
    private Analytics analytics;
    private HourLabelCreator hourLabelCreator;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = DailyReminderPreferences.newInstance(getActivity());
        analytics = AnalyticsProvider.getAnalytics(getActivity());
        hourLabelCreator = new HourLabelCreator(getActivity());
        setHasOptionsMenu(true);
        addPreferencesFromResource(R.xml.preference_dailyreminder);

        enablePreference = findPreference(R.string.key_daily_reminder);
        enablePreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Context context = getActivity();
                boolean isChecked = (boolean) newValue;
                preferences.setEnabled(isChecked);
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

            boolean isExternalStoragePermissionPressent() {
                return ActivityCompat.checkSelfPermission(getActivity(), READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
            }
        });
        ringtonePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                updateRingtoneSummary(Uri.parse(newValue.toString()));
                return true;
            }
        });

        timePreference = findPreference(R.string.key_daily_reminder_time);
        timePreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Time time = Time.valueOf((int[]) newValue);
                updateTimeSet(time);
                preferences.setDailyReminderTime(time);
                DailyReminderService.setup(getActivity());
                return true;
            }
        });

        hideVibratorSettingIfNotPresent();
    }

    private void hideVibratorSettingIfNotPresent() {
        Preference vibratePreference = findPreference(getString(R.string.key_daily_reminder_vibrate_enabled));
        if (doesNotSupportVibration()) {
            // hide the vibrator preference if the device doesn't support
            // vibration
            getPreferenceScreen().removePreference(vibratePreference);
        }
    }

    public boolean doesNotSupportVibration() {
        Vibrator vibr = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        return !vibr.hasVibrator();
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
        enablePreference.setChecked(preferences.isEnabled());
        updateRingtoneSummary(preferences.getRingtone());
        updateTimeSet(preferences.getDailyReminderTime());
    }

    private void updateTimeSet(Time time) {
        CharSequence timeLabel = hourLabelCreator.getLabelOf(time);
        String summary = getString(R.string.daily_reminder_time_summary, timeLabel);
        timePreference.setSummary(summary);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EXTERNAL_STORAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            ringtonePreference.onClick();
        }
    }

    private void updateRingtoneSummary(Uri ringtoneUri) {
        if (ringtoneUri.toString().length() > 0) {
            Ringtone ringtone = RingtoneManager.getRingtone(getActivity(), ringtoneUri);
            ringtonePreference.setSummary(ringtone.getTitle(getActivity()));
        } else {
            ringtonePreference.setSummary(R.string.ringtone_silent);
        }
    }
}

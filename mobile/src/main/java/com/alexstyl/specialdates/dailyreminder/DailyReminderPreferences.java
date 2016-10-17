package com.alexstyl.specialdates.dailyreminder;

import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;

import com.alexstyl.specialdates.EasyPreferences;
import com.alexstyl.specialdates.R;

import java.util.Locale;

public final class DailyReminderPreferences {

    private static final String DEFAULT_DAILY_REMINDER_TIME = "08:00";

    private final EasyPreferences preferences;

    public static DailyReminderPreferences newInstance(Context context) {
        EasyPreferences defaultPreferences = EasyPreferences.createForDefaultPreferences(context);
        return new DailyReminderPreferences(defaultPreferences);
    }

    private DailyReminderPreferences(EasyPreferences preferences) {
        this.preferences = preferences;
    }

    public Time getDailyReminderTime() {
        String time = preferences.getString(R.string.key_daily_reminder_time, DEFAULT_DAILY_REMINDER_TIME);
        return Time.valueOf(time);
    }

    public void setDailyReminderTime(Time time) {
        preferences.setString(R.string.key_daily_reminder_time, labelOf(time));

    }

    private static String labelOf(Time time) {
        return String.format(Locale.US, "%s:%s", time.getHourOfDay(), time.getMinutes());
    }

    public boolean isEnabled() {
        return preferences.getBoolean(R.string.key_daily_reminder, true);
    }

    public void setEnabled(boolean value) {
        preferences.setBoolean(R.string.key_daily_reminder, value);
    }

    public Uri getRingtone() {
        String selectedRingtone = preferences.getString(R.string.key_daily_reminder_ringtone, null);
        if (selectedRingtone == null) {
            return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }
        return Uri.parse(selectedRingtone);
    }

    public boolean isVibrationEnabled() {
        return preferences.getBoolean(R.string.key_daily_reminder_vibrate_enabled, false);
    }
}

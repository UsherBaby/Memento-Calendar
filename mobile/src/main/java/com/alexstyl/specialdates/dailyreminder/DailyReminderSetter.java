package com.alexstyl.specialdates.dailyreminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.novoda.notils.logger.simple.Log;

class DailyReminderSetter {

    private static final int DAILY_REMINDER_REQUEST_CODE = 1970;

    private final AlarmManager alarmManager;
    private final DailyReminderPreferences preferences;
    private final Context context;

    DailyReminderSetter(Context context, AlarmManager alarmManager, DailyReminderPreferences preferences) {
        this.context = context;
        this.alarmManager = alarmManager;
        this.preferences = preferences;
    }

    void refreshReminder() {
        Time selectedTime = preferences.getDailyReminderTime();
//                .addHour(1); // add one hour so that we don't fire instantly
        setNextReminderOn(selectedTime);
    }

    void setNextReminderOn(Time selectedTime) {
        Log.d("Setting daily reminder on " + selectedTime);
        PendingIntent pendingIntent = dailyReminderService();
        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                selectedTime.inMillis(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent
        );
    }

    private PendingIntent dailyReminderService() {
        return PendingIntent.getService(
                context,
                DAILY_REMINDER_REQUEST_CODE,
                new Intent(context, DailyReminderService.class),
                PendingIntent.FLAG_UPDATE_CURRENT
        );
    }

    void cancelReminder() {
        alarmManager.cancel(dailyReminderService());

    }
}

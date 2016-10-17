package com.alexstyl.specialdates.dailyreminder;

import android.app.AlarmManager;

class DailyReminderSetter {
    private final AlarmManager alarmManager;
    private final DailyReminderPreferences preferences;

    DailyReminderSetter(AlarmManager alarmManager, DailyReminderPreferences preferences) {
        this.alarmManager = alarmManager;
        this.preferences = preferences;
    }

    void refreshReminder() {
        // TODO
    }

    void cancelReminder() {

    }
}

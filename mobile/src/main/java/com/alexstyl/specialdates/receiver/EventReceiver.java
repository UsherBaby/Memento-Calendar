package com.alexstyl.specialdates.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.alexstyl.specialdates.BuildConfig;
import com.alexstyl.specialdates.ErrorTracker;
import com.alexstyl.specialdates.dailyreminder.DailyReminderPreferences;
import com.alexstyl.specialdates.dailyreminder.DailyReminderService;
import com.alexstyl.specialdates.wear.WearSyncService;
import com.alexstyl.specialdates.widgetprovider.TodayWidgetProvider;

/**
 * The EventReceiver listens to various broadcasts
 *
 * @author Alex
 */
public class EventReceiver extends BroadcastReceiver {

    public static final String ACTION_START_DAILYREMINDER = BuildConfig.APPLICATION_ID + ".start_daily_reminder";
    private DailyReminderPreferences dailyReminderPreferences;

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent == null || intent.getAction() == null) {
            return;
        }
        dailyReminderPreferences = DailyReminderPreferences.newInstance(context);
        String action = intent.getAction();
        if (ACTION_START_DAILYREMINDER.equalsIgnoreCase(action)) {
            onStartDailyReminder(context);
        } else if (Intent.ACTION_BOOT_COMPLETED.equalsIgnoreCase(action)) {
            onBootCompleted(context);
        } else if (Intent.ACTION_LOCALE_CHANGED.equalsIgnoreCase(action)) {
            onLocaleChanged(context);
        }
    }

    private void onLocaleChanged(Context context) {
        ErrorTracker.onLocaleChanged();
        TodayWidgetProvider.updateWidgets(context);
        WearSyncService.startService(context);
    }

    private void onBootCompleted(Context context) {
        if (dailyReminderPreferences.isVibrationEnabled()) {
            DailyReminderService.setup(context);
        }
        TodayWidgetProvider.updateWidgets(context);
        WearSyncService.startService(context);
    }

    private void onStartDailyReminder(Context context) {
        if (dailyReminderPreferences.isVibrationEnabled()) {
            DailyReminderService.startService(context);
        }
        WearSyncService.startService(context);
    }

}

package com.alexstyl.specialdates.dailyreminder;

import android.Manifest;
import android.app.AlarmManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.alexstyl.specialdates.BuildConfig;
import com.alexstyl.specialdates.date.Date;
import com.alexstyl.specialdates.events.bankholidays.BankHoliday;
import com.alexstyl.specialdates.events.bankholidays.BankHolidaysPreferences;
import com.alexstyl.specialdates.events.bankholidays.GreekBankHolidays;
import com.alexstyl.specialdates.events.namedays.NamedayLocale;
import com.alexstyl.specialdates.events.namedays.NamedayPreferences;
import com.alexstyl.specialdates.events.namedays.NamesInADate;
import com.alexstyl.specialdates.events.namedays.calendar.EasterCalculator;
import com.alexstyl.specialdates.events.namedays.calendar.NamedayCalendar;
import com.alexstyl.specialdates.events.namedays.calendar.resource.NamedayCalendarProvider;
import com.alexstyl.specialdates.events.peopleevents.ContactEvents;
import com.alexstyl.specialdates.images.ImageLoader;
import com.alexstyl.specialdates.permissions.PermissionChecker;
import com.alexstyl.specialdates.service.PeopleEventsProvider;
import com.alexstyl.specialdates.util.Notifier;
import com.novoda.notils.logger.simple.Log;

import java.util.List;

/**
 * A service that looks up all events on the specified date and notifies the user about it
 */
public class DailyReminderService extends IntentService {

    private NamedayPreferences namedayPreferences;
    private NamedayCalendarProvider namedayCalendarProvider;

    private BankHolidaysPreferences bankHolidaysPreferences;
    private PermissionChecker permissionChecker;
    private Notifier notifier;
    private DailyReminderPreferences preferences;

    public DailyReminderService() {
        super(DailyReminderService.class.getSimpleName());
    }

    public static void startService(Context context) {
        Intent service = new Intent(context, DailyReminderService.class);
        context.startService(service);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        notifier = new Notifier(this, getResources());
        namedayPreferences = NamedayPreferences.newInstance(this);
        namedayCalendarProvider = NamedayCalendarProvider.newInstance(this.getResources());
        bankHolidaysPreferences = BankHolidaysPreferences.newInstance(this);
        permissionChecker = new PermissionChecker(this);
        preferences = DailyReminderPreferences.newInstance(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        PeopleEventsProvider provider = PeopleEventsProvider.newInstance(this);
        Date today = getDayDateToDisplay();

        if (hasContactPermission()) {
            ContactEvents celebrationDate = provider.getCelebrationDateFor(today);
            if (containsAnyContactEvents(celebrationDate)) {
                ImageLoader imageLoader = ImageLoader.createSquareThumbnailLoader(getResources());
                notifier.forDailyReminder(celebrationDate, imageLoader, preferences);
            }
        }
        if (namedaysAreEnabledForAllCases()) {
            notifyForNamedaysFor(today);
        }
        if (bankholidaysAreEnabled()) {
            notifyForBankholidaysFor(today);
        }
    }

    private boolean hasContactPermission() {
        return permissionChecker.hasPermission(Manifest.permission.READ_CONTACTS);
    }

    private Date getDayDateToDisplay() {
        if (BuildConfig.DEBUG) {
            DailyReminderDebugPreferences preferences = DailyReminderDebugPreferences.newInstance(this);
            if (preferences.isFakeDateEnabled()) {
                Date selectedDate = preferences.getSelectedDate();
                Log.d("Using DEBUG date to display: " + selectedDate);
                return selectedDate;
            }
        }
        return Date.today();
    }

    private boolean containsAnyContactEvents(ContactEvents celebrationDate) {
        return celebrationDate.size() > 0;
    }

    private boolean namedaysAreEnabledForAllCases() {
        return namedayPreferences.isEnabled() && !namedayPreferences.isEnabledForContactsOnly();
    }

    private void notifyForNamedaysFor(Date date) {
        NamedayLocale locale = namedayPreferences.getSelectedLanguage();
        NamedayCalendar namedayCalendar = namedayCalendarProvider.loadNamedayCalendarForLocale(locale, date.getYear());
        NamesInADate names = namedayCalendar.getAllNamedayOn(date);
        if (containsNames(names)) {
            notifier.forNamedays(names.getNames(), date);
        }
    }

    private boolean bankholidaysAreEnabled() {
        return bankHolidaysPreferences.isEnabled();
    }

    private void notifyForBankholidaysFor(Date date) {
        BankHoliday bankHoliday = findBankholidayFor(date);
        if (bankHoliday != null) {
            notifier.forBankholiday(date, bankHoliday);
        }
    }

    private BankHoliday findBankholidayFor(Date date) {
        EasterCalculator calculator = new EasterCalculator();
        Date easter = calculator.calculateEasterForYear(date.getYear());
        List<BankHoliday> bankHolidays = new GreekBankHolidays(easter).getBankHolidaysForYear();
        for (BankHoliday bankHoliday : bankHolidays) {
            if (bankHoliday.getDate().equals(date)) {
                return bankHoliday;
            }
        }
        return null;
    }

    private boolean containsNames(NamesInADate names) {
        return names.nameCount() > 0;
    }

    public static void setup(Context context) {
        DailyReminderPreferences dailyReminderPreferences = DailyReminderPreferences.newInstance(context);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);

        DailyReminderSetter dailyReminderSetter = new DailyReminderSetter(alarmManager, dailyReminderPreferences);
        dailyReminderSetter.refreshReminder();
    }

    public static void cancel(Context context) {
        DailyReminderPreferences dailyReminderPreferences = DailyReminderPreferences.newInstance(context);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);

        DailyReminderSetter dailyReminderSetter = new DailyReminderSetter(alarmManager, dailyReminderPreferences);
        dailyReminderSetter.cancelReminder();
    }
}

package com.alexstyl.specialdates.dailyreminder;

import android.support.annotation.NonNull;
import android.support.annotation.Size;

import java.util.Calendar;

public final class Time {

    private final Calendar calendar;
    private static final String TIME_SEPARATOR = ":";
    private static int HOUR_INDEX = 0;
    private static int MINUTE_INDEX = 1;

    /**
     * Creates a new {@link Time} object out of the given representation of time
     *
     * @param rawTime A string of a "06:00" format. Hour is considered to be 24hour formatted
     */
    public static Time valueOf(String rawTime) {
        String[] split = rawTime.split(TIME_SEPARATOR);
        int hourOfDay = Integer.valueOf(split[HOUR_INDEX]);
        int minuteOfHour = Integer.valueOf(split[MINUTE_INDEX]);
        Calendar calendar = calendarOf(hourOfDay, minuteOfHour);
        return new Time(calendar);
    }

    /**
     * Creates a new {@link Time} object out of the given representation of time
     *
     * @param rawTime A string of a "06:00" format. Hour is considered to be 24hour formatted
     */
    public static Time valueOf(@Size(2) int[] rawTime) {
        int hourOfDay = rawTime[HOUR_INDEX];
        int minuteOfHour = rawTime[MINUTE_INDEX];
        Calendar calendar = calendarOf(hourOfDay, minuteOfHour);
        return new Time(calendar);
    }

    @NonNull
    private static Calendar calendarOf(int hourOfDay, int minuteOfHour) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minuteOfHour);
        return calendar;
    }

    private Time(Calendar calendar) {
        this.calendar = calendar;
    }

    int getHourOfDay() {
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    int getMinutes() {
        return calendar.get(Calendar.MINUTE);
    }

    public long inMillis() {
        return calendar.getTimeInMillis();
    }

    @Override
    public String toString() {
        return getHourOfDay() + ":" + getMinutes();
    }
}

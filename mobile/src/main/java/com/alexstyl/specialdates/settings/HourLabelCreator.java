package com.alexstyl.specialdates.settings;

import android.content.Context;
import android.text.format.DateFormat;

import com.alexstyl.specialdates.dailyreminder.Time;

final class HourLabelCreator {

    private static final CharSequence DEFAULT_FORMAT_12_HOUR = "hh:mm a";
    private static final CharSequence DEFAULT_FORMAT_24_HOUR = "kk:mm";

    private final Context context;

    HourLabelCreator(Context context) {
        this.context = context;
    }

    CharSequence getLabelOf(Time time) {
        boolean is24Hour = DateFormat.is24HourFormat(context);
        long timeInMillis = time.inMillis();
        if (is24Hour) {
            return DateFormat.format(DEFAULT_FORMAT_24_HOUR, timeInMillis);
        } else {
            return DateFormat.format(DEFAULT_FORMAT_12_HOUR, timeInMillis);
        }

    }
}

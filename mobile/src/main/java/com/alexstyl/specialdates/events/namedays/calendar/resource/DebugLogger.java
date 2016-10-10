package com.alexstyl.specialdates.events.namedays.calendar.resource;

import android.util.Log;

import com.alexstyl.Logger;

import java.util.Locale;

public class DebugLogger implements Logger {
    private final String tag;

    public DebugLogger(String tag) {
        this.tag = tag;
    }

    @Override
    public void log(String format, Object... arguments) {
        Log.d(tag, String.format(Locale.US, format, arguments));
    }
}

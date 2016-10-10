package com.alexstyl.specialdates.events.namedays.calendar.resource;

import android.content.res.Resources;

import com.alexstyl.TimeWatch;
import com.alexstyl.specialdates.ErrorTracker;
import com.alexstyl.specialdates.events.namedays.NamedayBundle;
import com.alexstyl.specialdates.events.namedays.NamedayLocale;
import com.alexstyl.specialdates.events.namedays.calendar.NamedayCalendar;

import org.json.JSONArray;
import org.json.JSONException;

public class NamedayCalendarProvider {

    private static NamedayCalendar cachedCalendar;

    private final SpecialNamedaysHandlerFactory factory;
    private final NamedayJSONResourceProvider jsonProvider;

    public static NamedayCalendarProvider newInstance(Resources resources) {
        NamedayJSONResourceLoader loader = new AndroidJSONResourceLoader(resources);
        NamedayJSONResourceProvider jsonResourceProvider = new NamedayJSONResourceProvider(loader);
        SpecialNamedaysHandlerFactory factory = new SpecialNamedaysHandlerFactory();
        return new NamedayCalendarProvider(jsonResourceProvider, factory);
    }

    // eventually we will use DI to provide this, so don't worry about it being `public`
    public NamedayCalendarProvider(NamedayJSONResourceProvider jsonProvider, SpecialNamedaysHandlerFactory factory) {
        this.factory = factory;
        this.jsonProvider = jsonProvider;
    }

    public NamedayCalendar loadNamedayCalendarForLocale(NamedayLocale locale, int year) {
        if (hasRequestedSameCalendar(locale, year)) {
            return cachedCalendar;
        }
        String tag = "namedayCalendar";
        TimeWatch timeWatch = new TimeWatch(new DebugLogger(tag));
        timeWatch.start(tag);
        NamedayJSON namedayJSON = getNamedayJSONFor(locale);
        SpecialNamedays specialCaseHandler = getSpecialnamedaysHandler(locale, namedayJSON);
        NamedayBundle namedaysBundle = getNamedayBundle(locale, namedayJSON);
        NamedayCalendar namedayCalendar = new NamedayCalendar(locale, namedaysBundle, specialCaseHandler, year);

        cacheCalendar(namedayCalendar);

        timeWatch.stop(tag);
        return namedayCalendar;
    }

    private boolean hasRequestedSameCalendar(NamedayLocale locale, int year) {
        return cachedCalendar != null && cachedCalendar.getYear() == year && cachedCalendar.getLocale().equals(locale);
    }

    private NamedayJSON getNamedayJSONFor(NamedayLocale locale) {
        try {
            return jsonProvider.getNamedayJSONFor(locale);
        } catch (JSONException e) {
            ErrorTracker.track(e);
            return new NamedayJSON(new JSONArray(), new JSONArray());
        }
    }

    private SpecialNamedays getSpecialnamedaysHandler(NamedayLocale locale, NamedayJSON namedayJSON) {
        return factory.createStrategyForLocale(locale, namedayJSON);
    }

    private NamedayBundle getNamedayBundle(NamedayLocale locale, NamedayJSON namedayJSON) {
        if (locale.isComparedBySound()) {
            return NamedayJSONParser.getNamedaysFromJSONasSounds(namedayJSON);
        } else {
            return NamedayJSONParser.getNamedaysFrom(namedayJSON);
        }
    }

    private void cacheCalendar(NamedayCalendar namedayCalendar) {
        cachedCalendar = namedayCalendar;
    }

}

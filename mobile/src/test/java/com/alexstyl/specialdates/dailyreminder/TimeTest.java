package com.alexstyl.specialdates.dailyreminder;

import java.util.Calendar;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static android.text.format.DateUtils.SECOND_IN_MILLIS;
import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class TimeTest {

    @Test
    public void timeInMillisIsCorrect() {
        Time time = Time.valueOf("6:00");
        long timeInMilis = time.inMillis();
        long calendarInMilis = calendarOf(time);

        assertThat(Math.abs(calendarInMilis - timeInMilis)).isLessThan(SECOND_IN_MILLIS);

    }

    private long calendarOf(Time time) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, time.getHourOfDay());
        calendar.set(Calendar.MINUTE, time.getMinutes());
        return calendar.getTimeInMillis();
    }
}

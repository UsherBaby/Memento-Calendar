package com.alexstyl;

import java.util.HashMap;
import java.util.Map;

public class TimeWatch {
    private final Logger logger;
    private final Map<String, Long> times;

    public TimeWatch(Logger logger) {
        this.logger = logger;
        times = new HashMap<>();
    }

    public void start(String label) {
        times.put(label, now());
    }

    public void stop(String label) {
        Long start = times.get(label);
        logger.log("%s took %dms", label, (now() - start));
    }

    private static Long now() {
        return System.currentTimeMillis();
    }

}

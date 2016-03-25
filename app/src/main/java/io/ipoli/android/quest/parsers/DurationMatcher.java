package io.ipoli.android.quest.parsers;

import android.support.annotation.NonNull;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/19/16.
 */
public class DurationMatcher implements QuestTextMatcher<Integer> {

    private static final String DURATION_PATTERN = " for (\\d{1,3})\\s?(hours|hour|h|minutes|minute|mins|min|m)(?: and (\\d{1,3})\\s?(minutes|minute|mins|min|m))?($|\\s)";
    Pattern pattern = Pattern.compile(DURATION_PATTERN, Pattern.CASE_INSENSITIVE);

    @Override
    public String match(String text) {

        Matcher dm = createMatcher(text);
        if (dm.find()) {
            return dm.group().trim();
        }
        return "";
    }

    @NonNull
    private Matcher createMatcher(String text) {
        return pattern.matcher(text);
    }

    @Override
    public Integer parse(String text) {
        Matcher dm = createMatcher(text);
        if (dm.find()) {
            int fd = Integer.valueOf(dm.group(1));
            String fUnit = dm.group(2);
            int duration = fd;
            if (fUnit.startsWith("h")) {
                duration = (int) TimeUnit.HOURS.toMinutes(fd);
            }

            if (dm.group(3) != null && dm.group(4) != null) {
                duration += Integer.valueOf(dm.group(3));
            }
            return duration;
        }
        return -1;
    }

    public Integer parseShort(String text) {
        return parse(" for " + text);
    }
}

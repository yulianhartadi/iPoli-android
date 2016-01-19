package io.ipoli.android.app.services;

import org.ocpsoft.prettytime.nlp.PrettyTimeParser;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.ipoli.android.R;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/13/16.
 */
public enum Command {

    ADD_QUEST("^(add quest|aq) (.+?)$", R.string.short_cmd_add_quest, R.string.desc_cmd_add_quest),
    ADD_TODAY_QUEST("^(add today quest|atq) (.+?)$", R.string.short_cmd_add_today_quest, R.string.desc_cmd_add_today_quest),
    SHOW_QUESTS("^(show quests|sq)$", R.string.short_cmd_show_quests, R.string.desc_cmd_show_quests),
    PLAN_TODAY("^(plan today|pt)$", R.string.short_cmd_plan_today, R.string.desc_cmd_plan_today),
    REVIEW_TODAY("^(review today|rt)$", R.string.short_cmd_review_today, R.string.desc_cmd_review_today),
    RENAME("^(rename|re) (\\w+)$", R.string.short_cmd_rename, R.string.desc_cmd_rename),
    HELP("^(help|h)$", R.string.short_cmd_help, R.string.desc_cmd_help),
    UNKNOWN("", 0, 0);

    private static final String DURATION_PATTERN = " for (\\d{1,3})\\s?(hours|hour|h|minutes|minute|mins|min|m)(?: and (\\d{1,3})\\s?(minutes|minute|mins|min|m))?";
    private static final String START_TIME_PATTERN = " at (\\d{1,2}[:|\\.]?(\\d{2})?\\s?(am|pm)?)";

    private final String pattern;
    private final int shortCommandText;
    private final int helpText;
    private HashMap<String, Object> parameters;

    Command(String pattern, int shortCommandText, int helpText) {
        this.pattern = pattern;
        this.shortCommandText = shortCommandText;
        this.helpText = helpText;
        this.parameters = new HashMap<>();
    }

    public int getHelpText() {
        return helpText;
    }

    public HashMap<String, Object> getParameters() {
        return parameters;
    }

    public int getShortCommandText() {
        return shortCommandText;
    }

    @Override
    public String toString() {
        return this.name().toLowerCase().replace("_", " ");
    }

    public static Command parseText(String text) {

        PrettyTimeParser parser = new PrettyTimeParser();

        String normalized = text.trim().replaceAll("\\s+", " ");
        for (Command cmd : values()) {
            Pattern pattern = Pattern.compile(cmd.pattern, Pattern.CASE_INSENSITIVE);
            Matcher m = pattern.matcher(normalized);
            if (m.find()) {
                if (m.groupCount() > 1) {
                    String txt = m.group(2);
                    if (cmd == RENAME) {
                        cmd.parameters.put("name", txt);
                    } else if (cmd == ADD_QUEST || cmd == ADD_TODAY_QUEST) {

                        Pattern dp = Pattern.compile(DURATION_PATTERN, Pattern.CASE_INSENSITIVE);
                        Matcher dm = dp.matcher(txt);
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

                            cmd.parameters.put("duration", duration);
                            txt = txt.replace(dm.group(), " ");
                            txt = txt.trim().replaceAll("\\s+", " ");
                        }

                        Pattern stp = Pattern.compile(START_TIME_PATTERN, Pattern.CASE_INSENSITIVE);
                        Matcher stm = stp.matcher(txt);

                        if (stm.find()) {
                            List<Date> dates = parser.parse(stm.group());
                            if (!dates.isEmpty()) {
                                Date startTime = dates.get(0);
                                cmd.parameters.put("startTime", startTime);
                                txt = txt.replace(stm.group(), " ");
                                txt = txt.trim().replaceAll("\\s+", " ");
                            }
                        }

                        cmd.parameters.put("name", txt.trim());
                    }
                }
                return cmd;
            }
        }

        return UNKNOWN;
    }
}

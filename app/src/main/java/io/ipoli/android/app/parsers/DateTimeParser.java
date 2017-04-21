package io.ipoli.android.app.parsers;

import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/9/17.
 * Pretty much copy-paste from here {@see https://github.com/ocpsoft/prettytime/blob/master/nlp/src/main/java/org/ocpsoft/prettytime/nlp/PrettyTimeParser.java}
 */
public class DateTimeParser {
    private final Parser parser;
    private Map<String, String> translations = new HashMap<>();

    private final String[] tensNames = {
            "",
            " ten",
            " twenty",
            " thirty",
            " forty",
            " fifty",
            " sixty",
            " seventy",
            " eighty",
            " ninety"
    };

    private final String[] numNames = {
            "",
            " one",
            " two",
            " three",
            " four",
            " five",
            " six",
            " seven",
            " eight",
            " nine",
            " ten",
            " eleven",
            " twelve",
            " thirteen",
            " fourteen",
            " fifteen",
            " sixteen",
            " seventeen",
            " eighteen",
            " nineteen"
    };

    public DateTimeParser() {
        parser = new Parser(TimeZone.getDefault());
        for (int hours = 0; hours < 24; hours++)
            for (int min = 0; min < 60; min++)
                translations.put(provideRepresentation(hours * 100 + min), "" + hours * 100 + min);
        translations.put(provideRepresentation(60), "" + 60);
        translations.put(provideRepresentation(70), "" + 70);
        translations.put(provideRepresentation(80), "" + 80);
        translations.put(provideRepresentation(90), "" + 90);
        translations.put(provideRepresentation(100), "" + 100);
    }

    /**
     * Provides a string representation for the number passed. This method works for limited set of numbers as parsing
     * will only be done at maximum for 2400, which will be used in military time format.
     */
    private String provideRepresentation(int number) {
        String key;

        if (number == 0)
            key = "zero";
        else if (number < 20)
            key = numNames[number];
        else if (number < 100) {
            int unit = number % 10;
            key = tensNames[number / 10] + numNames[unit];
        } else {
            int unit = number % 10;
            int ten = number % 100 - unit;
            int hundred = (number - ten) / 100;
            if (hundred < 20)
                key = numNames[hundred] + " hundred";
            else
                key = tensNames[hundred / 10] + numNames[hundred % 10] + " hundred";
            if (ten + unit < 20 && ten + unit > 10)
                key += numNames[ten + unit];
            else
                key += tensNames[ten / 10] + numNames[unit];
        }
        return key.trim();
    }

    /**
     * Parse the given text and return a {@link List} with all discovered {@link Date} instances.
     */
    public List<Date> parse(String text) {
        return parse(text, new Date());
    }

    public List<Date> parse(String text, Date currentDate) {
        text = words2numbers(text);

        List<Date> result = new ArrayList<>();
        List<DateGroup> groups = parser.parse(text, currentDate);
        for (DateGroup group : groups) {
            result.addAll(group.getDates());
        }
        return result;
    }

    private String words2numbers(String language) {
        for (Map.Entry<String, String> entry : translations.entrySet()) {
            language = language.replaceAll("\\b" + entry.getKey() + "\\b", entry.getValue());
        }
        return language;
    }
}
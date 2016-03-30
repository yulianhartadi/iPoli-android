package io.ipoli.android.quest.suggestions;

import org.ocpsoft.prettytime.shade.edu.emory.mathcs.backport.java.util.Arrays;
import org.ocpsoft.prettytime.shade.edu.emory.mathcs.backport.java.util.Collections;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/25/16.
 */
public enum SuggestionType {
    MAIN(6),
    DUE_DATE(3),
    DURATION(1),
    START_TIME(2),
    TIMES_PER_DAY(4),
    RECURRENT(5),
    RECURRENT_DAY_OF_WEEK(7),
    RECURRENT_DAY_OF_MONTH(8);

    public int parseOrder;
    public SuggestionType parent;

    SuggestionType(int parseOrder) {
        this.parseOrder = parseOrder;
    }

    public static List<SuggestionType> getOrdered() {
        List<SuggestionType> types = new ArrayList<>(Arrays.asList(values()));
        types.remove(MAIN);

        Collections.sort(types, new Comparator<SuggestionType>() {

            @Override
            public int compare(SuggestionType p1, SuggestionType p2) {
                return p1.parseOrder < p2.parseOrder ? -1 : 1;
            }
        });

        return types;
    }
}


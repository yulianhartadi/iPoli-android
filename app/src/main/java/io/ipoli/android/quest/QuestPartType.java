package io.ipoli.android.quest;

import org.ocpsoft.prettytime.shade.edu.emory.mathcs.backport.java.util.Arrays;
import org.ocpsoft.prettytime.shade.edu.emory.mathcs.backport.java.util.Collections;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Polina Zhelyazkova <poly_vjk@abv.bg>
 * on 3/25/16.
 */
public enum QuestPartType {
    MAIN("", 6),
    DUE_DATE("on", 3),
    DURATION("for", 1),
    START_TIME("at", 2),
    RECURRENT("every", 5),
    TIMES_PER_DAY("times per day", 4);

    public String text;

    public int parseOrder;

    QuestPartType(String text, int parseOrder) {
        this.text = text;
        this.parseOrder = parseOrder;
    }

    public static QuestPartType get(String text) {
        for (QuestPartType s : QuestPartType.values()) {
            if (s.text.equals(text)) {
                return s;
            }
        }
        return null;
    }

    public static List<QuestPartType> getOrdered() {
        List<QuestPartType> types = new ArrayList<>(Arrays.asList(values()));
        types.remove(MAIN);

        Collections.sort(types, new Comparator<QuestPartType>() {

            @Override
            public int compare(QuestPartType p1, QuestPartType p2) {
                return p1.parseOrder < p2.parseOrder ? -1 : 1;
            }
        });

        return types;
    }
}


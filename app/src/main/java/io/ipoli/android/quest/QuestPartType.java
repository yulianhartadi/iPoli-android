package io.ipoli.android.quest;

/**
 * Created by Polina Zhelyazkova <poly_vjk@abv.bg>
 * on 3/25/16.
 */
public enum QuestPartType {
    MAIN(""),
    DUE_DATE("on"),
    DURATION("for");
//        , START_TIME, RECURRENT, TIMES_PER_DAY;

    public String text;

    QuestPartType(String text) {
        this.text = text;
    }

    public static QuestPartType get(String text) {
        for (QuestPartType s : QuestPartType.values()) {
            if (s.text.equals(text)) {
                return s;
            }
        }
        return null;
    }
}


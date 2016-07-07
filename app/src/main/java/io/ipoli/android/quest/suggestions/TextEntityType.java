package io.ipoli.android.quest.suggestions;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/25/16.
 *
 * The order of the values is important for quest text parsers
 */
public enum TextEntityType {
    MAIN,
    DUE_DATE,
    RECURRENT,
    RECURRENT_DAY_OF_WEEK,
    RECURRENT_DAY_OF_MONTH,
    DURATION,
    START_TIME,
    FLEXIBLE,
    TIMES_A_WEEK,
    TIMES_A_MONTH;
}


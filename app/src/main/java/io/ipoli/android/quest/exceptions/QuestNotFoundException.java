package io.ipoli.android.quest.exceptions;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 12/8/16.
 */
public class QuestNotFoundException extends Exception {
    public final String questId;

    public QuestNotFoundException(String questId) {
        this.questId = questId;
    }
}

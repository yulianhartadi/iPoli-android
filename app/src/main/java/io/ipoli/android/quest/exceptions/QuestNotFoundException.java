package io.ipoli.android.quest.exceptions;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 12/8/16.
 */
public class QuestNotFoundException extends Exception {

    public QuestNotFoundException(String questId) {
        super("Quest with id " + questId + " was not found");
    }
}

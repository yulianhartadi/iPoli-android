package io.ipoli.android.quest.exceptions;

import io.ipoli.android.app.App;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 12/8/16.
 */
public class QuestNotFoundException extends Exception {

    public QuestNotFoundException(String questId) {
        super("Quest with id " + questId + " of player with id " + App.getPlayerId() + " was not found");
    }
}

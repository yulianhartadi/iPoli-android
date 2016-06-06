package io.ipoli.android;

import org.junit.Test;

import io.ipoli.android.quest.data.RepeatingQuest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/6/16.
 */
public class QuestSchedulerTest {

    @Test
    public void createRepeatingQuest() {
        new RepeatingQuest("Hi");
    }
}

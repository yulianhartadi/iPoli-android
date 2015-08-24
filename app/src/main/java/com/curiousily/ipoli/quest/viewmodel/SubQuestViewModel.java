package com.curiousily.ipoli.quest.viewmodel;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/24/15.
 */
public class SubQuestViewModel {
    public String name;
    public boolean isComplete;

    public SubQuestViewModel(String name, boolean isComplete) {
        this.name = name;
        this.isComplete = isComplete;
    }
}

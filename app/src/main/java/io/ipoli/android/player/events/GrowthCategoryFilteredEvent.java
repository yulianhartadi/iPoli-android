package io.ipoli.android.player.events;

import io.ipoli.android.quest.data.Category;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/27/17.
 */
public class GrowthCategoryFilteredEvent {

    public final Category category;
    public final boolean isChecked;
    public final int interval;
    public final String chartType;

    public GrowthCategoryFilteredEvent(Category category, boolean isChecked, int interval, String chartType) {
        this.category = category;
        this.isChecked = isChecked;
        this.interval = interval;
        this.chartType = chartType;
    }
}

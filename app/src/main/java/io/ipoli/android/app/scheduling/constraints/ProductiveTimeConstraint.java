package io.ipoli.android.app.scheduling.constraints;

import io.ipoli.android.app.scheduling.Task;
import io.ipoli.android.quest.data.Category;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/23/17.
 */
public class ProductiveTimeConstraint extends SoftConstraint {

    public ProductiveTimeConstraint(int productiveTimeStartMinute, int productiveTimeEndMinute) {
        super(productiveTimeStartMinute, productiveTimeEndMinute);
    }

    @Override
    public boolean shouldApply(Task task) {
        Category category = task.getCategory();
        return category == Category.WORK || category == Category.LEARNING;
    }
}

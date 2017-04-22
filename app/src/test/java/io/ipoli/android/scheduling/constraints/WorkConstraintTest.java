package io.ipoli.android.scheduling.constraints;

import org.junit.Test;

import io.ipoli.android.app.scheduling.constraints.WorkConstraint;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.Quest;

import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertFalse;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/22/17.
 */

public class WorkConstraintTest {

    @Test
    public void shouldNotApplyToNonWorkQuest() {
        WorkConstraint constraint = new WorkConstraint();
        Quest q = new Quest("t1", Category.WELLNESS);
        assertFalse(constraint.shouldApply(q));
    }

    @Test
    public void shouldApplyToWorkQuest() {
        WorkConstraint constraint = new WorkConstraint();
        Quest q = new Quest("t1", Category.WORK);
        assertTrue(constraint.shouldApply(q));
    }
}

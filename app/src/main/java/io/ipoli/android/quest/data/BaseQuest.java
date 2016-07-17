package io.ipoli.android.quest.data;

import io.ipoli.android.quest.Category;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 7/14/16.
 */
public interface BaseQuest {

    String getId();
    String getName();
    Category getCategory();

}

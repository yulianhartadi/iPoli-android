package io.ipoli.android.app.ui;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/17/15.
 */
public interface ItemTouchHelperViewHolder {

    void onItemSelected();

    void onItemClear();

    void onItemSwipeStart();

    void onItemSwipeStopped();
}
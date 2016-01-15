package io.ipoli.android.app.ui;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/17/15.
 */
public interface ItemTouchHelperAdapter {

    void onItemMoved(int fromPosition, int toPosition);

    void onItemDismissed(int position, int direction);
}
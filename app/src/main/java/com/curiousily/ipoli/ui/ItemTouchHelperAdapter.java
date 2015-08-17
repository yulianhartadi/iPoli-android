package com.curiousily.ipoli.ui;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/17/15.
 */
public interface ItemTouchHelperAdapter {

    void onItemMove(int fromPosition, int toPosition);

    void onItemDismiss(int position);
}
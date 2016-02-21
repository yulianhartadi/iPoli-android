package io.ipoli.android.app.ui;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/17/15.
 */
public class ItemTouchCallback extends ItemTouchHelper.SimpleCallback {

    private static final int SWIPE_START_THRESHOLD = 2;
    public static final int SWIPE_DRAWABLE_OFFSET = 5;
    private final ItemTouchHelperAdapter adapter;
    private boolean isLongPressDragEnabled;
    private Drawable swipeStartDrawable;
    private Drawable swipeEndDrawable;

    public ItemTouchCallback(ItemTouchHelperAdapter adapter) {
        this(adapter, ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.START | ItemTouchHelper.END);
    }

    public ItemTouchCallback(ItemTouchHelperAdapter adapter, int swipeFlags) {
        this(adapter, ItemTouchHelper.UP | ItemTouchHelper.DOWN, swipeFlags);
    }

    public ItemTouchCallback(ItemTouchHelperAdapter adapter, int dragDirs, int swipeDirs) {
        super(dragDirs, swipeDirs);
        this.adapter = adapter;
        this.isLongPressDragEnabled = true;
    }

    public void setSwipeStartDrawable(Drawable swipeStartDrawable) {
        this.swipeStartDrawable = swipeStartDrawable;
    }

    public void setSwipeEndDrawable(Drawable swipeEndDrawable) {
        this.swipeEndDrawable = swipeEndDrawable;
    }

    public void setLongPressDragEnabled(boolean longPressDragEnabled) {
        isLongPressDragEnabled = longPressDragEnabled;
    }

    @Override
    public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        if (!(viewHolder instanceof ItemTouchHelperViewHolder)) {
            return 0;
        }
        ItemTouchHelperViewHolder holder = (ItemTouchHelperViewHolder) viewHolder;
        int flags = 0;
        if (holder.isStartSwipeEnabled()) {
            flags = ItemTouchHelper.START;
        }
        if (holder.isEndSwipeEnabled()) {
            flags |= ItemTouchHelper.END;
        }
        return flags;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return isLongPressDragEnabled;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder source, RecyclerView.ViewHolder target) {
        if (source.getItemViewType() != target.getItemViewType()) {
            return false;
        }

        adapter.onItemMoved(source.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        adapter.onItemDismissed(viewHolder.getAdapterPosition(), direction);
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (!(viewHolder instanceof ItemTouchHelperViewHolder)) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            return;
        }
        ItemTouchHelperViewHolder touchHelperViewHolder = (ItemTouchHelperViewHolder) viewHolder;

        if (!isCurrentlyActive && actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            int direction = dX > 0 ? ItemTouchHelper.END : ItemTouchHelper.START;
            touchHelperViewHolder.onItemSwipeStopped(direction);
        }

        if (isCurrentlyActive && actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            onSwipeStart(c, viewHolder, dX);
        }

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

    }

    private void onSwipeStart(Canvas c, RecyclerView.ViewHolder viewHolder, float dX) {
        View itemView = viewHolder.itemView;
        ItemTouchHelperViewHolder itemViewHolder =
                (ItemTouchHelperViewHolder) viewHolder;
        if (dX > SWIPE_START_THRESHOLD) {
            if (swipeEndDrawable != null) {
                swipeEndDrawable.setBounds(itemView.getLeft(), itemView.getTop(), (int) dX + SWIPE_DRAWABLE_OFFSET, itemView.getBottom());
                swipeEndDrawable.draw(c);
            }
            itemViewHolder.onItemSwipeStart(ItemTouchHelper.END);

        } else if (dX < -SWIPE_START_THRESHOLD) {
            if (swipeStartDrawable != null) {
                swipeStartDrawable.setBounds(viewHolder.itemView.getRight() + (int) dX - SWIPE_DRAWABLE_OFFSET, viewHolder.itemView.getTop(), viewHolder.itemView.getRight(), viewHolder.itemView.getBottom());
                swipeStartDrawable.draw(c);
            }
            itemViewHolder.onItemSwipeStart(ItemTouchHelper.START);
        }
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder,
                                  int actionState) {
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            if (viewHolder instanceof ItemTouchHelperViewHolder) {
                ItemTouchHelperViewHolder itemViewHolder =
                        (ItemTouchHelperViewHolder) viewHolder;
                itemViewHolder.onItemSelected();
            }
        }

        super.onSelectedChanged(viewHolder, actionState);
    }

    @Override
    public void clearView(RecyclerView recyclerView,
                          RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);

        if (viewHolder instanceof ItemTouchHelperViewHolder) {
            ItemTouchHelperViewHolder itemViewHolder =
                    (ItemTouchHelperViewHolder) viewHolder;
            itemViewHolder.onItemClear();
        }
    }
}
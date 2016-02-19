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
public class ItemTouchCallback extends ItemTouchHelper.Callback {

    private static final int SWIPE_START_THRESHOLD = 2;
    public static final int SWIPE_DRAWABLE_OFFSET = 5;
    private final ItemTouchHelperAdapter adapter;
    private final int swipeFlags;
    private final int dragFlags;
    private boolean isLongPressDragEnabled;
    private Drawable swipeStartDrawable;
    private Drawable swipeEndDrawable;

    public ItemTouchCallback(ItemTouchHelperAdapter adapter) {
        this(adapter, ItemTouchHelper.START | ItemTouchHelper.END, ItemTouchHelper.UP | ItemTouchHelper.DOWN);
    }

    public ItemTouchCallback(ItemTouchHelperAdapter adapter, int swipeFlags) {
        this(adapter, swipeFlags, ItemTouchHelper.UP | ItemTouchHelper.DOWN);
    }

    public ItemTouchCallback(ItemTouchHelperAdapter adapter, int swipeFlags, int dragFlags) {
        this.adapter = adapter;
        this.swipeFlags = swipeFlags;
        this.dragFlags = dragFlags;
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
    public boolean isLongPressDragEnabled() {
        return isLongPressDragEnabled;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        if (viewHolder instanceof ItemTouchHelperViewHolder) {
            return makeMovementFlags(dragFlags, swipeFlags);
        }
        return 0;
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
        if (!isCurrentlyActive && actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            if (viewHolder instanceof ItemTouchHelperViewHolder) {
                ItemTouchHelperViewHolder itemViewHolder =
                        (ItemTouchHelperViewHolder) viewHolder;
                int direction = dX > 0 ? ItemTouchHelper.END : ItemTouchHelper.START;
                itemViewHolder.onItemSwipeStopped(direction);
            }
        }


        if (isCurrentlyActive && actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            if (viewHolder instanceof ItemTouchHelperViewHolder) {
                onSwipeStart(c, viewHolder, dX);
            }
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
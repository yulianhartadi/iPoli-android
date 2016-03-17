package io.ipoli.android.app.ui.calendar;

import android.view.DragEvent;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/22/16.
 */
public interface DragStrategy {
    void onDragStarted(DragEvent event);

    void onDragEntered(DragEvent event);

    void onDragMoved(DragEvent event);

    void onDragDropped(DragEvent event);

    void onDragExited(DragEvent event);

    void onDragEnded();
}

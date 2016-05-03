package io.ipoli.android.app.ui.events;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/3/16.
 */
public class ToolbarCalendarTapEvent {
    public final boolean isExpanded;

    public ToolbarCalendarTapEvent(boolean isExpanded) {
        this.isExpanded = isExpanded;
    }
}

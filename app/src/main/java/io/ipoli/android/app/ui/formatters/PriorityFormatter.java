package io.ipoli.android.app.ui.formatters;

import android.content.Context;

import io.ipoli.android.R;
import io.ipoli.android.quest.data.Quest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/15/17.
 */

public class PriorityFormatter {

    public static String format(Context context, int priority) {
        switch (priority) {
            case Quest.PRIORITY_IMPORTANT_URGENT:
                return context.getString(R.string.important_urgent_readable);
            case Quest.PRIORITY_IMPORTANT_NOT_URGENT:
                return context.getString(R.string.important_not_urgent_readable);
            case Quest.PRIORITY_NOT_IMPORTANT_URGENT:
                return context.getString(R.string.not_important_urgent_readable);
            default:
                return context.getString(R.string.not_important_not_urgent_readable);
        }
    }
}

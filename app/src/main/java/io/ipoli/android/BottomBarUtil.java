package io.ipoli.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.BottomBarTab;
import com.roughike.bottombar.OnTabSelectedListener;

import io.ipoli.android.app.CalendarDayActivity;
import io.ipoli.android.quest.activities.AddQuestActivity;
import io.ipoli.android.quest.activities.HabitsActivity;
import io.ipoli.android.quest.activities.InboxActivity;
import io.ipoli.android.quest.activities.OverviewActivity;

/**
 * Created by Polina Zhelyazkova <poly_vjk@abv.bg>
 * on 3/22/16.
 */
public class BottomBarUtil {
    public static final int CALENDAR_TAB_INDEX = 0;
    public static final int OVERVIEW_TAB_INDEX = 1;
    public static final int ADD_QUEST_TAB_INDEX = 2;
    public static final int INBOX_TAB_INDEX = 3;
    public static final int HABITS_TAB_INDEX = 4;

    public static BottomBar getBottomBar(final AppCompatActivity activity, Bundle savedInstanceState, final int selectedPosition) {
        BottomBar bottomBar = BottomBar.attach(activity, savedInstanceState);
        bottomBar.useOnlyStatusBarTopOffset();

        bottomBar.setItems(
                new BottomBarTab(R.drawable.ic_event_white_24dp, "Calendar"),
                new BottomBarTab(R.drawable.ic_assignment_white_24dp, "Overview"),
                new BottomBarTab(R.drawable.ic_add_white_24dp, "Add Quest"),
                new BottomBarTab(R.drawable.ic_storage_white_24dp, "Inbox"),
                new BottomBarTab(R.drawable.ic_favorite_white_24dp, "Habits")
        );

        bottomBar.setOnItemSelectedListener(new OnTabSelectedListener() {
            @Override
            public void onItemSelected(int position) {
                if (position == selectedPosition) {
                    return;
                }

                switch (position) {
                    case CALENDAR_TAB_INDEX:
                        startActivity(activity, CalendarDayActivity.class);
                        break;
                    case OVERVIEW_TAB_INDEX:
                        startActivity(activity, OverviewActivity.class);
                        break;
                    case ADD_QUEST_TAB_INDEX:
                        Bundle b = new Bundle();
                        if (activity instanceof CalendarDayActivity) {
                            b.putBoolean(Constants.IS_TODAY_QUEST_EXTRA_KEY, true);
                        }
                        startActivity(activity, AddQuestActivity.class, b);
                        break;
                    case INBOX_TAB_INDEX:
                        startActivity(activity, InboxActivity.class);
                        break;
                    case HABITS_TAB_INDEX:
                        startActivity(activity, HabitsActivity.class);
                        break;
                }
            }
        });

        bottomBar.selectTabAtPosition(selectedPosition, true);

        bottomBar.mapColorForTab(CALENDAR_TAB_INDEX, ContextCompat.getColor(activity, R.color.colorPrimary));
        bottomBar.mapColorForTab(OVERVIEW_TAB_INDEX, ContextCompat.getColor(activity, R.color.colorPrimary));
        bottomBar.mapColorForTab(ADD_QUEST_TAB_INDEX, ContextCompat.getColor(activity, R.color.colorPrimary));
        bottomBar.mapColorForTab(INBOX_TAB_INDEX, ContextCompat.getColor(activity, R.color.colorPrimary));
        bottomBar.mapColorForTab(HABITS_TAB_INDEX, ContextCompat.getColor(activity, R.color.colorPrimary));


        return bottomBar;
    }

    private static void startActivity(AppCompatActivity activity, Class<?> newActivityClass) {
        startActivity(activity, newActivityClass, null);
    }

    private static void startActivity(AppCompatActivity activity, Class<?> newActivityClass, Bundle params) {
        Intent i = new Intent(activity, newActivityClass);
        if (params != null) {
            i.putExtras(params);
        }
        activity.startActivity(i);
        activity.overridePendingTransition(0, 0);
        activity.finish();
    }

}

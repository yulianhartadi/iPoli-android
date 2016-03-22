package io.ipoli.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnMenuTabSelectedListener;

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

        bottomBar.setItemsFromMenu(R.menu.bottom_bar_menu, new OnMenuTabSelectedListener() {
            @Override
            public void onMenuItemSelected(int menuItemId) {
                switch (menuItemId) {
                    case R.id.bottom_bar_calendar:
                        if (selectedPosition != CALENDAR_TAB_INDEX) {
                            startActivity(activity, CalendarDayActivity.class);
                        }
                        break;
                    case R.id.bottom_bar_overview:
                        if (selectedPosition != OVERVIEW_TAB_INDEX) {
                            startActivity(activity, OverviewActivity.class);
                        }
                        break;
                    case R.id.bottom_bar_add_quest:
                        if (selectedPosition != ADD_QUEST_TAB_INDEX) {
                            Bundle b = new Bundle();
                            if (activity instanceof CalendarDayActivity) {
                                b.putBoolean(Constants.IS_TODAY_QUEST_EXTRA_KEY, true);
                            }
                            startActivity(activity, AddQuestActivity.class, b);
                        }
                        break;
                    case R.id.bottom_bar_inbox:
                        if (selectedPosition != INBOX_TAB_INDEX) {
                            startActivity(activity, InboxActivity.class);
                        }
                        break;
                    case R.id.bottom_bar_habits:
                        if (selectedPosition != HABITS_TAB_INDEX) {
                            startActivity(activity, HabitsActivity.class);
                        }
                        break;
                }

            }
        });

        bottomBar.selectTabAtPosition(selectedPosition, false);

        bottomBar.mapColorForTab(CALENDAR_TAB_INDEX, ContextCompat.getColor(activity, R.color.colorAccent));
        bottomBar.mapColorForTab(OVERVIEW_TAB_INDEX, 0xFF5D4037);
        bottomBar.mapColorForTab(ADD_QUEST_TAB_INDEX, "#7B1FA2");
        bottomBar.mapColorForTab(INBOX_TAB_INDEX, "#FF5252");
        bottomBar.mapColorForTab(HABITS_TAB_INDEX, "#FF9800");


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

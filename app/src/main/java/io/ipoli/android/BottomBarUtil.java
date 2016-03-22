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

    public static BottomBar getBottomBar(final AppCompatActivity activity, Bundle savedInstanceState, final int selectedPosition) {
        BottomBar bottomBar = BottomBar.attach(activity, savedInstanceState);
        bottomBar.useOnlyStatusBarTopOffset();
        bottomBar.setItemsFromMenu(R.menu.bottom_bar_menu, new OnMenuTabSelectedListener() {
            @Override
            public void onMenuItemSelected(int menuItemId) {
                switch (menuItemId) {
                    case R.id.bottom_bar_calendar:
                        if (selectedPosition != 0) {
                            activity.startActivity(new Intent(activity, CalendarDayActivity.class));
                            activity.overridePendingTransition(0, 0);
                        }
                        break;
                    case R.id.bottom_bar_overview:
                        if (selectedPosition != 1) {
                            activity.startActivity(new Intent(activity, OverviewActivity.class));
                            activity.overridePendingTransition(0, 0);
                        }
                        break;
                    case R.id.bottom_bar_add_quest:
                        if (selectedPosition != 2) {
                            Intent i = new Intent(activity, AddQuestActivity.class);
                            if (activity instanceof CalendarDayActivity) {
                                i.putExtra(Constants.IS_TODAY_QUEST_EXTRA_KEY, true);
                            }
                            activity.startActivity(i);
                            activity.overridePendingTransition(0, 0);
                        }
                        break;
                    case R.id.bottom_bar_inbox:
                        if (selectedPosition != 3) {
                            activity.startActivity(new Intent(activity, InboxActivity.class));
                            activity.overridePendingTransition(0, 0);
                        }
                        break;
                    case R.id.bottom_bar_habits:
                        if (selectedPosition != 4) {
                            activity.startActivity(new Intent(activity, HabitsActivity.class));
                            activity.overridePendingTransition(0, 0);
                        }
                        break;
                }

            }
        });

        bottomBar.selectTabAtPosition(selectedPosition, false);

        bottomBar.mapColorForTab(0, ContextCompat.getColor(activity, R.color.colorAccent));
        bottomBar.mapColorForTab(1, 0xFF5D4037);
        bottomBar.mapColorForTab(2, "#7B1FA2");
        bottomBar.mapColorForTab(3, "#FF5252");
        bottomBar.mapColorForTab(4, "#FF9800");


        return bottomBar;
    }
}

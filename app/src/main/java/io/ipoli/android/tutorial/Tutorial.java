package io.ipoli.android.tutorial;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.StringRes;
import android.view.View;

import com.squareup.otto.Bus;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import co.mobiwise.materialintro.MaterialIntroConfiguration;
import co.mobiwise.materialintro.animation.MaterialIntroListener;
import co.mobiwise.materialintro.view.MaterialIntroView;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.tutorial.events.ShowTutorialItemEvent;

/**
 * Created by Polina Zhelyazkova <poly_vjk@abv.bg>
 * on 3/9/16.
 */
public class Tutorial {
    private static final String KEY_TUTORIAL_COMPLETED_STEP = "tutorial_completed_step";

    @Inject
    Bus eventBus;

    private static Context context;
    private final SharedPreferences prefs;
    private static Tutorial instance;
    private boolean isTutorialVisible = false;
    private int lastCompletedStep;
    private MaterialIntroConfiguration config;

    public static Tutorial getInstance(Context context) {
        Tutorial.context = context;
        if (instance == null) {
            instance = new Tutorial(context);
        }

        return instance;
    }

    private Tutorial(Context context) {
        App.getAppComponent(context).inject(this);
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        lastCompletedStep = prefs.getInt(KEY_TUTORIAL_COMPLETED_STEP, 0);
    }

    public enum State {
        TUTORIAL_WELCOME(R.string.tutorial_welcome, 0),
        TUTORIAL_CALENDAR_DRAG_QUEST(R.string.tutorial_calendar_quest, 0),
        TUTORIAL_CALENDAR_COMPLETE_QUEST(R.string.tutorial_calendar_complete_quest, 0),
        TUTORIAL_CALENDAR_UNSCHEDULE_QUESTS(R.string.tutorial_calendar_unscheduled_quests, 4000),
        TUTORIAL_START_OVERVIEW(R.string.tutorial_start_overview, 2500),
        TUTORIAL_OVERVIEW_SWIPE(R.string.tutorial_overview_swipe, 0),
        TUTORIAL_START_ADD_QUEST(R.string.tutorial_start_add_quest, 2500),
        TUTORIAL_ADD_QUEST(R.string.tutorial_add_quest, 0),
        TUTORIAL_START_INBOX(R.string.tutorial_start_inbox, 0),
        TUTORIAL_INBOX_SWIPE(R.string.tutorial_inbox_swipe, 0),
        TUTORIAL_VIEW_FEEDBACK(R.string.tutorial_view_feedback, 0);

        @StringRes
        private int textRes;

        private int delayMillis;

        State(int textRes, int delayMillis) {
            this.textRes = textRes;
            this.delayMillis = delayMillis;
        }

        private int getStep() {
            for (int i = 0; i < values().length; i++) {
                if (this == values()[i]) {
                    return i + 1;
                }
            }
            return -1;
        }

    }

    private Map<State, TutorialItem> stateToTutorialItem = new HashMap<>();

    public void addItem(TutorialItem item) {
        if (item.getState().getStep() < lastCompletedStep) {
            return;
        }
        stateToTutorialItem.put(item.getState(), item);
        if (!isTutorialVisible) {
            show();
        }
    }

    private State getNextState() {
        for (State s : stateToTutorialItem.keySet()) {
            if (s.getStep() == lastCompletedStep + 1) {
                return s;
            }
        }

        return null;
    }

    public void show() {
        State s = getNextState();
        if (s == null) {
            return;
        }
        TutorialItem item = stateToTutorialItem.get(s);
        if(item.getTarget() == null) {
            stateToTutorialItem.remove(s);
            lastCompletedStep += 1;
            show();
            return;
        }

        if(item.getTarget().getVisibility() != View.VISIBLE) {
            return;
        }
        eventBus.post(new ShowTutorialItemEvent(item.getState().name()));
        build(stateToTutorialItem.remove(s)).show();
        isTutorialVisible = true;
    }

    private MaterialIntroView.Builder build(final TutorialItem item) {
        return item.getMivBuilder()
                .setInfoText(context.getResources().getString(item.getState().textRes))
                .setUsageId(item.getState().name()) //THIS SHOULD BE UNIQUE ID
                .setDelayMillis(item.getState().delayMillis)
                .setListener(new MaterialIntroListener() {
                    @Override
                    public void onUserClicked(String s) {
                        isTutorialVisible = false;
                        lastCompletedStep = item.getState().getStep();
                        saveCompletedStep();
                        show();
                    }
                });
    }

    private void saveCompletedStep() {
        SharedPreferences.Editor e = prefs.edit();
        e.putInt(KEY_TUTORIAL_COMPLETED_STEP, lastCompletedStep);
        e.apply();
    }

}

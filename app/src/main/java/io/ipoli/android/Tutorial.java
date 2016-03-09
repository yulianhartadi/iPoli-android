package io.ipoli.android;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.StringRes;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

import co.mobiwise.materialintro.MaterialIntroConfiguration;
import co.mobiwise.materialintro.animation.MaterialIntroListener;
import co.mobiwise.materialintro.shape.Focus;
import co.mobiwise.materialintro.shape.FocusGravity;
import co.mobiwise.materialintro.view.MaterialIntroView;

/**
 * Created by Polina Zhelyazkova <poly_vjk@abv.bg>
 * on 3/9/16.
 */
public class Tutorial {
    private static final String KEY_TUTORIAL_COMPLETED_STEP = "tutorial_completed_step";
    private final SharedPreferences prefs;
    private static Tutorial instance;
    private boolean isTutorialVisible = false;
    private int lastCompletedStep;
    private MaterialIntroConfiguration config;

    public static Tutorial getInstance(Context context) {
        if(instance == null) {
            instance = new Tutorial(context);
        }

        return instance;
    }

    private Tutorial(Context context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        lastCompletedStep = prefs.getInt(KEY_TUTORIAL_COMPLETED_STEP, 0);
        initConfig();
    }

    public enum State {
//        TUTORIAL_START,
        TUTORIAL_DRAG_CALENDAR_QUEST(1, R.string.invite_welcome_text),
//        TUTORIAL_COMPLETE_CALENDAR_QUEST,
//        TUTORIAL_SCHEDULE_CALENDAR_UNSCHEDULED_QUEST,
        TUTORIAL_VIEW_OVERVIEW(2, R.string.invite_welcome_text),
        //TUTORIAL_SWIPE_OVERVIEW_LEFT, TUTORIAL_SWIPE_OVERVIEW_RIGHT,
        //TUTORIAL_ADD_QUEST,
        //...
        TUTORIAL_VIEW_INBOX(3, R.string.invite_welcome_text);
        //...
        //NORMAL
        private int step;

        @StringRes
        private int textRes;

        State(int step, int textRes) {
            this.step = step;
            this.textRes = textRes;
        }
    }

    private Map<State, TutorialItem> stateToTutorialItem= new HashMap<>();


    private void initConfig() {
        config = new MaterialIntroConfiguration();
        config.setDelayMillis(1000);
        config.setFadeAnimationEnabled(true);
        config.setDotViewEnabled(true);
        config.setFocusGravity(FocusGravity.CENTER);
        config.setFocusType(Focus.MINIMUM);
        config.setDelayMillis(500);
        config.setFadeAnimationEnabled(true);
    }

    public void addItem(State state, Activity activity, View view) {
        addItem(state, activity, view, true, Focus.NORMAL, FocusGravity.CENTER);
    }

    public void addItem(State state, Activity activity, View view, boolean performClick) {
        addItem(state, activity, view, performClick, Focus.NORMAL, FocusGravity.CENTER);
    }

    public void addItem(State state, Activity activity, View view, Focus focusType) {
        addItem(state, activity, view, true, focusType, FocusGravity.CENTER);
    }

    public void addItem(State state, Activity activity, View view, FocusGravity focusGravity) {
        addItem(state, activity, view, true, Focus.NORMAL, focusGravity);
    }

    public void addItem(State state, Activity activity, View view, boolean performClick, Focus focusType, FocusGravity focusGravity) {
        if(state.step < lastCompletedStep) {
            return;
        }
        TutorialItem item = new TutorialItem(state, activity, view);
        item.performClick = performClick;
        item.focus = focusType;
        item.focusGravity = focusGravity;
        stateToTutorialItem.put(state, item);
        if(!isTutorialVisible) {
            show();
        }
    }

    private State getNextState() {
        for(State s : stateToTutorialItem.keySet()) {
            if(s.step == lastCompletedStep + 1) {
                return s;
            }
        }

        return null;
    }

    public void show() {
        State s = getNextState();
        if(s == null) {
            return;
        }
        build(stateToTutorialItem.remove(s)).show();
        isTutorialVisible = true;
    }

    private MaterialIntroView.Builder build(final TutorialItem item) {
        return new MaterialIntroView.Builder(item.activity)
                .setConfiguration(config)
                .performClick(item.performClick)
                .setInfoText(item.activity.getResources().getString(item.state.textRes))
                .setTarget(item.view)
                .setUsageId(item.state.name()) //THIS SHOULD BE UNIQUE ID
                .setFocusGravity(item.focusGravity)
                .setFocusType(item.focus)
                .setListener(new MaterialIntroListener() {
                    @Override
                    public void onUserClicked(String s) {
                        isTutorialVisible = false;
                        lastCompletedStep = item.state.step;
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

    public static class TutorialItem {
        private State state;
        private Activity activity;
        private View view;
        private boolean performClick = true;
        private Focus focus = Focus.NORMAL;
        private FocusGravity focusGravity = FocusGravity.CENTER;

        public TutorialItem(State state, Activity activity, View view) {
            this.state = state;
            this.activity = activity;
            this.view = view;
        }
    }
}

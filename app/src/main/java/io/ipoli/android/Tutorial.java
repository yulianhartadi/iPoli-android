package io.ipoli.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.StringRes;

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
    private static Context context;
    private final SharedPreferences prefs;
    private static Tutorial instance;
    private boolean isTutorialVisible = false;
    private int lastCompletedStep;
    private MaterialIntroConfiguration config;

    public static Tutorial getInstance(Context context) {
        Tutorial.context = context;
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
        TUTORIAL_DRAG_CALENDAR_QUEST(R.string.invite_welcome_text),
//        TUTORIAL_COMPLETE_CALENDAR_QUEST,
//        TUTORIAL_SCHEDULE_CALENDAR_UNSCHEDULED_QUEST,
        TUTORIAL_VIEW_OVERVIEW(R.string.invite_welcome_text),
        //TUTORIAL_SWIPE_OVERVIEW_LEFT, TUTORIAL_SWIPE_OVERVIEW_RIGHT,
        //TUTORIAL_ADD_QUEST,
        //...
        TUTORIAL_VIEW_INBOX(R.string.invite_welcome_text);
        //...
        //NORMAL

        @StringRes
        private int textRes;

        State(int textRes) {
            this.textRes = textRes;
        }

        private int getStep() {
            for(int i=0; i< values().length; i++) {
                if(this == values()[i]) {
                    return i + 1;
                }
            }
            return -1;
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

    public void addItem(TutorialItem item) {
        if(item.getState().getStep() < lastCompletedStep) {
            return;
        }
        stateToTutorialItem.put(item.getState(), item);
        if(!isTutorialVisible) {
            show();
        }
    }

    private State getNextState() {
        for(State s : stateToTutorialItem.keySet()) {
            if(s.getStep() == lastCompletedStep + 1) {
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
        return item.getMivBuilder()
                .setInfoText(context.getResources().getString(item.getState().textRes))
                .setUsageId(item.getState().name()) //THIS SHOULD BE UNIQUE ID
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

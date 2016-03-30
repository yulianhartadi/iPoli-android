package io.ipoli.android.tutorial;

import android.app.Activity;
import android.view.View;

import co.mobiwise.materialintro.MaterialIntroConfiguration;
import co.mobiwise.materialintro.shape.Focus;
import co.mobiwise.materialintro.shape.FocusGravity;
import co.mobiwise.materialintro.view.MaterialIntroView;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/9/16.
 */
public class TutorialItem {
    private Tutorial.State state;
    private MaterialIntroView.Builder mivBuilder;
    private View target;

    public Tutorial.State getState() {
        return state;
    }

    public MaterialIntroView.Builder getMivBuilder() {
        return mivBuilder;
    }

    public View getTarget() {
        return target;
    }

    public static class Builder {

        private TutorialItem tutorialItem;
        private MaterialIntroView.Builder mivBuilder;

        public Builder(Activity activity) {
            mivBuilder = new MaterialIntroView.Builder(activity).setConfiguration(getConfig());
            mivBuilder.performClick(true);
            tutorialItem = new TutorialItem();
            tutorialItem.mivBuilder = mivBuilder;
        }

        private MaterialIntroConfiguration getConfig() {
            MaterialIntroConfiguration config = new MaterialIntroConfiguration();
            config.setDelayMillis(1000);
            config.setFadeAnimationEnabled(true);
            config.setDotViewEnabled(true);
            config.setFocusGravity(FocusGravity.CENTER);
            config.setFocusType(Focus.MINIMUM);
            config.setDelayMillis(500);
            config.setFadeAnimationEnabled(true);
            return config;
        }

        public TutorialItem.Builder setTarget(View view) {
            mivBuilder.setTarget(view);
            tutorialItem.target = view;
            return this;
        }


        public TutorialItem.Builder setFocusType(Focus focusType) {
            mivBuilder.setFocusType(focusType);
            return this;
        }

        public TutorialItem.Builder setFocusGravity(FocusGravity focusGravity) {
            mivBuilder.setFocusGravity(focusGravity);
            return this;
        }


        public TutorialItem.Builder setTargetPadding(int padding) {
            mivBuilder.setTargetPadding(padding);
            return this;
        }

        public TutorialItem.Builder dismissOnTouch(boolean dismissOnTouch) {
            mivBuilder.dismissOnTouch(dismissOnTouch);
            return this;
        }

        public TutorialItem.Builder enableDotAnimation(boolean isDotAnimationEnabled) {
            mivBuilder.enableDotAnimation(isDotAnimationEnabled);
            return this;
        }

        public TutorialItem.Builder setConfiguration(MaterialIntroConfiguration configuration) {
            mivBuilder.setConfiguration(configuration);
            return this;
        }

        public TutorialItem.Builder performClick(boolean isPerformClick) {
            mivBuilder.performClick(isPerformClick);
            return this;
        }

        public TutorialItem.Builder setDelayMillis(int delayMillis) {
            mivBuilder.setDelayMillis(delayMillis);
            return this;
        }

        public TutorialItem.Builder setState(Tutorial.State state) {
            tutorialItem.state = state;
            return this;
        }

        public TutorialItem build() {
            return tutorialItem;
        }
    }
}

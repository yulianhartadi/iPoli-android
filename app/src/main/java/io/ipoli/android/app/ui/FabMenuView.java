package io.ipoli.android.app.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.activities.QuickAddActivity;
import io.ipoli.android.app.utils.ViewUtils;
import io.ipoli.android.challenge.activities.AddChallengeActivity;
import io.ipoli.android.quest.activities.AddQuestActivity;
import io.ipoli.android.quest.activities.AddRepeatingQuestActivity;
import io.ipoli.android.reward.activities.EditRewardActivity;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 6/27/16.
 */
public class FabMenuView extends RelativeLayout {
    private enum FabName {
        OPEN, QUEST, REPEATING_QUEST, CHALLENGE, REWARD, QUICK_ADD
    }

    private Unbinder unbinder;

    @BindView(R.id.fab_menu_container)
    ViewGroup container;

    @BindView(R.id.fab_add_quest)
    FloatingActionButton quest;

    @BindView(R.id.fab_add_repeating_quest)
    FloatingActionButton repeatingQuest;

    @BindView(R.id.fab_add_challenge)
    FloatingActionButton challenge;

    @BindView(R.id.fab_add_reward)
    FloatingActionButton reward;

    @BindView(R.id.fab_quick_add_quest)
    FloatingActionButton quickAddQuest;

    @BindView(R.id.fab_quest_label)
    TextView questLabel;

    @BindView(R.id.fab_repeating_quest_label)
    TextView repeatingQuestLabel;

    @BindView(R.id.fab_challenge_label)
    TextView challengeLabel;

    @BindView(R.id.fab_reward_label)
    TextView rewardLabel;

    @BindView(R.id.fab_quick_add_label)
    TextView quickAddLabel;

    private Animation fabOpenAnimation;
    private Animation fabCloseAnimation;
    private Animation rotateForwardAnimation;
    private Animation rotateBackwardAnimation;
    private boolean isOpen = false;

    private boolean onlyQuests = false;

    private List<FabClickListener> fabClickListeners;

    public FabMenuView(Context context) {
        super(context);
        if (!isInEditMode()) {
            initUI(context);
        }
    }

    public FabMenuView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            TypedArray typedArray = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.FabMenuView,
                    0, 0);

            try {
                onlyQuests = typedArray.getBoolean(R.styleable.FabMenuView_onlyQuests, false);
            } finally {
                typedArray.recycle();
            }

            if (!isInEditMode()) {
                initUI(context);
            }
        }
    }

    private void initUI(Context context) {
        View view = LayoutInflater.from(context).inflate(
                R.layout.layout_fab_menu, this);
        unbinder = ButterKnife.bind(this, view);
        fabClickListeners = new ArrayList<>();

        setElevation(ViewUtils.dpToPx(5, getResources()));

        fabOpenAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.fab_open);
        fabCloseAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.fab_close);
        rotateForwardAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_forward);
        rotateBackwardAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_backward);

        if(onlyQuests) {
            challenge.setVisibility(GONE);
            challengeLabel.setVisibility(GONE);
            reward.setVisibility(GONE);
            rewardLabel.setVisibility(GONE);
        }

    }

    @OnClick({R.id.fab_add_quest, R.id.fab_quest_label})
    public void onAddQuestClick(View view) {
        Intent intent = new Intent(getContext(), AddQuestActivity.class);
        onFabClicked(intent, FabName.QUEST);
    }

    @OnClick({R.id.fab_add_repeating_quest, R.id.fab_repeating_quest_label})
    public void onAddRepeatingQuestClick(View view) {
        Intent intent = new Intent(getContext(), AddRepeatingQuestActivity.class);
        onFabClicked(intent, FabName.REPEATING_QUEST);
    }

    @OnClick({R.id.fab_add_challenge, R.id.fab_challenge_label})
    public void onAddChallengeClick(View view) {
        onFabClicked(new Intent(getContext(), AddChallengeActivity.class), FabName.CHALLENGE);
    }

    @OnClick({R.id.fab_add_reward, R.id.fab_reward_label})
    public void onAddRewardClick(View view) {
        onFabClicked(new Intent(getContext(), EditRewardActivity.class), FabName.REWARD);
    }

    @OnClick({R.id.fab_quick_add_quest, R.id.fab_quick_add_label})
    public void onQuickAddQuestClick(View view) {
        Intent intent = new Intent(getContext(), QuickAddActivity.class);
        intent.putExtra(Constants.QUICK_ADD_ADDITIONAL_TEXT, " " + getContext().getString(R.string.today).toLowerCase());
        onFabClicked(intent, FabName.QUICK_ADD);
    }

    private void onFabClicked(Intent intent, FabName fabName) {
        if (isOpen) {
            callListeners(fabName);
            getContext().startActivity(intent);
            close();
        } else {
            open();
            callListeners(FabName.OPEN);
        }
    }

    private void callListeners(FabName fabName) {
        for (FabClickListener listener : fabClickListeners) {
            listener.onFabClicked(fabName.name().toLowerCase());
        }
    }

    public void open() {
        isOpen = true;
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        container.setClickable(true);
        for (int i = 0; i < container.getChildCount(); i++) {
            container.getChildAt(i).setClickable(true);
        }
        questLabel.setClickable(true);
        container.setVisibility(VISIBLE);
        container.setAlpha(1);
        playOpenAnimation();
    }

    private void close() {
        isOpen = false;
        container.setClickable(false);
        for (int i = 0; i < container.getChildCount(); i++) {
            container.getChildAt(i).setClickable(false);
        }
        questLabel.setClickable(false);
        playCloseAnimation();
    }

    private void playOpenAnimation() {
        rotateForwardAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                quest.setImageResource(R.drawable.ic_done_white_24dp);
                quest.setRotation(-45);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        quest.startAnimation(rotateForwardAnimation);
        questLabel.startAnimation(fabOpenAnimation);
        repeatingQuest.startAnimation(fabOpenAnimation);
        repeatingQuestLabel.startAnimation(fabOpenAnimation);
        if(!onlyQuests) {
            challenge.startAnimation(fabOpenAnimation);
            challengeLabel.startAnimation(fabOpenAnimation);
            reward.startAnimation(fabOpenAnimation);
            rewardLabel.startAnimation(fabOpenAnimation);
        }
        quickAddQuest.startAnimation(fabOpenAnimation);
        quickAddLabel.startAnimation(fabOpenAnimation);
    }


    private void playCloseAnimation() {
        rotateBackwardAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                quest.setImageResource(R.drawable.ic_add_white_24dp);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        fabCloseAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                container.animate().alpha(0).setDuration(getResources().getInteger(
                        android.R.integer.config_shortAnimTime)).start();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        quest.startAnimation(rotateBackwardAnimation);
        quickAddLabel.startAnimation(fabCloseAnimation);
        quickAddQuest.startAnimation(fabCloseAnimation);
        if(!onlyQuests) {
            rewardLabel.startAnimation(fabCloseAnimation);
            reward.startAnimation(fabCloseAnimation);
            challengeLabel.startAnimation(fabCloseAnimation);
            challenge.startAnimation(fabCloseAnimation);
        }
        repeatingQuestLabel.startAnimation(fabCloseAnimation);
        repeatingQuest.startAnimation(fabCloseAnimation);
        questLabel.startAnimation(fabCloseAnimation);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && isOpen) {
            close();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @OnClick(R.id.fab_menu_container)
    public void onContainerClick(View v) {
        close();
    }

    @Override
    protected void onDetachedFromWindow() {
        unbinder.unbind();
        super.onDetachedFromWindow();
    }

    public void addFabClickListener(FabClickListener listener) {
        fabClickListeners.add(listener);
    }

    public void removeFabClickListener(FabClickListener listener) {
        fabClickListeners.remove(listener);
    }

    public interface FabClickListener {
        void onFabClicked(String name);
    }
}

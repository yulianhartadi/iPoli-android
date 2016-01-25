package io.ipoli.android.app.ui;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.assistant.Assistant;
import io.ipoli.android.assistant.events.RenameAssistantEvent;
import io.ipoli.android.assistant.persistence.AssistantPersistenceService;
import io.ipoli.android.player.LevelExperienceGenerator;
import io.ipoli.android.player.Player;
import io.ipoli.android.player.events.PlayerXPIncreasedEvent;
import io.ipoli.android.player.events.PlayerLevelUpEvent;
import io.ipoli.android.player.persistence.PlayerPersistenceService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/11/16.
 */
public class PlayerBarLayout extends AppBarLayout {

    public static final int INITIAL_EXPERIENCE_ANIMATION_DELAY = 500;

    @Inject
    Bus eventBus;

    @Inject
    PlayerPersistenceService playerPersistenceService;

    @Inject
    AssistantPersistenceService assistantPersistenceService;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.experience_bar)
    ProgressBar experienceBar;

    @Bind(R.id.player_level)
    TextView playerLevel;

    private Player player;

    public PlayerBarLayout(Context context) {
        super(context);
        if (!isInEditMode()) {
            init(context, null);
        }
    }

    public PlayerBarLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            init(context, attrs);
        }
    }

    private void init(Context context, AttributeSet attrs) {
        App.getAppComponent(context).inject(this);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.player_bar, this, true);
        ButterKnife.bind(this, view);

        Assistant assistant = assistantPersistenceService.find();

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.PlayerBarLayout,
                0, 0);

        String title;
        boolean showLevel;
        try {
            showLevel = a.getBoolean(R.styleable.PlayerBarLayout_player_bar_show_level, true);
            title = a.getString(R.styleable.PlayerBarLayout_player_bar_title);
        } finally {
            a.recycle();
        }


        if (TextUtils.isEmpty(title)) {
            title = assistant.getName();
        }

        if (!showLevel) {
            playerLevel.setVisibility(View.GONE);
        }

        toolbar.setTitle(title);

        player = playerPersistenceService.find();
        experienceBar.setMax(LevelExperienceGenerator.experienceForLevel(player.getLevel()));
        playerLevel.setText(context.getString(R.string.player_level, player.getLevel()));
        animateExperienceProgress(0, player.getExperience(), INITIAL_EXPERIENCE_ANIMATION_DELAY);
    }

    private void animateLevelUp() {

        float originalTextSize = playerLevel.getTextSize();

        float scaledDensity = getResources().getDisplayMetrics().scaledDensity;
        float originalTextSizeSP = originalTextSize / scaledDensity;

        ValueAnimator forwardAnimation = ObjectAnimator.ofFloat(playerLevel, "textSize", originalTextSizeSP, originalTextSizeSP * 1.5f);
        forwardAnimation.setDuration(getResources().getInteger(android.R.integer.config_longAnimTime));
        forwardAnimation.setInterpolator(new AccelerateInterpolator());

        ValueAnimator reverseAnimation = ObjectAnimator.ofFloat(playerLevel, "textSize", originalTextSizeSP * 1.5f, originalTextSizeSP);
        reverseAnimation.setDuration(getResources().getInteger(android.R.integer.config_longAnimTime));
        reverseAnimation.setInterpolator(new DecelerateInterpolator());
        reverseAnimation.setStartDelay(getResources().getInteger(android.R.integer.config_longAnimTime));

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(forwardAnimation, reverseAnimation);
        animatorSet.start();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        eventBus.register(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        eventBus.unregister(this);
        super.onDetachedFromWindow();
    }

    @Subscribe
    public void onRenameAssistant(RenameAssistantEvent e) {
        toolbar.setTitle(e.name);
    }

    @Subscribe
    public void onPlayerLevelUp(PlayerLevelUpEvent e) {
        experienceBar.setMax(e.maxXPForLevel);
        experienceBar.setProgress(e.newLevelXP);
        playerLevel.setText(getContext().getString(R.string.player_level, player.getLevel()));
        animateLevelUp();
    }

    @Subscribe
    public void onPlayerXPIncreased(PlayerXPIncreasedEvent e) {
        animateExperienceProgress(e.currentXP, e.newXP, 0);
    }

    private void animateExperienceProgress(int currentXP, int newXP, long startDelay) {
        ObjectAnimator progressAnimator = ObjectAnimator.ofInt(experienceBar, "progress", currentXP, newXP);
        progressAnimator.setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));
        progressAnimator.setStartDelay(startDelay);
        progressAnimator.setInterpolator(new DecelerateInterpolator());
        progressAnimator.start();
    }
}

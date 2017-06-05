package io.ipoli.android.app.tutorial.fragments;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.internal.NavigationMenuView;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.format.DateFormat;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import co.mobiwise.materialintro.prefs.PreferencesManager;
import co.mobiwise.materialintro.shape.Focus;
import co.mobiwise.materialintro.shape.FocusGravity;
import co.mobiwise.materialintro.shape.ShapeType;
import co.mobiwise.materialintro.view.MaterialIntroView;
import de.hdodenhof.circleimageview.CircleImageView;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.tutorial.OnboardingActivity;
import io.ipoli.android.app.ui.calendar.CalendarDayView;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.pet.data.Pet;
import io.ipoli.android.quest.data.Category;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/4/17.
 */
public class TutorialCalendarFragment extends Fragment {

    private static final HashMap<Category, Integer> QUEST_CATEGORY_TO_CHECKBOX_STYLE = new HashMap<Category, Integer>() {{
        put(Category.LEARNING, R.style.LearningCheckbox);
        put(Category.WELLNESS, R.style.WellnessCheckbox);
        put(Category.PERSONAL, R.style.PersonalCheckbox);
        put(Category.WORK, R.style.WorkCheckbox);
        put(Category.FUN, R.style.FunCheckbox);
        put(Category.CHORES, R.style.ChoresCheckbox);
    }};


    @BindView(R.id.tutorial_calendar)
    CalendarDayView calendarDayView;

    @BindView(R.id.quest_details_container)
    ViewGroup detailsContainer;

    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    @BindView(R.id.navigation_view)
    NavigationView navigationView;

    @BindView(R.id.quest_name)
    TextView questName;

    @BindView(R.id.quest_category_indicator)
    View questCategoryIndicator;

    @BindView(R.id.quest_background)
    View questBackground;

    private Unbinder unbinder;

    private PreferencesManager preferencesManager;

    private String name;
    private Category category;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tutorial_calendar, container, false);
        unbinder = ButterKnife.bind(this, v);
        calendarDayView.hideTimeLine();
        calendarDayView.smoothScrollToTime(Time.atHours(13));
        boolean use24HourFormat = DateFormat.is24HourFormat(getContext());
        calendarDayView.setTimeFormat(use24HourFormat);
        CheckBox checkBox = createCheckBox(category, getContext());
        detailsContainer.addView(checkBox, 0);

        preferencesManager = new PreferencesManager(getContext());


        questName.setText(name);
        questCategoryIndicator.setBackgroundResource(category.color500);
        questBackground.setBackgroundResource(category.color500);

        new MaterialIntroView.Builder(getActivity())
                .enableIcon(false)
                .setFocusGravity(FocusGravity.CENTER)
                .setFocusType(Focus.NORMAL)
                .setDelayMillis(500)
                .enableFadeAnimation(true)
                .performClick(true)
                .setInfoText("Your quest has been added to your calendar. Complete it by tapping the checkbox.")
                .setShape(ShapeType.RECTANGLE)
                .setTargetPadding(20)
                .setTarget(v.findViewById(R.id.tutorial_quest_container))
                .setListener(s -> {
                    preferencesManager.resetAll();
                    onQuestComplete(v, checkBox);
                }).show();

        return v;
    }

    private void onQuestComplete(View v, CheckBox checkBox) {
        checkBox.setChecked(true);
        Snackbar snackBar = Snackbar.make(v, getString(R.string.quest_complete_with_bounty, 10, 10), Snackbar.LENGTH_INDEFINITE);
        snackBar.show();

        snackBar.getView().postDelayed(() ->
                        new MaterialIntroView.Builder(getActivity())
                                .enableIcon(false)
                                .setFocusGravity(FocusGravity.CENTER)
                                .setFocusType(Focus.NORMAL)
                                .enableFadeAnimation(true)
                                .setTargetPadding(5)
                                .performClick(true)
                                .setInfoText("Every time you complete a quest you get a reward! Experience allows you to level up and with life coins you can unlock upgrades, buy new avatars ot pets.")
                                .setShape(ShapeType.RECTANGLE)
                                .setTarget(snackBar.getView())
                                .setListener(s -> {
                                    preferencesManager.resetAll();
                                    snackBar.dismiss();
                                    onRewardTaken();
                                })
                                .show()
                , 500);
    }

    private void onRewardTaken() {
        drawerLayout.openDrawer(Gravity.START);
        View headerView = navigationView.getHeaderView(0);

        TextView level = (TextView) headerView.findViewById(R.id.player_level);
        int playerLevel = Constants.DEFAULT_PLAYER_LEVEL;
        String[] playerTitles = getResources().getStringArray(R.array.player_titles);
        String title = playerTitles[Math.min(playerLevel / 10, playerTitles.length - 1)];
        level.setText(String.format(getString(R.string.player_level), playerLevel, title));

        TextView coins = (TextView) headerView.findViewById(R.id.player_coins);
        coins.setText(String.valueOf(Constants.DEFAULT_PLAYER_COINS));

        TextView rewardPoints = (TextView) headerView.findViewById(R.id.player_reward_points);
        rewardPoints.setText(String.valueOf(Constants.DEFAULT_PLAYER_REWARD_POINTS));

        TextView xpPoints = (TextView) headerView.findViewById(R.id.player_current_xp);
        xpPoints.setText(getString(R.string.nav_drawer_player_xp, String.valueOf(Constants.DEFAULT_PLAYER_XP)));

        ProgressBar experienceBar = (ProgressBar) headerView.findViewById(R.id.player_experience);
        experienceBar.setMax(Constants.XP_BAR_MAX_VALUE);
        experienceBar.setProgress(Constants.DEFAULT_PLAYER_XP);

        CircleImageView avatarPictureView = (CircleImageView) headerView.findViewById(R.id.player_picture);
        avatarPictureView.setImageResource(Constants.DEFAULT_PLAYER_AVATAR.picture);

        CircleImageView petPictureView = (CircleImageView) headerView.findViewById(R.id.pet_picture);
        petPictureView.setImageResource(Constants.DEFAULT_PET_AVATAR.headPicture);

        ImageView petStateView = (ImageView) headerView.findViewById(R.id.pet_state);
        GradientDrawable drawable = (GradientDrawable) petStateView.getBackground();
        drawable.setColor(ContextCompat.getColor(getContext(), Pet.PetState.HAPPY.color));

        headerView.postDelayed(() -> {
            new MaterialIntroView.Builder(getActivity())
                    .enableIcon(false)
                    .setFocusGravity(FocusGravity.CENTER)
                    .setFocusType(Focus.NORMAL)
                    .enableFadeAnimation(true)
                    .setTargetPadding(5)
                    .performClick(true)
                    .setInfoText("You can follow your progress here. These are your avatar and pet. Keep your pet happy by completing your quests regularly to achieve maximum coins and XP bonus!")
                    .setShape(ShapeType.RECTANGLE)
                    .setTarget(headerView)
                    .setListener(s -> {
                        preferencesManager.resetAll();
                        onProgressReviewed();
                    })
                    .show();
        }, 500);
    }

    private void onProgressReviewed() {
        new MaterialIntroView.Builder(getActivity())
                .enableIcon(false)
                .setFocusGravity(FocusGravity.CENTER)
                .setFocusType(Focus.NORMAL)
                .enableFadeAnimation(true)
                .setTargetPadding(5)
                .performClick(true)
                .setInfoText("Use your hard earned coins to unlock powerful upgrades (Repeating quests, Calendar sync, Notes etc) or buy new avatars and cute pets!")
                .setShape(ShapeType.RECTANGLE)
                .setTarget(((NavigationMenuView) navigationView.getChildAt(0)).findViewHolderForAdapterPosition(9).itemView)
                .setListener(s -> {
                    preferencesManager.resetAll();
                    ((OnboardingActivity) getActivity()).onCalendarDone();
                })
                .show();
    }

    @NonNull
    private CheckBox createCheckBox(Category category, Context context) {
        CheckBox check = new CheckBox(new ContextThemeWrapper(context, QUEST_CATEGORY_TO_CHECKBOX_STYLE.get(category)));
        LinearLayout.LayoutParams checkLP = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        int marginEndDP = 16;
        int px = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                marginEndDP,
                context.getResources().getDisplayMetrics()
        );
        check.setId(R.id.quest_check);
        check.setScaleX(1.3f);
        check.setScaleY(1.3f);
        checkLP.setMarginEnd(px);
        checkLP.gravity = Gravity.CENTER_VERTICAL;
        check.setLayoutParams(checkLP);
        return check;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    public void setQuestInfo(String name, Category category) {
        this.name = name;
        this.category = category;
    }
}
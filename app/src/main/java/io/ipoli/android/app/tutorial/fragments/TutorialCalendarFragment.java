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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import com.squareup.otto.Bus;

import javax.inject.Inject;

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
import io.ipoli.android.app.App;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.events.ScreenShownEvent;
import io.ipoli.android.app.tutorial.TutorialActivity;
import io.ipoli.android.app.ui.calendar.CalendarDayView;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.pet.data.Pet;
import io.ipoli.android.quest.data.Category;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/4/17.
 */
public class TutorialCalendarFragment extends Fragment {

    @Inject
    Bus eventBus;

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
        App.getAppComponent(getContext()).inject(this);
        View v = inflater.inflate(R.layout.fragment_tutorial_calendar, container, false);
        unbinder = ButterKnife.bind(this, v);
        calendarDayView.hideTimeLine();
        calendarDayView.scrollTo(Time.atHours(13));
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
                .setInfoText(getString(R.string.tutorial_calendar_quest_hint))
                .setShape(ShapeType.RECTANGLE)
                .setTargetPadding(20)
                .setTarget(v.findViewById(R.id.tutorial_quest_container))
                .setListener(s -> {
                    preferencesManager.resetAll();
                    onQuestComplete(v, checkBox);
                }).show();
        eventBus.post(new ScreenShownEvent(getActivity(), EventSource.TUTORIAL_CALENDAR));
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
                                .dismissOnTouch(true)
                                .setInfoText(getString(R.string.tutorial_quest_complete))
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
                    .dismissOnTouch(true)
                    .setInfoText(getString(R.string.tutorial_follow_progress))
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
        NavigationMenuView navigationMenuView = (NavigationMenuView) navigationView.getChildAt(0);
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) navigationMenuView.getLayoutManager();
        linearLayoutManager.scrollToPositionWithOffset(6, 0);
        RecyclerView.ViewHolder viewHolder = navigationMenuView.findViewHolderForAdapterPosition(9);
        View targetView = viewHolder != null && viewHolder.itemView != null ? viewHolder.itemView : navigationMenuView;
        new MaterialIntroView.Builder(getActivity())
                .enableIcon(false)
                .setFocusGravity(FocusGravity.CENTER)
                .setFocusType(Focus.NORMAL)
                .enableFadeAnimation(true)
                .setTargetPadding(5)
                .dismissOnTouch(true)
                .setInfoText(getString(R.string.tutorial_store))
                .setShape(ShapeType.RECTANGLE)
                .setTarget(targetView)
                .setListener(s -> {
                    preferencesManager.resetAll();
                    onDone();
                })
                .show();
    }

    private void onDone() {
        if (getActivity() != null) {
            ((TutorialActivity) getActivity()).onCalendarDone();
        }
    }

    @NonNull
    private CheckBox createCheckBox(Category category, Context context) {
        CheckBox check = new CheckBox(new ContextThemeWrapper(context, getCheckBoxStyle(category)));
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

    private int getCheckBoxStyle(Category category) {
        switch (category) {
            case LEARNING:
                return R.style.LearningCheckbox;
            case WELLNESS:
                return R.style.WellnessCheckbox;
            case PERSONAL:
                return R.style.PersonalCheckbox;
            case WORK:
                return R.style.WorkCheckbox;
            case FUN:
                return R.style.FunCheckbox;
            default:
                return R.style.ChoresCheckbox;
        }
    }
}
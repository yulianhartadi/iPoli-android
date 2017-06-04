package io.ipoli.android.app.tutorial.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import co.mobiwise.materialintro.prefs.PreferencesManager;
import co.mobiwise.materialintro.shape.Focus;
import co.mobiwise.materialintro.shape.FocusGravity;
import co.mobiwise.materialintro.shape.ShapeType;
import co.mobiwise.materialintro.view.MaterialIntroView;
import io.ipoli.android.R;
import io.ipoli.android.app.ui.calendar.CalendarDayView;
import io.ipoli.android.app.utils.Time;
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

    private Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tutorial_calendar, container, false);
        unbinder = ButterKnife.bind(this, v);
        calendarDayView.hideTimeLine();
        calendarDayView.smoothScrollToTime(Time.atHours(13));
        boolean use24HourFormat = DateFormat.is24HourFormat(getContext());
        calendarDayView.setTimeFormat(use24HourFormat);

        CheckBox checkBox = createCheckBox(Category.WELLNESS, getContext());
        detailsContainer.addView(checkBox, 0);

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
                    PreferencesManager manager = new PreferencesManager(getContext());
                    manager.resetAll();
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
                        .setTarget(snackBar.getView()).show(), 500);
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
}

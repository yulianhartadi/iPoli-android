package io.ipoli.android.challenge.fragments;

import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.ocpsoft.prettytime.shade.net.fortuna.ical4j.model.Recur;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.BaseFragment;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.events.ScreenShownEvent;
import io.ipoli.android.app.help.HelpDialog;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.challenge.activities.ChallengeActivity;
import io.ipoli.android.challenge.data.Challenge;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.Recurrence;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.events.RepeatingQuestSavedEvent;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.persistence.RepeatingQuestPersistenceService;
import io.ipoli.android.quest.persistence.events.QuestSavedEvent;
import io.ipoli.android.quest.persistence.events.QuestsSavedEvent;
import io.ipoli.android.quest.persistence.events.RepeatingQuestsSavedEvent;
import io.ipoli.android.quest.ui.formatters.DateFormatter;
import io.ipoli.android.quest.ui.formatters.DurationFormatter;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/27/16.
 */
public class ChallengeOverviewFragment extends BaseFragment {

    private Unbinder unbinder;

    @Inject
    Bus eventBus;

    @BindView(R.id.challenge_progress_fraction)
    TextView progressFraction;

    @BindView(R.id.challenge_progress_percent)
    TextView progressPercent;

    @BindView(R.id.challenge_history)
    BarChart history;

    @BindView(R.id.challenge_progress)
    ProgressBar progress;

    @BindView(R.id.challenge_summary_stats_container)
    ViewGroup summaryStatsContainer;

    @BindView(R.id.challenge_category_image)
    ImageView categoryImage;

    @BindView(R.id.challenge_category_name)
    TextView categoryName;

    @BindView(R.id.challenge_next_scheduled_date)
    TextView nextScheduledDate;

    @BindView(R.id.challenge_due_date)
    TextView dueDate;

    @BindView(R.id.challenge_total_time_spent)
    TextView totalTimeSpent;

    private Challenge challenge;

    @Inject
    QuestPersistenceService questPersistenceService;

    @Inject
    RepeatingQuestPersistenceService repeatingQuestPersistenceService;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_challenge_overview, container, false);
        unbinder = ButterKnife.bind(this, view);
        App.getAppComponent(getContext()).inject(this);

        challenge = ((ChallengeActivity) getActivity()).getChallenge();

        displayChallenge();

        eventBus.post(new ScreenShownEvent(EventSource.CHALLENGE_OVERVIEW));

        return view;
    }

    private void displayChallenge() {
        showProgress();
        showSummaryStats();
        setupChart();
    }

    private long totalCount = 0;

    private void showProgress() {
        totalCount = 0;
        Date challengeEnd = challenge.getEndDate();
        LocalDate today = LocalDate.now();
        LocalDate scheduledQuestsEndDate = today.plusWeeks(3).dayOfWeek().withMaximumValue();

        if (notAllQuestsAreScheduledForChallenge(challengeEnd, scheduledQuestsEndDate)) {
            repeatingQuestPersistenceService.findNotDeleted(challenge, repeatingQuests -> {
                Date challengeStart = DateUtils.toStartOfDayUTC(new LocalDate(challenge.getCreatedAt(), DateTimeZone.UTC));
                for (RepeatingQuest rq : repeatingQuests) {
                    Recurrence recurrence = rq.getRecurrence();
                    Date rqStart = recurrence.getDtstart();
                    Date rqEnd = recurrence.getDtend();

                    Date progressStart = challengeStart.after(rqStart) ? challengeStart : rqStart;
                    Date progressEnd = rqEnd == null || challengeEnd.before(rqEnd) ? challengeEnd : rqEnd;

                    float questsPerDayCoefficient = calculateQuestsPerDayCoefficient(recurrence) * recurrence.getTimesADay();

                    int dayCount = (int) TimeUnit.MILLISECONDS.toDays(progressEnd.getTime() - progressStart.getTime()) + 1;
                    totalCount += Math.ceil(dayCount * questsPerDayCoefficient);
                }
                questPersistenceService.countNotRepeating(challenge, count -> {
                    totalCount += count;
                    populateProgress();
                });
            });
        } else {
            questPersistenceService.countNotDeleted(challenge, count -> {
                totalCount = count;
                populateProgress();
            });
        }

    }

    private void populateProgress() {
       questPersistenceService.countCompleted(challenge, completed -> {
           progressFraction.setText(completed + " / " + totalCount);

           int percentDone = Math.round((completed / (float) totalCount) * 100);

           progressPercent.setText(String.valueOf(percentDone) + "% done");

           int progressColor = R.color.colorAccent;

           Category category = Challenge.getCategory(challenge);
           if (category == Category.WORK || category == Category.FUN || category == Category.CHORES) {
               progressColor = R.color.colorAccentAlternative;
           }
           progress.getProgressDrawable().setColorFilter(ContextCompat.getColor(getContext(), progressColor), PorterDuff.Mode.SRC_IN);
           progress.setProgress(percentDone);

           ObjectAnimator animation = ObjectAnimator.ofInt(progress, "progress", 0, percentDone);
           int animationTime = getResources().getInteger(percentDone > 50 ? android.R.integer.config_longAnimTime : android.R.integer.config_mediumAnimTime);
           animation.setDuration(animationTime);
           animation.setInterpolator(new DecelerateInterpolator());
           animation.start();
       });

    }

    private float calculateQuestsPerDayCoefficient(Recurrence recurrence) {
        Recurrence.RecurrenceType recurrenceType = recurrence.getRecurrenceType();
        if (recurrenceType == Recurrence.RecurrenceType.MONTHLY) {
            if (recurrence.isFlexible()) {
                return recurrence.getFlexibleCount() / 31f;
            } else {
                return 1f / 31f;
            }
        } else if (recurrenceType == Recurrence.RecurrenceType.WEEKLY) {
            if (recurrence.isFlexible()) {
                return recurrence.getFlexibleCount() / 7f;
            } else {
                try {
                    return new Recur(recurrence.getRrule()).getDayList().size() / 7f;
                } catch (ParseException e) {
                    return 0.0f;
                }
            }
        } else {
            return 1f;
        }
    }

    private boolean notAllQuestsAreScheduledForChallenge(Date challengeEnd, LocalDate scheduledQuestsEndDate) {
        return new LocalDate(challengeEnd, DateTimeZone.UTC).isAfter(scheduledQuestsEndDate);
    }

    private void showSummaryStats() {
        Category category = Challenge.getCategory(challenge);

        summaryStatsContainer.setBackgroundResource(category.color500);

        categoryName.setText(StringUtils.capitalize(category.name()));
        categoryImage.setImageResource(category.whiteImage);


        Date nextDate = questPersistenceService.findNextUncompletedQuestEndDate(challenge);
        nextScheduledDate.setText(DateFormatter.formatWithoutYear(nextDate, getContext().getString(R.string.unscheduled)));

        dueDate.setText(DateFormatter.formatWithoutYear(challenge.getEndDate()));

        questPersistenceService.findAllCompleted(challenge, quests -> {
            int timeSpent = (int) getTotalTimeSpent(quests);
            totalTimeSpent.setText(timeSpent > 0 ? DurationFormatter.formatShort(timeSpent, "") : "0");
        });
    }

    private long getTotalTimeSpent(List<Quest> completedQuests) {
        long totalTime = 0;
        for (Quest completedQuest : completedQuests) {
            totalTime += completedQuest.getActualDuration();
        }
        return totalTime;
    }

    private void setupChart() {
        history.setDescription("");
        history.setTouchEnabled(false);
        history.setPinchZoom(false);
        history.setExtraBottomOffset(20);

        history.setDrawGridBackground(false);
        history.setDrawBarShadow(true);

        history.setDrawValueAboveBar(false);
        history.setDrawGridBackground(false);

        YAxis leftAxis = history.getAxisLeft();
        leftAxis.setAxisMinValue(0f);
        leftAxis.setEnabled(false);
        history.getAxisRight().setEnabled(false);

        XAxis xLabels = history.getXAxis();
        xLabels.setPosition(XAxis.XAxisPosition.BOTTOM);
        xLabels.setTextColor(ContextCompat.getColor(getContext(), R.color.md_dark_text_54));
        xLabels.setLabelsToSkip(0);
        xLabels.setTextSize(13f);
        xLabels.setDrawAxisLine(false);
        xLabels.setDrawGridLines(false);
        xLabels.setYOffset(5);
        history.getLegend().setEnabled(false);


        List<BarEntry> yValues = new ArrayList<>();
        List<Pair<LocalDate, LocalDate>> weekPairs = getBoundsFor4WeeksInThePast(LocalDate.now());
        for (int i = 0; i < Constants.DEFAULT_BAR_COUNT; i++) {
            Pair<LocalDate, LocalDate> weekPair = weekPairs.get(i);
            yValues.add(new BarEntry(getCompletedForRange(weekPair.first, weekPair.second), i));
        }

        BarDataSet dataSet = new BarDataSet(yValues, "");
        dataSet.setColors(getColors());
        dataSet.setBarShadowColor(ContextCompat.getColor(getContext(), Challenge.getCategory(challenge).color100));

        List<String> xValues = new ArrayList<>();
        xValues.add(getWeekRangeText(weekPairs.get(0).first, weekPairs.get(0).second));
        xValues.add(getWeekRangeText(weekPairs.get(1).first, weekPairs.get(1).second));
        xValues.add("last week");
        xValues.add("this week");
        setHistoryData(dataSet, xValues);
    }

    private long getCompletedForRange(LocalDate start, LocalDate end) {
        return questPersistenceService.countCompleted(challenge, start, end);
    }

    private void setHistoryData(BarDataSet dataSet, List<String> xValues) {
        BarData data = new BarData(xValues, dataSet);
        data.setValueTextSize(14f);
        data.setValueTextColor(Color.WHITE);
        data.setValueFormatter((value, entry, dataSetIndex, viewPortHandler) -> {
            if (value == 0) {
                return "";
            }
            return String.valueOf((int) value);
        });

        history.setData(data);
        history.invalidate();
        history.animateY(1400, Easing.EasingOption.EaseInOutQuart);
    }

    private int[] getColors() {
        int[] colors = new int[Constants.DEFAULT_BAR_COUNT];
        for (int i = 0; i < Constants.DEFAULT_BAR_COUNT; i++) {
            colors[i] = ContextCompat.getColor(getContext(), Challenge.getCategory(challenge).color300);
        }
        return colors;
    }

    @NonNull
    private List<Pair<LocalDate, LocalDate>> getBoundsFor4WeeksInThePast(LocalDate currentDate) {
        LocalDate weekStart = currentDate.minusWeeks(3).dayOfWeek().withMinimumValue();
        LocalDate weekEnd = weekStart.dayOfWeek().withMaximumValue();

        List<Pair<LocalDate, LocalDate>> weekBounds = new ArrayList<>();
        weekBounds.add(new Pair<>(weekStart, weekEnd));
        for (int i = 0; i < 3; i++) {
            weekStart = weekStart.plusWeeks(1);
            weekEnd = weekStart.dayOfWeek().withMaximumValue();
            weekBounds.add(new Pair<>(weekStart, weekEnd));
        }
        return weekBounds;
    }

    private String getWeekRangeText(LocalDate weekStart, LocalDate weekEnd) {
        if (weekStart.getMonthOfYear() == weekEnd.getMonthOfYear()) {
            return weekStart.getDayOfMonth() + " - " + weekEnd.getDayOfMonth() + " " + weekEnd.monthOfYear().getAsShortText();
        } else {
            return weekStart.getDayOfMonth() + " " + weekStart.monthOfYear().getAsShortText() + " - " + weekEnd.getDayOfMonth() + " " + weekEnd.monthOfYear().getAsShortText();
        }
    }

    @Override
    protected boolean useOptionsMenu() {
        return true;
    }

    @Override
    protected void showHelpDialog() {
        HelpDialog.newInstance(R.layout.fragment_help_dialog_challenge, R.string.help_dialog_challenge_title, "challenge").show(getActivity().getSupportFragmentManager());
    }

    @Override
    public void onResume() {
        super.onResume();
        eventBus.register(this);

    }

    @Override
    public void onPause() {
        eventBus.unregister(this);
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
        super.onDestroyView();
    }

    @Subscribe
    public void onQuestSaved(QuestSavedEvent e) {
        displayChallenge();
    }

    @Subscribe
    public void onQuestsSaved(QuestsSavedEvent e) {
        displayChallenge();
    }

    @Subscribe
    public void onRepeatingQuestSaved(RepeatingQuestSavedEvent e) {
        displayChallenge();
    }

    @Subscribe
    public void onRepeatingQuestsSaved(RepeatingQuestsSavedEvent e) {
        displayChallenge();
    }
}

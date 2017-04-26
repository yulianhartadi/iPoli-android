package io.ipoli.android.quest.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;

import org.threeten.bp.LocalDate;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.activities.BaseActivity;
import io.ipoli.android.app.events.CalendarDayChangedEvent;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.events.ScreenShownEvent;
import io.ipoli.android.app.ui.EmptyStateRecyclerView;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.ViewUtils;
import io.ipoli.android.quest.adapters.AgendaAdapter;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.viewmodels.AgendaViewModel;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 12/30/16.
 */

public class AgendaActivity extends BaseActivity implements CalendarView.OnDateChangeListener {

    @Inject
    QuestPersistenceService questPersistenceService;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.agenda_calendar)
    CalendarView calendar;

    @BindView(R.id.agenda_list_container)
    ViewGroup questListContainer;

    @BindView(R.id.agenda_list)
    EmptyStateRecyclerView questList;

    @BindView(R.id.agenda_journey_text)
    TextView journeyText;

    private LocalDate selectedDate;
    private boolean use24HourFormat;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_agenda);
        ButterKnife.bind(this);
        appComponent().inject(this);

        long selectedDateMillis = getIntent().getLongExtra(Constants.CURRENT_SELECTED_DAY_EXTRA_KEY, 0);
        if (selectedDateMillis == 0) {
            finish();
            return;
        }

        eventBus.post(new ScreenShownEvent(EventSource.AGENDA_CALENDAR));

        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
        }

        use24HourFormat = shouldUse24HourFormat();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        questList.setLayoutManager(layoutManager);
        questList.setHasFixedSize(true);

        questList.setEmptyView(questListContainer, R.string.empty_agenda_text, R.drawable.ic_calendar_blank_grey_24dp);

        calendar.setDate(selectedDateMillis, true, true);
        calendar.setOnDateChangeListener(this);
        selectedDate = DateUtils.fromMillis(selectedDateMillis);

        if (Build.VERSION.SDK_INT < 23) {
            ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) calendar.getLayoutParams();
            lp.height = (int) ViewUtils.dpToPx(300, getResources());
            lp.topMargin = (int) ViewUtils.dpToPx(16, getResources());
            calendar.setLayoutParams(lp);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        showQuestsForDate(selectedDate);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem helpMenu = menu.findItem(R.id.action_help);
        if (helpMenu != null) {
            helpMenu.setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onClose();
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onBackPressed() {
        onClose();
    }

    private void onClose() {
        finish();
        overridePendingTransition(android.R.anim.fade_in, R.anim.slide_out_bottom);
    }

    private void showQuestsForDate(LocalDate date) {
        eventBus.post(new CalendarDayChangedEvent(date, CalendarDayChangedEvent.Source.AGENDA_CALENDAR));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getString(getToolbarText(date)), Locale.getDefault());
        Date startOfDayDate = DateUtils.toStartOfDay(date);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(simpleDateFormat.format(startOfDayDate));
        }
        String dayNumberSuffix = DateUtils.getDayNumberSuffix(date.getDayOfMonth());
        DateFormat dateFormat = new SimpleDateFormat(getString(R.string.agenda_daily_journey_format, dayNumberSuffix));
        journeyText.setText(getString(R.string.agenda_daily_journey, dateFormat.format(startOfDayDate)));
        questPersistenceService.findAllNonAllDayForDate(date, quests -> {
            List<AgendaViewModel> vms = new ArrayList<>();
            for (Quest quest : quests) {
                vms.add(new AgendaViewModel(this, quest, use24HourFormat));
            }
            questList.setAdapter(new AgendaAdapter(this, eventBus, vms));
        });
    }

    private int getToolbarText(LocalDate date) {
        if (date.isEqual(LocalDate.now().minusDays(1))) {
            return R.string.yesterday_calendar_format;
        }
        if (date.isEqual(LocalDate.now())) {
            return R.string.today_calendar_format;
        }
        if (date.isEqual(LocalDate.now().plusDays(1))) {
            return R.string.tomorrow_calendar_format;
        }
        return R.string.agenda_calendar_format;
    }

    @Override
    public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
        selectedDate = LocalDate.of(year, month + 1, dayOfMonth);
        showQuestsForDate(selectedDate);
    }

    @Override
    public void finish() {
        Intent data = new Intent();
        data.putExtra(Constants.CURRENT_SELECTED_DAY_EXTRA_KEY, DateUtils.toMillis(selectedDate));
        setResult(RESULT_OK, data);
        super.finish();
    }
}

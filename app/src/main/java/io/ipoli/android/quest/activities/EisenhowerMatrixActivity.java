package io.ipoli.android.quest.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import org.threeten.bp.LocalDate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.activities.BaseActivity;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.events.ScreenShownEvent;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.quest.adapters.EisenhowerMatrixAdapter;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.viewmodels.EisenhowerMatrixViewModel;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/26/17.
 */
public class EisenhowerMatrixActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.do_list)
    RecyclerView doList;

    @BindView(R.id.accomplish_list)
    RecyclerView accomplishList;

    @BindView(R.id.delegate_list)
    RecyclerView delegateList;

    @BindView(R.id.delete_list)
    RecyclerView deleteList;

    @Inject
    QuestPersistenceService questPersistenceService;

    private boolean use24HourFormat;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        appComponent().inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eisenhower_matrix);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();

        long selectedDateMillis = getIntent().getLongExtra(Constants.CURRENT_SELECTED_DAY_EXTRA_KEY, 0);
        if (selectedDateMillis == 0) {
            finish();
            return;
        }

        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
            LocalDate selectedDate = DateUtils.fromMillis(selectedDateMillis);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getString(getToolbarText(selectedDate)), Locale.getDefault());
            ab.setTitle(simpleDateFormat.format(DateUtils.toStartOfDay(selectedDate)));
        }

        use24HourFormat = shouldUse24HourFormat();

        setLayoutManagerToList(doList);
        setLayoutManagerToList(accomplishList);
        setLayoutManagerToList(delegateList);
        setLayoutManagerToList(deleteList);
        showMatrix();
        eventBus.post(new ScreenShownEvent(EventSource.EISENHOWER_MATRIX));
    }

    private void showMatrix() {
        questPersistenceService.findAllNonAllDayForDate(LocalDate.now(), quests -> {
            List<EisenhowerMatrixViewModel> doQuests = new ArrayList<>();
            List<EisenhowerMatrixViewModel> accomplishQuests = new ArrayList<>();
            List<EisenhowerMatrixViewModel> delegateQuests = new ArrayList<>();
            List<EisenhowerMatrixViewModel> deleteQuests = new ArrayList<>();
            for (Quest quest : quests) {
                switch (quest.getPriority()) {
                    case Quest.PRIORITY_IMPORTANT_NOT_URGENT:
                        accomplishQuests.add(new EisenhowerMatrixViewModel(this, quest, use24HourFormat));
                        break;
                    case Quest.PRIORITY_NOT_IMPORTANT_URGENT:
                        delegateQuests.add(new EisenhowerMatrixViewModel(this, quest, use24HourFormat));
                        break;
                    case Quest.PRIORITY_NOT_IMPORTANT_NOT_URGENT:
                        deleteQuests.add(new EisenhowerMatrixViewModel(this, quest, use24HourFormat));
                        break;
                    default:
                        doQuests.add(new EisenhowerMatrixViewModel(this, quest, use24HourFormat));
                }
            }
            doList.setAdapter(new EisenhowerMatrixAdapter(this, eventBus, doQuests));
            accomplishList.setAdapter(new EisenhowerMatrixAdapter(this, eventBus, accomplishQuests));
            delegateList.setAdapter(new EisenhowerMatrixAdapter(this, eventBus, delegateQuests));
            deleteList.setAdapter(new EisenhowerMatrixAdapter(this, eventBus, deleteQuests));
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

    private void setLayoutManagerToList(RecyclerView list) {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        list.setLayoutManager(layoutManager);
        list.setHasFixedSize(true);
    }

    @Override
    protected boolean useParentOptionsMenu() {
        return false;
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
}
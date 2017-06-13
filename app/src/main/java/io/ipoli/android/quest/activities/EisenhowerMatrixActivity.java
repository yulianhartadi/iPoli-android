package io.ipoli.android.quest.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
import io.ipoli.android.app.persistence.OnDataChangedListener;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.quest.adapters.EisenhowerMatrixAdapter;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.viewmodels.EisenhowerMatrixViewModel;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/26/17.
 */
public class EisenhowerMatrixActivity extends BaseActivity implements OnDataChangedListener<List<Quest>> {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.matrix_container)
    ViewGroup matrixContainer;

    @BindView(R.id.do_list)
    RecyclerView doList;

    @BindView(R.id.accomplish_list)
    RecyclerView accomplishList;

    @BindView(R.id.delegate_list)
    RecyclerView delegateList;

    @BindView(R.id.delete_list)
    RecyclerView deleteList;

    @BindView(R.id.empty_view)
    ViewGroup emptyView;

    @BindView(R.id.empty_image)
    ImageView emptyImage;

    @BindView(R.id.empty_text)
    TextView emptyText;

    @Inject
    QuestPersistenceService questPersistenceService;

    private boolean use24HourFormat;
    private LocalDate selectedDate;

    private EisenhowerMatrixAdapter doListAdapter;
    private EisenhowerMatrixAdapter accomplishListAdapter;
    private EisenhowerMatrixAdapter delegateListAdapter;
    private EisenhowerMatrixAdapter deleteListAdapter;

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

        selectedDate = DateUtils.fromMillis(selectedDateMillis);
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getString(getToolbarText(selectedDate)), Locale.getDefault());
            ab.setTitle(simpleDateFormat.format(DateUtils.toStartOfDay(selectedDate)));
        }

        use24HourFormat = shouldUse24HourFormat();

        setLayoutManagerToList(doList);
        setLayoutManagerToList(accomplishList);
        setLayoutManagerToList(delegateList);
        setLayoutManagerToList(deleteList);
        eventBus.post(new ScreenShownEvent(this, EventSource.EISENHOWER_MATRIX));
    }

    @Override
    protected void onStart() {
        super.onStart();
        questPersistenceService.listenForAllNonAllDayForDate(selectedDate, this);
    }

    @Override
    protected void onStop() {
        questPersistenceService.removeAllListeners();
        super.onStop();
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

    @Override
    public void onDataChanged(List<Quest> quests) {
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

        if (doQuests.isEmpty() && accomplishQuests.isEmpty() && delegateQuests.isEmpty() && deleteQuests.isEmpty()) {
            matrixContainer.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
            return;
        }

        if(doListAdapter == null) {
            doListAdapter = new EisenhowerMatrixAdapter(this, eventBus, new ArrayList<>());
            doList.setAdapter(doListAdapter);
        }
        doListAdapter.setViewModels(doQuests);

        if(accomplishListAdapter == null) {
            accomplishListAdapter = new EisenhowerMatrixAdapter(this, eventBus, new ArrayList<>());
            accomplishList.setAdapter(accomplishListAdapter);
        }
        accomplishListAdapter.setViewModels(accomplishQuests);

        if(delegateListAdapter == null) {
            delegateListAdapter = new EisenhowerMatrixAdapter(this, eventBus, new ArrayList<>());
            delegateList.setAdapter(delegateListAdapter);
        }
        delegateListAdapter.setViewModels(delegateQuests);

        if(deleteListAdapter == null) {
            deleteListAdapter = new EisenhowerMatrixAdapter(this, eventBus, new ArrayList<>());
            deleteList.setAdapter(deleteListAdapter);
        }
        deleteListAdapter.setViewModels(deleteQuests);
    }
}
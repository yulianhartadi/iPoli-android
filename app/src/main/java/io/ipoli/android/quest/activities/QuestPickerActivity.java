package io.ipoli.android.quest.activities;

import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.app.activities.BaseActivity;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.events.ScreenShownEvent;
import io.ipoli.android.app.persistence.OnDataChangedListener;
import io.ipoli.android.app.ui.EmptyStateRecyclerView;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.quest.adapters.QuestPickerAdapter;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.persistence.QuestPersistenceService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/26/17.
 */
public class QuestPickerActivity extends BaseActivity {

    public static final int MIN_FILTER_QUERY_LEN = 3;

    @Inject
    QuestPersistenceService questPersistenceService;

    @BindView(R.id.root_container)
    ViewGroup rootContainer;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.result_list)
    EmptyStateRecyclerView resultList;

    private QuestPickerAdapter adapter;

    private List<Quest> allQuests;

    private SearchView searchView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        appComponent().inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quest_picker);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        resultList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        resultList.setEmptyView(rootContainer, R.string.empty_daily_challenge_quests_text, R.drawable.ic_compass_grey_24dp);
        adapter = new QuestPickerAdapter(this, eventBus, new ArrayList<>());
        resultList.setAdapter(adapter);

        eventBus.post(new ScreenShownEvent(this, EventSource.PICK_CHALLENGE_QUESTS));
    }

    @Override
    protected void onStart() {
        super.onStart();
        questPersistenceService.listenForAllNonAllDayCompletedBetween(LocalDate.now().minusDays(1), LocalDate.now(), new OnDataChangedListener<List<Quest>>() {
            @Override
            public void onDataChanged(List<Quest> result) {
                allQuests = new ArrayList<>();
                allQuests.addAll(result);
                String searchQuery = searchView != null ? searchView.getQuery().toString() : "";
                updateAdapter(searchQuery);
            }
        });
    }

    private void updateAdapter(String query) {
        filter(query, quests -> adapter.setQuests(quests));
    }

    private void filter(String query, QuestPickerActivity.FilterListener filterListener) {
        if (query == null) {
            return;
        }

        List<Quest> quests = new ArrayList<>();
        for(Quest q : allQuests) {
            if (q.getName().toLowerCase().contains(query.toLowerCase())) {
                quests.add(q);
            }
        }

        Collections.sort(quests,
                (q1, q2) -> {
                    int dateResult = - Long.compare(q1.getCompletedAt(), q2.getCompletedAt());
                    if(dateResult != 0) {
                        return dateResult;
                    }

                    return - Long.compare(q1.getCompletedAtMinute(), q2.getCompletedAtMinute());
                });
        filterListener.onFilterCompleted(quests);
    }

    @Override
    protected boolean useParentOptionsMenu() {
        return false;
    }

    @Override
    protected void onStop() {
        questPersistenceService.removeAllListeners();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.quest_picker_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (StringUtils.isEmpty(newText)) {
                    filter("", quests -> adapter.setQuests(quests));
                    return true;
                }

                if (newText.trim().length() < MIN_FILTER_QUERY_LEN) {
                    return true;
                }
                filter(newText.trim(), quests -> adapter.setQuests(quests));
                return true;
            }
        });

        return true;
    }

    public interface FilterListener {
        void onFilterCompleted(List<Quest> quests);
    }
}
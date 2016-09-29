package io.ipoli.android.quest.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.Chronometer;
import android.widget.ProgressBar;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.app.activities.BaseActivity;
import io.ipoli.android.note.data.Note;
import io.ipoli.android.quest.adapters.QuestDetailsAdapter;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.SubQuest;
import io.ipoli.android.quest.events.EditNoteRequestEvent;
import io.ipoli.android.quest.ui.dialogs.TextPickerFragment;
import io.ipoli.android.quest.ui.formatters.TimerFormatter;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/26/16.
 */

public class QuestDetailActivity extends BaseActivity implements Chronometer.OnChronometerTickListener, TextPickerFragment.OnTextPickedListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.appbar)
    AppBarLayout appBar;

    @BindView(R.id.toolbar_collapsing_container)
    CollapsingToolbarLayout collapsingToolbarLayout;

    @BindView(R.id.quest_details_progress)
    ProgressBar timerProgress;

    @BindView(R.id.quest_details_time)
    Chronometer timer;

    @BindView(R.id.quest_details)
    RecyclerView details;
    private Quest quest;
    private QuestDetailsAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quest_detail);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowTitleEnabled(true);
            ab.setTitle("Workout");
        }
        collapsingToolbarLayout.setTitleEnabled(false);

        timer.setBase(0);
        timer.setText(TimerFormatter.format(TimeUnit.MINUTES.toMillis(20)));
        timer.start();

        timer.setOnChronometerTickListener(this);

        timerProgress.setProgress(80);


        details.setLayoutManager(new LinearLayoutManager(this));
        details.setHasFixedSize(true);

        quest = new Quest("Hello world");

        List<SubQuest> subQuests = new ArrayList<>();
        subQuests.add(new SubQuest("Prepare Barbell"));
        subQuests.add(new SubQuest("Do 10 pull-ups"));
        subQuests.add(new SubQuest("Do 10 push-ups"));

        quest.setSubQuests(subQuests);

        List<Note> notes = new ArrayList<>();
        notes.add(new Note("Workout hard even though Vihar is not the smartest cat in the world!"));
        notes.add(new Note(Note.Type.URL, "Visit Medium", "https://medium.com/"));
        notes.add(new Note(Note.Type.INTENT, "Learn English on Duolingo", "https://medium.com/"));
        quest.setNotes(notes);

        adapter = new QuestDetailsAdapter(this, quest, eventBus);
        details.setAdapter(adapter);

//        adapter.setSubQuests(subQuests);
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
    public void onChronometerTick(Chronometer chronometer) {
        timerProgress.setProgress(timerProgress.getProgress() + new Random().nextInt(2));
    }

    @Subscribe
    public void onEditNoteRequest(EditNoteRequestEvent e) {
        TextPickerFragment.newInstance((String) e.note.getText(), R.string.pick_note_title, this).show(getSupportFragmentManager());
    }

    @Override
    public void onTextPicked(String text) {
        List<Note> notes = quest.getTextNotes();
        if (!notes.isEmpty()) {
            notes.get(0).setText(text);
        }
        adapter.updateNotes(quest.getNotes());
    }
}

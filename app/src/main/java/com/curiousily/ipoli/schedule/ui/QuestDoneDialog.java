package com.curiousily.ipoli.schedule.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;

import com.curiousily.ipoli.EventBus;
import com.curiousily.ipoli.R;
import com.curiousily.ipoli.quest.Quest;
import com.curiousily.ipoli.schedule.events.QuestRatedEvent;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/2/15.
 */
public class QuestDoneDialog extends DialogFragment {

    @Bind(R.id.quest_rate_rating_bar)
    RatingBar rating;

    @Bind(R.id.quest_rate_journal)
    EditText journal;

    private Quest quest;

    public static QuestDoneDialog newInstance() {
        return new QuestDoneDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_quest_rate, null);
        ButterKnife.bind(this, view);
        setCancelable(false);
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.quest_done_title)
                .setView(view)
                .create();
    }

    @OnClick(R.id.quest_rate_done)
    public void onDoneClick() {
        quest.rating = (int) rating.getRating();
        quest.log = journal.getText().toString();
        EventBus.post(new QuestRatedEvent(quest));
    }


    public void setQuest(Quest quest) {
        this.quest = quest;
    }
}
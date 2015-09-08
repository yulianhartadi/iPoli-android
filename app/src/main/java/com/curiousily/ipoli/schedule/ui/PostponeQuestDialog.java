package com.curiousily.ipoli.schedule.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioGroup;

import com.curiousily.ipoli.Constants;
import com.curiousily.ipoli.EventBus;
import com.curiousily.ipoli.R;
import com.curiousily.ipoli.quest.Quest;
import com.curiousily.ipoli.schedule.events.QuestPostponedEvent;

import java.util.Calendar;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/2/15.
 */
public class PostponeQuestDialog extends DialogFragment implements DialogInterface.OnClickListener {

    @Bind(R.id.postpone_quest_group)
    RadioGroup options;

    private Quest quest;

    public static PostponeQuestDialog newInstance() {
        return new PostponeQuestDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_postpone_quest, null);
        ButterKnife.bind(this, view);
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.dialog_postpone_title)
                .setCancelable(false)
                .setView(view)
                .setPositiveButton(R.string.ok, this)
                .setNegativeButton(R.string.cancel, this)
                .create();
    }

    public void setQuest(Quest quest) {
        this.quest = quest;
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int button) {
        switch (button) {
            case DialogInterface.BUTTON_POSITIVE:

                int radioButton = options.getCheckedRadioButtonId();

                Calendar c = Calendar.getInstance();
                c.setTime(quest.due);

                switch (radioButton) {
                    case R.id.postpone_quest_tomorrow:
                        c.add(Calendar.DATE, 1);
                        break;

                    case R.id.postpone_quest_week:
                        c.add(Calendar.DATE, Constants.DAYS_IN_A_WEEK);
                        break;

                    case R.id.postpone_quest_month:
                        c.add(Calendar.MONTH, 1);
                        break;

                    case R.id.postpone_quest_cancel:
                        quest.status = Quest.Status.CANCELED;
                }

                quest.due = c.getTime();

                EventBus.post(new QuestPostponedEvent(quest));
                break;
        }
    }
}
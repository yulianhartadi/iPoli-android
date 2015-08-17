package com.curiousily.ipoli.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import com.curiousily.ipoli.R;
import com.curiousily.ipoli.quest.Quest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/2/15.
 */
public class QuestDoneDialog extends DialogFragment {

    public static QuestDoneDialog newInstance(Quest quest) {
        QuestDoneDialog frag = new QuestDoneDialog();
        Bundle args = new Bundle();
        frag.setArguments(args);
        return frag;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_quest_rate, null);
        return new AlertDialog.Builder(getActivity())
                .setTitle("Great job! Did you liked it?")
                .setCancelable(false)
                .setView(view)
                .create();
    }
}
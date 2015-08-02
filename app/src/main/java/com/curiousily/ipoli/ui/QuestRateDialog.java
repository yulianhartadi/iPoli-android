package com.curiousily.ipoli.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import com.curiousily.ipoli.R;
import com.curiousily.ipoli.models.Quest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/2/15.
 */
public class QuestRateDialog extends DialogFragment {

    public static QuestRateDialog newInstance(Quest quest) {
        QuestRateDialog frag = new QuestRateDialog();
        Bundle args = new Bundle();
        args.putString("name", quest.name);
        frag.setArguments(args);
        return frag;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String name = getArguments().getString("name");
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_quest_rate, null);
        return new AlertDialog.Builder(getActivity())
                .setTitle("How much you enjoyed this quest?")
                .setCancelable(false)
                .setView(view)
                .create();
    }
}
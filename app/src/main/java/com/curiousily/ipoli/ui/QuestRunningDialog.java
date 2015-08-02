package com.curiousily.ipoli.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.curiousily.ipoli.R;
import com.curiousily.ipoli.models.Quest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/17/15.
 */
public class QuestRunningDialog extends DialogFragment {

    public static QuestRunningDialog newInstance(Quest quest) {
        QuestRunningDialog frag = new QuestRunningDialog();
        Bundle args = new Bundle();
        args.putString("name", quest.name);
        args.putString("duration", quest.duration + "");
        frag.setArguments(args);
        return frag;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String name = getArguments().getString("name");
        String duration = getArguments().getString("duration");
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_quest_running, null);
        TextView durationView = (TextView) view.findViewById(R.id.quest_running_duration);
        durationView.setText("9:54");
        return new AlertDialog.Builder(getActivity())
                .setTitle(name)
                .setCancelable(false)
                .setView(view)
                .create();
    }
}

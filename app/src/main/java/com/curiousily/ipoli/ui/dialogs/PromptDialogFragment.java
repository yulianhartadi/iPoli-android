package com.curiousily.ipoli.ui.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.curiousily.ipoli.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/11/15.
 */
public class PromptDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    @Bind(R.id.dialog_prompt_input)
    EditText input;

    public static PromptDialogFragment newInstance(int title) {
        PromptDialogFragment fragment = new PromptDialogFragment();
        Bundle arguments = new Bundle();
        arguments.putInt("title", title);
        fragment.setArguments(arguments);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_prompt, null);
        ButterKnife.bind(this, view);

        return new AlertDialog.Builder(getActivity())
                .setTitle(getArguments().getInt("title"))
                .setCancelable(false)
                .setView(view)
                .setPositiveButton(R.string.save, this)
                .setNegativeButton(R.string.cancel, this)
                .create();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {

    }
}

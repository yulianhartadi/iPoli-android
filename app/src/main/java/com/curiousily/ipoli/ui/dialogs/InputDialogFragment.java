package com.curiousily.ipoli.ui.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import com.curiousily.ipoli.EventBus;
import com.curiousily.ipoli.R;
import com.curiousily.ipoli.ui.events.UserInputEvent;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/11/15.
 */
public class InputDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    @Bind(R.id.dialog_input)
    EditText input;

    public static InputDialogFragment newInstance(int title) {
        InputDialogFragment fragment = new InputDialogFragment();
        Bundle arguments = new Bundle();
        arguments.putInt("title", title);
        fragment.setArguments(arguments);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_input, null);
        ButterKnife.bind(this, view);

        Dialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(getArguments().getInt("title"))
                .setCancelable(false)
                .setView(view)
                .setPositiveButton(R.string.save, this)
                .setNegativeButton(R.string.cancel, this)
                .create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        return dialog;
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            EventBus.post(new UserInputEvent(input.getText().toString()));
        }
    }
}

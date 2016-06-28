package io.ipoli.android.challenge.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;

import io.ipoli.android.R;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.challenge.data.Difficulty;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/23/16.
 */
public class DifficultyPickerFragment extends DialogFragment {

    private static final String TAG = "difficulty-picker-dialog";
    private static final String DIFFICULTY = "difficulty";

    private Difficulty difficulty;
    private int selectedDifficultyIndex;

    private OnDifficultyPickedListener difficultyPickedListener;

    public interface OnDifficultyPickedListener {
        void onDifficultyPicked(Difficulty difficulty);
    }

    public static DifficultyPickerFragment newInstance(Difficulty difficulty, OnDifficultyPickedListener difficultyPickedListener) {
        DifficultyPickerFragment fragment = new DifficultyPickerFragment();
        Bundle args = new Bundle();
        args.putSerializable(DIFFICULTY, difficulty);
        fragment.setArguments(args);
        fragment.difficultyPickedListener = difficultyPickedListener;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            difficulty = (Difficulty) getArguments().getSerializable(DIFFICULTY);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String[] names = new String[Difficulty.values().length];
        selectedDifficultyIndex = -1;
        for (int i = 0; i < Difficulty.values().length; i++) {
            Difficulty d = Difficulty.values()[i];
            names[i] = StringUtils.capitalize(d.name());
            if (difficulty == d) {
                selectedDifficultyIndex = i;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(R.drawable.logo)
                .setTitle(R.string.challenge_difficulty_question)
                .setSingleChoiceItems(names, selectedDifficultyIndex, (dialog, which) -> {
                    selectedDifficultyIndex = which;
                })
                .setPositiveButton(getString(R.string.help_dialog_ok), (dialog, which) -> {
                    difficultyPickedListener.onDifficultyPicked(Difficulty.values()[selectedDifficultyIndex]);
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {

                });
        return builder.create();

    }

    public void show(FragmentManager fragmentManager) {
        show(fragmentManager, TAG);
    }
}

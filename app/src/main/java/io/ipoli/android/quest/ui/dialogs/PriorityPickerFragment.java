package io.ipoli.android.quest.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;

import io.ipoli.android.R;
import io.ipoli.android.app.ui.formatters.PriorityFormatter;
import io.ipoli.android.quest.data.Quest;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 6/14/16.
 */
public class PriorityPickerFragment extends DialogFragment {
    private static final String TAG = "priority-picker-dialog";
    private static final String PRIORITY = "priority";

    private int priority;
    private int selectedPriorityIndex;

    private OnPriorityPickedListener priorityPickedListener;

    public interface OnPriorityPickedListener {
        void onPriorityPicked(int priority);
    }

    public static PriorityPickerFragment newInstance(OnPriorityPickedListener priorityPickedListener) {
        return newInstance(-1, priorityPickedListener);
    }

    public static PriorityPickerFragment newInstance(int priority, OnPriorityPickedListener priorityPickedListener) {
        PriorityPickerFragment fragment = new PriorityPickerFragment();
        Bundle args = new Bundle();
        args.putInt(PRIORITY, priority);
        fragment.setArguments(args);
        fragment.priorityPickedListener = priorityPickedListener;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            priority = getArguments().getInt(PRIORITY);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int[] availablePriorities = {Quest.PRIORITY_IMPORTANT_URGENT, Quest.PRIORITY_IMPORTANT_NOT_URGENT,
                Quest.PRIORITY_NOT_IMPORTANT_URGENT, Quest.PRIORITY_NOT_IMPORTANT_NOT_URGENT};
        String[] priorities = new String[availablePriorities.length];
        selectedPriorityIndex = -1;
        priority = priority == -1 ? Quest.PRIORITY_NOT_IMPORTANT_NOT_URGENT : priority;

        for (int i = 0; i < availablePriorities.length; i++) {
            int p = availablePriorities[i];
            priorities[i] = PriorityFormatter.format(getContext(), p);
            if (p == priority) {
                selectedPriorityIndex = i;
            }
        }

        selectedPriorityIndex = selectedPriorityIndex == -1 ? 0 : selectedPriorityIndex;

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(R.drawable.logo)
                .setTitle(R.string.quest_priority_question)
                .setSingleChoiceItems(priorities, selectedPriorityIndex, (dialog, which) -> {
                    selectedPriorityIndex = which;
                })
                .setPositiveButton(getString(R.string.help_dialog_ok), (dialog, which) ->
                        priorityPickedListener.onPriorityPicked(availablePriorities[selectedPriorityIndex]))
                .setNegativeButton(R.string.cancel, (dialog, which) -> {

                });
        return builder.create();

    }

    public void show(FragmentManager fragmentManager) {
        show(fragmentManager, TAG);
    }

}

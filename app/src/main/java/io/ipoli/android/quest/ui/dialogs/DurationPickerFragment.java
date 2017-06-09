package io.ipoli.android.quest.ui.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.ui.formatters.DurationFormatter;
import io.ipoli.android.player.UpgradeDialog;
import io.ipoli.android.player.UpgradeManager;
import io.ipoli.android.store.Upgrade;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 6/14/16.
 */
public class DurationPickerFragment extends DialogFragment {
    private static final String TAG = "duration-picker-dialog";
    private static final String DURATION = "duration";

    @Inject
    UpgradeManager upgradeManager;

    private Integer duration;
    private int selectedDurationIndex;

    private OnDurationPickedListener durationPickedListener;

    public static DurationPickerFragment newInstance(OnDurationPickedListener durationPickedListener) {
        return newInstance(null, durationPickedListener);
    }

    public static DurationPickerFragment newInstance(Integer duration, OnDurationPickedListener durationPickedListener) {
        DurationPickerFragment fragment = new DurationPickerFragment();
        Bundle args = new Bundle();
        args.putInt(DURATION, Math.max(duration, Constants.QUEST_MIN_DURATION));
        fragment.setArguments(args);
        fragment.durationPickedListener = durationPickedListener;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getAppComponent(getActivity()).inject(this);
        if (getArguments() != null) {
            duration = getArguments().getInt(DURATION);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        List<Integer> availableDurations = new ArrayList<>(Arrays.asList(Constants.DURATIONS));
        List<String> questDurations = new ArrayList<>();
        selectedDurationIndex = -1;
        for (int i = 0; i < availableDurations.size(); i++) {
            int d = availableDurations.get(i);
            questDurations.add(DurationFormatter.formatReadable(getContext(), d));
            if (d == duration) {
                selectedDurationIndex = i;
            }
        }

        if (selectedDurationIndex == -1) {
            selectedDurationIndex = 0;
            availableDurations.add(selectedDurationIndex, duration);
            questDurations.add(selectedDurationIndex, DurationFormatter.formatReadable(getContext(), duration));
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(R.drawable.logo)
                .setTitle(R.string.quest_duration_question)
                .setSingleChoiceItems(questDurations.toArray(new String[questDurations.size()]), selectedDurationIndex, (dialog, which) -> {
                    selectedDurationIndex = which;
                })
                .setPositiveButton(getString(R.string.help_dialog_ok), (dialog, which) -> durationPickedListener.onDurationPicked(availableDurations.get(selectedDurationIndex)))
                .setNegativeButton(R.string.cancel, (dialog, which) -> {

                })
                .setNeutralButton(R.string.custom, (dialog, which) -> {
                });

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> onShowDialog(dialog));

        return dialog;

    }

    private void onShowDialog(AlertDialog dialog) {
        Button custom = dialog.getButton(DialogInterface.BUTTON_NEUTRAL);
        custom.setOnClickListener(v -> {
            if (upgradeManager.isLocked(Upgrade.CUSTOM_DURATION)) {
                UpgradeDialog.newInstance(Upgrade.CUSTOM_DURATION, new UpgradeDialog.OnUnlockListener() {
                    @Override
                    public void onUnlock() {
                        CustomDurationPickerFragment.newInstance(durationPickedListener).show(getFragmentManager());
                        dismiss();
                    }
                }).show(getFragmentManager());
                return;
            }

            CustomDurationPickerFragment.newInstance(durationPickedListener).show(getFragmentManager());
            dismiss();
        });
    }

    public void show(FragmentManager fragmentManager) {
        show(fragmentManager, TAG);
    }

}

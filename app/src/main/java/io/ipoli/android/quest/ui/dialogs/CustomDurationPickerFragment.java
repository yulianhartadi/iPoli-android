package io.ipoli.android.quest.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.ipoli.android.Constants;
import io.ipoli.android.R;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 6/7/17.
 */

public class CustomDurationPickerFragment extends DialogFragment {
    private static final String TAG = "custom-duration-picker-dialog";
    private static final String DURATION = "duration";

    private OnCustomDurationPickedListener durationPickedListener;

    private int duration;
    private Unbinder unbinder;

    public interface OnCustomDurationPickedListener{
        void onCustomDurationPicked(int duration);
    }

    public static CustomDurationPickerFragment newInstance(OnCustomDurationPickedListener durationPickedListener) {
        return newInstance(-1, durationPickedListener);
    }

    public static CustomDurationPickerFragment newInstance(int duration, OnCustomDurationPickedListener durationPickedListener) {
        CustomDurationPickerFragment fragment = new CustomDurationPickerFragment();
        Bundle args = new Bundle();
        args.putInt(DURATION, Math.max(duration, Constants.QUEST_MIN_DURATION));
        fragment.setArguments(args);
        fragment.durationPickedListener = durationPickedListener;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            duration = getArguments().getInt(DURATION);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_text_picker, null);
        unbinder = ButterKnife.bind(this, view);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(R.drawable.logo)
                .setTitle(R.string.quest_duration_question)
                .setView(view)
                .setPositiveButton(getString(R.string.help_dialog_ok), (dialog, which) -> {

                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {

                });
        return builder.create();

    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
        super.onDestroyView();
    }


    public void show(FragmentManager fragmentManager) {
        show(fragmentManager, TAG);
    }

}

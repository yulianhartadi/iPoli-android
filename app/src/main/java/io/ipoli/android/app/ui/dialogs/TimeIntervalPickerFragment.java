package io.ipoli.android.app.ui.dialogs;


import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.ipoli.android.R;
import io.ipoli.android.app.utils.Time;

public class TimeIntervalPickerFragment extends DialogFragment {

    public static final String TAG = "time-interval-picker-dialog";
    private static final String START_MINUTES_AFTER_MIDNIGHT = "start_minutes_after_midnight";
    private static final String END_MINUTES_AFTER_MIDNIGHT = "end_minutes_after_midnight";
    private static final String TITLE = "title";

    private Time startTime;
    private Time endTime;
    private OnTimePickedListener timePickedListener;
    private Unbinder unbinder;
    private int title;

    @BindView(R.id.start_time)
    Button startTimeBtn;

    @BindView(R.id.end_time)
    Button endTimeBtn;

    public interface OnTimePickedListener {
        void onTimePicked(Time startTime, Time endTime);
    }

    public static TimeIntervalPickerFragment newInstance(@StringRes int title, OnTimePickedListener timePickedListener) {
        return newInstance(title, Time.now(), Time.now(), timePickedListener);
    }


    public static TimeIntervalPickerFragment newInstance(@StringRes int title, Time startTime, Time endTime, OnTimePickedListener timePickedListener) {
        TimeIntervalPickerFragment fragment = new TimeIntervalPickerFragment();
        if(startTime == null) {
            startTime = Time.now();
        }

        if(endTime == null) {
            endTime = Time.now();
        }
        Bundle args = new Bundle();
        args.putInt(START_MINUTES_AFTER_MIDNIGHT, startTime.toMinuteOfDay());
        args.putInt(END_MINUTES_AFTER_MIDNIGHT, endTime.toMinuteOfDay());
        args.putInt(TITLE, title);
        fragment.setArguments(args);
        fragment.timePickedListener = timePickedListener;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            startTime = Time.of(getArguments().getInt(START_MINUTES_AFTER_MIDNIGHT));
            endTime = Time.of(getArguments().getInt(END_MINUTES_AFTER_MIDNIGHT));
            title = getArguments().getInt(TITLE);
        }
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_time_interval_picker, null);
        unbinder = ButterKnife.bind(this, view);

        startTimeBtn.setText(startTime.toString());
        endTimeBtn.setText(endTime.toString());

        startTimeBtn.setOnClickListener(view1 -> {
            TimePickerFragment fragment = TimePickerFragment.newInstance(false, startTime, time -> {
                startTime = time;
                startTimeBtn.setText(startTime.toString());
            });
            fragment.show(getFragmentManager());
        });

        endTimeBtn.setOnClickListener(view1 -> {
            TimePickerFragment fragment = TimePickerFragment.newInstance(false, startTime, time -> {
                endTime = time;
                endTimeBtn.setText(endTime.toString());
            });
            fragment.show(getFragmentManager());
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(R.drawable.logo)
                .setView(view)
                .setTitle(title)
                .setPositiveButton(getString(R.string.help_dialog_ok), (dialog, which) -> {
                    timePickedListener.onTimePicked(startTime, endTime);
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {

                });
        return builder.create();
    }

    @Override
    public void onDestroy() {
        unbinder.unbind();
        super.onDestroy();
    }

    public void show(FragmentManager fragmentManager) {
        show(fragmentManager, TAG);
    }
}
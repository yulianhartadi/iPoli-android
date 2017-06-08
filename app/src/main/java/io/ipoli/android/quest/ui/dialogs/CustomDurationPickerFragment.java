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
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.utils.Time;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 6/7/17.
 */

public class CustomDurationPickerFragment extends DialogFragment {
    private static final String TAG = "custom-duration-picker-dialog";
    private static final String DURATION = "duration";
    public static final int MAX_DIGIT_COUNT = 3;
    public static final int HOUR_MINUTES = 60;
    public static final String MAX_DURATION_TEXT = "400";

    @BindView(R.id.custom_duration_hours)
    TextView hoursView;

    @BindView(R.id.custom_duration_minutes)
    TextView minutesView;

    @BindView(R.id.custom_duration_delete_digit)
    ImageButton delete;

    private String durationText = "";

    private OnDurationPickedListener durationPickedListener;

    private int duration;
    private Unbinder unbinder;

    public static CustomDurationPickerFragment newInstance(OnDurationPickedListener durationPickedListener) {
        return newInstance(-1, durationPickedListener);
    }

    public static CustomDurationPickerFragment newInstance(int duration, OnDurationPickedListener durationPickedListener) {
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
        View titleView = inflater.inflate(R.layout.custom_duration_dialog_header, null);
        unbinder = ButterKnife.bind(this, titleView);

        initDurationText();
        setHoursAndMinutes();

        delete.setOnClickListener(v -> {
            if (durationText.length() == 1) {
                durationText = "";
            } else if (durationText.length() > 1) {
                durationText = durationText.substring(0, durationText.length() - 1);
            }
            setHoursAndMinutes();
        });


        View view = inflater.inflate(R.layout.fragment_custom_duration_picker, null);
        ViewGroup digitsContainer = (ViewGroup) view.findViewById(R.id.custom_duration_digits_container);

        for (int i = 0; i < digitsContainer.getChildCount(); i++) {
            View digitView = digitsContainer.getChildAt(i);
            View.OnClickListener digitClickListener = v -> {
                if (durationText.length() >= MAX_DIGIT_COUNT) {
                    return;
                }

                if (getDuration(durationText + v.getTag()) > Time.h2Min(Constants.MAX_QUEST_DURATION_HOURS)) {
                    durationText = MAX_DURATION_TEXT;
                    Toast.makeText(getContext(), "More than 4", Toast.LENGTH_SHORT).show();
                } else {
                    durationText += v.getTag();

                }
                setHoursAndMinutes();
            };
            digitView.setOnClickListener(digitClickListener);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(R.drawable.logo)
                .setCustomTitle(titleView)
                .setView(view)
                .setPositiveButton(getString(R.string.help_dialog_ok), (dialog, which) -> {
                    int duration = getDuration(durationText);
                    if (duration > Time.h2Min(Constants.MAX_QUEST_DURATION_HOURS)) {
                        return;
                    }
                    durationPickedListener.onDurationPicked(duration);
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {

                });
        return builder.create();

    }

    private void initDurationText() {
        if (duration == -1) {
            durationText = "";
            return;
        }

        int hours = duration / HOUR_MINUTES;
        int minutes = duration % HOUR_MINUTES;
        if (hours == 0 && minutes == 0) {
            durationText = "";
            return;
        }

        durationText = String.valueOf(hours);
        if (minutes < 10) {
            durationText += "0" + String.valueOf(minutes);
        } else {
            durationText += String.valueOf(minutes);
        }
    }

    private int getDuration(String durationString) {
        for (int i = durationString.length(); i < MAX_DIGIT_COUNT; i++) {
            durationString = "0" + durationString;
        }
        int hours = Integer.parseInt(durationString.substring(0, 1));
        int minutes = Integer.parseInt(durationString.substring(1));

        return hours * HOUR_MINUTES + minutes;
    }

    private void setHoursAndMinutes() {
        if (durationText.length() > MAX_DIGIT_COUNT) {
            return;
        }

        String hours = "0";
        String minutes = "0";

        if (durationText.isEmpty()) {
            minutes += "0";
        } else if (durationText.length() == 1) {
            minutes += durationText;
        } else if (durationText.length() == 2) {
            minutes = durationText;
        } else if (durationText.length() == 3) {
            hours = durationText.substring(0, 1);
            minutes = durationText.substring(1);
        }

        hoursView.setText(hours);
        minutesView.setText(minutes);
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

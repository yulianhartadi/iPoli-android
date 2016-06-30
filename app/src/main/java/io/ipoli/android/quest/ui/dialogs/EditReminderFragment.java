package io.ipoli.android.quest.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.ipoli.android.R;
import io.ipoli.android.quest.data.Reminder;
import io.ipoli.android.quest.ui.formatters.ReminderTimeFormatter;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 6/14/16.
 */
public class EditReminderFragment extends DialogFragment {
    private static final String TAG = "edit-reminder-dialog";
    private static final String REMINDER = "reminder";

    enum TimeType {
        MINUTES(1), HOURS(60), DAYS(TimeUnit.DAYS.toMinutes(1)), WEEKS(TimeUnit.DAYS.toMinutes(7));

        private final long minutes;

        TimeType(long minutes) {
            this.minutes = minutes;
        }

        public long getMinutes() {
            return minutes;
        }
    }

    @BindView(R.id.reminder_message)
    TextInputEditText message;

    @BindView(R.id.reminder_predefined_times)
    Spinner predefinedTimesView;

    @BindView(R.id.reminder_custom_time_container)
    ViewGroup customTimeContainer;

    @BindView(R.id.reminder_custom_time_value)
    TextInputEditText customTimeValue;

    @BindView(R.id.reminder_custom_time_offset_type)
    Spinner customTimeTypesView;

    private Reminder reminder;
    private boolean isCustom = false;

    int[] minutesBefore = new int[] {10, 15, 30, 60};

    private OnReminderCreatedListener reminderCreatedListener;
    private Unbinder unbinder;

    public interface OnReminderCreatedListener {
        void onReminderCreated(Reminder reminder);
    }

    public static EditReminderFragment newInstance(Reminder reminder, OnReminderCreatedListener reminderCreatedListener) {
        EditReminderFragment fragment = new EditReminderFragment();
        Bundle args = new Bundle();
        if (reminder != null) {
            args.putString(REMINDER, new Gson().toJson(reminder));
        }
        fragment.setArguments(args);
        fragment.reminderCreatedListener = reminderCreatedListener;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String reminderJson = getArguments().getString(REMINDER);
            if (!TextUtils.isEmpty(reminderJson)) {
                reminder = new Gson().fromJson(reminderJson, Reminder.class);
            } else {
                reminder = new Reminder();
            }
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_edit_reminder, null);
        unbinder = ButterKnife.bind(this, view);

        initPredefinedTimes();

        initCustomTimes();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(R.drawable.logo)
                .setTitle(R.string.quest_reminders_question)
                .setView(view)
                .setPositiveButton(getString(R.string.help_dialog_ok), (dialog, which) -> {
                    long minutes = 0;
                    if(isCustom) {
                        int value = Integer.valueOf(customTimeValue.getText().toString());
                        long typeMinutes = TimeType.values()[customTimeTypesView.getSelectedItemPosition()].getMinutes();
                        minutes = value * typeMinutes;
                    } else {
                        minutes = minutesBefore[predefinedTimesView.getSelectedItemPosition()];
                    }

                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {

                })
                .setNeutralButton(R.string.none, (dialogInterface, i) -> {

                });
        return builder.create();

    }

    private void initCustomTimes() {
        List<String> times = new ArrayList<>();
        for(TimeType type : TimeType.values()) {
            times.add(type.name().toLowerCase() + " before");
        }
        ArrayAdapter<String> customTimeTypesAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, times);
        customTimeTypesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        customTimeTypesView.setAdapter(customTimeTypesAdapter);
    }

    private void initPredefinedTimes() {

        List<String> predefinedTimes = new ArrayList<>();
        for(int i = 0; i < minutesBefore.length; i ++) {
            predefinedTimes.add(ReminderTimeFormatter.formatMinutesBeforeReadable(minutesBefore[i]));
        }
        predefinedTimes.add("Custom");

        ArrayAdapter<String> predefinedTimesAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, predefinedTimes);
        predefinedTimesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        predefinedTimesView.setAdapter(predefinedTimesAdapter);
        predefinedTimesView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if(position == predefinedTimesAdapter.getCount() - 1) {
                    predefinedTimesView.setVisibility(View.GONE);
                    customTimeContainer.setVisibility(View.VISIBLE);
                    isCustom = true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    public void show(FragmentManager fragmentManager) {
        show(fragmentManager, TAG);
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
        super.onDestroyView();
    }


}

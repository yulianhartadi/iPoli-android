package io.ipoli.android.quest.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import butterknife.Unbinder;
import io.ipoli.android.R;
import io.ipoli.android.app.App;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/13/16.
 */
public class RecurrencePickerFragment extends DialogFragment {

    private static final String TAG = "recurrence-picker-dialog";
    public static final int FREQUENCY_DAILY = 0;
    public static final int FREQUENCY_WEEKLY = 1;
    public static final int FREQUENCY_MONTHLY = 2;

    @Inject
    Bus eventBus;

    @BindView(R.id.recurrence_frequency)
    Spinner recurrenceFrequency;

    @BindView(R.id.day_of_week_container)
    ViewGroup dayOfWeekContainer;

    @BindView(R.id.day_of_month_container)
    ViewGroup dayOfMonthContainer;

    @BindView(R.id.day_of_month)
    Spinner dayOfMonth;

    @BindView(R.id.recurrence_until)
    Button until;

    private Unbinder unbinder;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getAppComponent(getActivity()).inject(this);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = inflater.inflate(R.layout.fragment_recurrence_picker, null);
        unbinder = ButterKnife.bind(this, view);

        recurrenceFrequency.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, new String[]{"Daily", "Weekly", "Monthly"}));
        recurrenceFrequency.setSelection(0, false);

        List<String> daysOfMonth = new ArrayList<>();
        for (int i = 1; i <= 31; i++) {
            daysOfMonth.add(String.valueOf(i));
        }
        dayOfMonth.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, daysOfMonth));

        builder.setView(view)
                .setIcon(R.drawable.logo)
                .setTitle("Pick repeating pattern")
                .setPositiveButton(getString(R.string.done), (dialog, which) -> {

                })
                .setNegativeButton(getString(R.string.cancel), null)
                .setNeutralButton(getString(R.string.do_not_repeat), (dialog, which) -> {
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

    @OnItemSelected(R.id.recurrence_frequency)
    public void onFrequencySelected(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case FREQUENCY_DAILY:
                dayOfWeekContainer.setVisibility(View.GONE);
                dayOfMonthContainer.setVisibility(View.GONE);
                break;
            case FREQUENCY_WEEKLY:
                dayOfWeekContainer.setVisibility(View.VISIBLE);
                dayOfMonthContainer.setVisibility(View.GONE);
                break;
            case FREQUENCY_MONTHLY:
                dayOfWeekContainer.setVisibility(View.GONE);
                dayOfMonthContainer.setVisibility(View.VISIBLE);
                break;

        }
    }

    @OnItemSelected(R.id.day_of_month)
    public void onDayOfMonthSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.d("DayOfMonthSel", position + " ");
    }

    @OnClick(R.id.recurrence_until)
    public void onUntilTapped() {
        DatePickerFragment.newInstance().show(getFragmentManager());
    }
}

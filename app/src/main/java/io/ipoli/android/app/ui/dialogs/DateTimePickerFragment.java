package io.ipoli.android.app.ui.dialogs;

import android.app.Dialog;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import org.threeten.bp.LocalDate;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.ipoli.android.R;
import io.ipoli.android.app.ui.formatters.DateFormatter;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.Time;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 7/1/17.
 */

public class DateTimePickerFragment extends DialogFragment {

    private static final String TAG = "date-time-picker-dialog";
    private static final int DATE_PICKER_TAB = 0;
    private static final int TIME_PICKER_TAB = 1;

    private static final String DATE = "year";
    private static final String MINUTES_AFTER_MIDNIGHT = "minutes_after_midnight";
    private static final String DISABLE_PAST_DAY_SELECTION = "disable_past_day_selection";
    private static final String USE_24_HOUR_FORMAT = "use_24_hour_format";

    @BindView(R.id.pickers_tabs)
    TabLayout tabLayout;

    @BindView(R.id.date_picker)
    DatePicker datePicker;

    @BindView(R.id.time_picker)
    TimePicker timePicker;

    private Unbinder unbinder;

    private LocalDate date;
    private Time time;
    private boolean use24HourFormat = false;
    private OnDateTimePickedListener dateTimePickerListener;
    private boolean disablePastDaySelection;

    public interface OnDateTimePickedListener {
        void onDateTimePicked(LocalDate date, Time time);
    }

    public static DateTimePickerFragment newInstance(boolean use24HourFormat, OnDateTimePickedListener listener) {
        return newInstance(null, null, use24HourFormat, true, listener);
    }


    public static DateTimePickerFragment newInstance(LocalDate date, Time time, boolean use24HourFormat, boolean disablePastDaySelection, OnDateTimePickedListener listener) {
        DateTimePickerFragment fragment = new DateTimePickerFragment();
        Bundle args = new Bundle();
        if (date != null) {
            args.putLong(DATE, DateUtils.toMillis(date));
        }
        if (time != null) {
            args.putInt(MINUTES_AFTER_MIDNIGHT, time.toMinuteOfDay());
        }
        args.putBoolean(DISABLE_PAST_DAY_SELECTION, disablePastDaySelection);
        args.putBoolean(USE_24_HOUR_FORMAT, use24HourFormat);
        fragment.setArguments(args);
        fragment.dateTimePickerListener = listener;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(DATE)) {
            date = DateUtils.fromMillis(getArguments().getLong(DATE));
        } else {
            date = LocalDate.now();
        }

        if (getArguments().containsKey(MINUTES_AFTER_MIDNIGHT)) {
            time = Time.of(getArguments().getInt(MINUTES_AFTER_MIDNIGHT));
        } else {
            time = Time.now();
        }

        if (getArguments().containsKey(USE_24_HOUR_FORMAT)) {
            use24HourFormat = getArguments().getBoolean(USE_24_HOUR_FORMAT);
        } else {
            use24HourFormat = DateFormat.is24HourFormat(getContext());
        }

        if (getArguments().containsKey(DISABLE_PAST_DAY_SELECTION)) {
            disablePastDaySelection = getArguments().getBoolean(DISABLE_PAST_DAY_SELECTION);
        } else {
            disablePastDaySelection = true;
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_date_time_picker, null);
        unbinder = ButterKnife.bind(this, view);

        View headerView = initHeaderView();
        initTabs();
        initDatePicker();
        initTimePicker();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(R.drawable.logo)
                .setCustomTitle(headerView)
                .setView(view)
                .setPositiveButton(getString(R.string.help_dialog_ok), (dialog, which) ->
                        dateTimePickerListener.onDateTimePicked(date, time))
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                });
        return builder.create();
    }

    private void initTimePicker() {
        tabLayout.getTabAt(TIME_PICKER_TAB).setText(time.toString());
        timePicker.setOnTimeChangedListener((v, hourOfDay, minute) -> {
            time = Time.at(hourOfDay, minute);
            tabLayout.getTabAt(TIME_PICKER_TAB).setText(time.toString(use24HourFormat));
        });
    }

    private void initDatePicker() {
        tabLayout.getTabAt(DATE_PICKER_TAB).setText(DateFormatter.formatWithoutYearSimple(date));
        datePicker.init(date.getYear(), date.getMonth().getValue() - 1, date.getDayOfMonth(), (view, year1, monthOfYear, dayOfMonth) -> {
            date = LocalDate.of(year1, monthOfYear + 1, dayOfMonth);
            tabLayout.getTabAt(DATE_PICKER_TAB).setText(DateFormatter.formatWithoutYearSimple(date));
        });
        if (disablePastDaySelection) {
            datePicker.setMinDate(System.currentTimeMillis() - 1000);
        }
    }

    private void initTabs() {


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == DATE_PICKER_TAB) {
                    timePicker.setVisibility(View.INVISIBLE);
                    datePicker.setVisibility(View.VISIBLE);
                } else {
                    timePicker.setVisibility(View.VISIBLE);
                    datePicker.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                //intentional
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                //intentional
            }
        });
    }

    @NonNull
    private View initHeaderView() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View headerView = inflater.inflate(R.layout.fancy_dialog_header, null);

        ViewGroup dialogContainer = (ViewGroup) headerView.findViewById(R.id.fancy_dialog_container);
        dialogContainer.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorAccent));

        TextView dialogTitle = (TextView) headerView.findViewById(R.id.fancy_dialog_title);
        dialogTitle.setText(R.string.date_time_picker_title);

        ImageView image = (ImageView) headerView.findViewById(R.id.fancy_dialog_image);
        image.setImageResource(R.drawable.ic_sandclock_white_24dp);

        GradientDrawable drawable = (GradientDrawable) image.getBackground();
        drawable.setColor(ContextCompat.getColor(getContext(), R.color.md_red_A700));
        return headerView;
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

package io.ipoli.android.app.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.SyncAndroidCalendarProvider;
import io.ipoli.android.app.adapters.AndroidCalendarAdapter;
import io.ipoli.android.app.ui.viewmodels.AndroidCalendarViewModel;
import io.ipoli.android.quest.data.Category;
import me.everything.providers.android.calendar.Calendar;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 */
public class AndroidCalendarsPickerFragment extends DialogFragment {

    private static final String TAG = "android-calendars-picker-dialog";
    private static final String TITLE = "title";
    private static final String PREDEFINED_CALENDARS_KEY = "predefined-calendars";

    @Inject
    SyncAndroidCalendarProvider syncAndroidCalendarProvider;

    @Inject
    ObjectMapper objectMapper;

    @BindView(R.id.calendar_list)
    RecyclerView calendarList;

    private OnCalendarsPickedListener calendarsPickedListener;

    private Map<Long, Category> preSelectedCalendars;

    @StringRes
    private int title;
    private Unbinder unbinder;

    public static AndroidCalendarsPickerFragment newInstance(@StringRes int title, OnCalendarsPickedListener listener) {
        return newInstance(title, new HashMap<>(), listener);
    }

    public static AndroidCalendarsPickerFragment newInstance(@StringRes int title, Map<Long, Category> selectedCalendars, OnCalendarsPickedListener listener) {
        AndroidCalendarsPickerFragment fragment = new AndroidCalendarsPickerFragment();
        Bundle args = new Bundle();
        args.putInt(TITLE, title);
        if(selectedCalendars != null && !selectedCalendars.isEmpty()) {
            try {
                args.putString(PREDEFINED_CALENDARS_KEY, new ObjectMapper().writeValueAsString(selectedCalendars));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Can't write selected calendars Map as JSON", e);
            }
        }
        fragment.setArguments(args);
        fragment.calendarsPickedListener = listener;
        return fragment;
    }

    public interface OnCalendarsPickedListener {
        void onCalendarsPicked(Map<Long, Category> selectedCalendars);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getAppComponent(getContext()).inject(this);
        Bundle arguments = getArguments();
        title = arguments.getInt(TITLE);
        preSelectedCalendars = new HashMap<>();
        if(arguments.containsKey(PREDEFINED_CALENDARS_KEY)) {
            try {
                TypeReference<Map<Long, Category>> mapTypeReference = new TypeReference<Map<Long, Category>>() {
                };
                preSelectedCalendars = objectMapper.readValue(arguments.getString(PREDEFINED_CALENDARS_KEY), mapTypeReference);
            } catch (IOException e) {
                throw new RuntimeException("Can't convert JSON to Map " + arguments.getString(PREDEFINED_CALENDARS_KEY), e);
            }
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_calendar_picker, null);
        unbinder = ButterKnife.bind(this, view);

        List<AndroidCalendarViewModel> viewModels = new ArrayList<>();
        List<Calendar> calendars = syncAndroidCalendarProvider.getAndroidCalendars();
        for (Calendar c : calendars) {
            boolean selected = false;
            Category category = Category.PERSONAL;
            if (preSelectedCalendars.containsKey(c.id)) {
                selected = true;
                category = preSelectedCalendars.get(c.id);
            }
            viewModels.add(new AndroidCalendarViewModel(c.id, c.displayName, category, selected));
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        calendarList.setLayoutManager(layoutManager);
        AndroidCalendarAdapter adapter = new AndroidCalendarAdapter(getContext(), viewModels);
        calendarList.setAdapter(adapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(R.drawable.logo)
                .setView(view)
                .setTitle(title)
                .setPositiveButton(R.string.help_dialog_ok, (dialog, which) ->
                        calendarsPickedListener.onCalendarsPicked(adapter.getSelectedCalendars()))
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

package io.ipoli.android.app.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

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
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.quest.data.Category;
import me.everything.providers.android.calendar.Calendar;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 */
public class AndroidCalendarsPickerFragment extends DialogFragment {

    private static final String TAG = "android-calendars-picker-dialog";
    private static final String TITLE = "title";

    @Inject
    SyncAndroidCalendarProvider syncAndroidCalendarProvider;

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
        fragment.setArguments(args);
        fragment.preSelectedCalendars = selectedCalendars;
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
        title = getArguments().getInt(TITLE);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_calendar_picker, null);
        unbinder = ButterKnife.bind(this, view);

        List<CalendarViewModel> viewModels = new ArrayList<>();
        viewModels.add(new CalendarViewModel(1L, "Polina Zhelyazkova", Category.PERSONAL, true));
        viewModels.add(new CalendarViewModel(2L, "Vihar calendar", Category.PERSONAL, false));
        viewModels.add(new CalendarViewModel(3L, "Holidays", Category.PERSONAL, false));
        viewModels.add(new CalendarViewModel(4L, "Birthdays", Category.PERSONAL, false));
        List<Calendar> calendars = syncAndroidCalendarProvider.getAndroidCalendars();
        for (Calendar c : calendars) {
            boolean selected = false;
            Category category = Category.PERSONAL;
            if (preSelectedCalendars.containsKey(c.id)) {
                selected = true;
                category = preSelectedCalendars.get(c.id);
            }
            viewModels.add(new CalendarViewModel(c.id, c.displayName, category, selected));
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        calendarList.setLayoutManager(layoutManager);
        CalendarAdapter adapter = new CalendarAdapter(viewModels);
        calendarList.setAdapter(adapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(R.drawable.logo)
                .setView(view)
                .setTitle(title)
                .setPositiveButton(R.string.help_dialog_ok, (dialog, which) -> {
                    List<CalendarViewModel> selectedCalendarViewModels = adapter.getSelectedCalendars();
                    Map<Long, Category> selectedCalendars = new HashMap<>();
                    for (CalendarViewModel vm : selectedCalendarViewModels) {
                        selectedCalendars.put(vm.id, vm.category);
                    }
                    calendarsPickedListener.onCalendarsPicked(selectedCalendars);
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

    private class CalendarViewModel {
        private Long id;
        private String name;
        private Category category;
        private boolean isSelected;

        public CalendarViewModel(Long id, String name, Category category, boolean isSelected) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.isSelected = isSelected;
        }

        public void select() {
            isSelected = true;
        }

        public void deselect() {
            isSelected = false;
        }
    }

    public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.ViewHolder> {
        private List<CalendarViewModel> viewModels;

        public CalendarAdapter(List<CalendarViewModel> viewModels) {
            this.viewModels = viewModels;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.pick_calendar_item, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(CalendarAdapter.ViewHolder holder, int position) {
            final CalendarViewModel vm = viewModels.get(holder.getAdapterPosition());

            holder.name.setText(vm.name);

            Category category = vm.category;

            CategoryAdapter adapter = new CategoryAdapter(getContext(), R.layout.category_spinner_item, Category.values());
            holder.categories.setAdapter(adapter);
            holder.categories.setSelection(category.ordinal(), false);
            holder.categories.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    vm.category = Category.values()[position];
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            holder.check.setOnCheckedChangeListener(null);
            holder.check.setChecked(vm.isSelected);
            holder.check.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    vm.select();
                } else {
                    vm.deselect();
                }
            });
            holder.itemView.setOnClickListener(view -> {
                CheckBox cb = holder.check;
                cb.setChecked(!cb.isChecked());

            });
        }

        @Override
        public int getItemCount() {
            return viewModels.size();
        }

        public List<CalendarViewModel> getSelectedCalendars() {
            List<CalendarViewModel> selectedCalendars = new ArrayList<>();
            for (CalendarViewModel vm : viewModels) {
                if (vm.isSelected) {
                    selectedCalendars.add(vm);
                }
            }
            return selectedCalendars;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.calendar_check)
            CheckBox check;

            @BindView(R.id.calendar_name)
            TextView name;

            @BindView(R.id.calendar_category_container)
            Spinner categories;

            public ViewHolder(View v) {
                super(v);
                ButterKnife.bind(this, v);
            }
        }
    }

    private class CategoryAdapter extends ArrayAdapter<Category> {

        @NonNull
        private final Category[] categories;


        public CategoryAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull Category[] categories) {
            super(context, resource, categories);
            this.categories = categories;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            return populateView(categories[position], convertView, false);
        }

        @Override
        public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            return populateView(categories[position], convertView, true);
        }

        @NonNull
        private View populateView(Category category, @Nullable View convertView, boolean isNameVisible) {
            View view = convertView;
            if (view == null) {
                LayoutInflater layoutInflater = LayoutInflater.from(getContext());
                view = layoutInflater.inflate(R.layout.category_spinner_item, null);
            }

            TextView name = (TextView) view.findViewById(R.id.category_name);
            if (isNameVisible) {
                name.setVisibility(View.VISIBLE);
                name.setText(StringUtils.capitalize(category.name()));
            } else {
                name.setVisibility(View.GONE);
            }

            ImageView image = (ImageView) view.findViewById(R.id.category_image);
            image.setImageResource(category.colorfulImage);
            return view;
        }
    }

}

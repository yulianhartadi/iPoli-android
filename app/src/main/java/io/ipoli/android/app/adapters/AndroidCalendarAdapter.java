package io.ipoli.android.app.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.app.ui.viewmodels.AndroidCalendarViewModel;
import io.ipoli.android.quest.data.Category;

public class AndroidCalendarAdapter extends RecyclerView.Adapter<AndroidCalendarAdapter.ViewHolder> {
    private final Context context;
    private List<AndroidCalendarViewModel> viewModels;

    public AndroidCalendarAdapter(Context context, List<AndroidCalendarViewModel> viewModels) {
        this.context = context;
        this.viewModels = viewModels;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.pick_calendar_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(AndroidCalendarAdapter.ViewHolder holder, int position) {
        final AndroidCalendarViewModel vm = viewModels.get(holder.getAdapterPosition());

        holder.name.setText(vm.getName());

        Category category = vm.getCategory();

        CategoryAdapter adapter = new CategoryAdapter(context, R.layout.category_spinner_item, Category.values());
        holder.categories.setAdapter(adapter);
        holder.categories.setSelection(category.ordinal(), false);
        holder.categories.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                vm.setCategory(Category.values()[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        holder.check.setOnCheckedChangeListener(null);
        holder.check.setChecked(vm.isSelected());
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

    public List<AndroidCalendarViewModel> getSelectedCalendarList() {
        List<AndroidCalendarViewModel> selectedCalendars = new ArrayList<>();
        for (AndroidCalendarViewModel vm : viewModels) {
            if (vm.isSelected()) {
                selectedCalendars.add(vm);
            }
        }
        return selectedCalendars;
    }

    public Map<Long, Category> getSelectedCalendars() {
        Map<Long, Category> selectedCalendars = new HashMap<>();
        for (AndroidCalendarViewModel vm : viewModels) {
            if(vm.isSelected()) {
                selectedCalendars.put(vm.getId(), vm.getCategory());
            }
        }
        return selectedCalendars;
    }

    public void setViewModels(List<AndroidCalendarViewModel> viewModels) {
        this.viewModels = viewModels;
        notifyDataSetChanged();
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
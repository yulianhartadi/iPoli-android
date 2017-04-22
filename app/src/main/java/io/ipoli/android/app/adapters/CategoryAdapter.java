package io.ipoli.android.app.adapters;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import io.ipoli.android.R;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.quest.data.Category;

public class CategoryAdapter extends ArrayAdapter<Category> {

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
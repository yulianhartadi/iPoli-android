package io.ipoli.android.app.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.ipoli.android.R;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.app.utils.ViewUtils;
import io.ipoli.android.quest.data.Category;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 6/27/16.
 */
public class CategoryView extends LinearLayout {
    private static final int DEFAULT_GAP_DP = 12;
    private float gap;

    @ColorInt
    private int categoryNameColor;
    private List<OnCategoryChangedListener> categoryChangedListeners = new ArrayList<>();
    private View view;

    public interface OnCategoryChangedListener {
        void onCategoryChanged(Category category);
    }

    private TextView categoryName;

    private Category category;

    public CategoryView(Context context) {
        super(context);
        gap = ViewUtils.dpToPx(DEFAULT_GAP_DP, getResources());
        if (!isInEditMode()) {
            initUI(context);
        }
    }

    public CategoryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.CategoryView,
                0, 0);

        try {
            gap = typedArray.getDimensionPixelSize(R.styleable.CategoryView_category_gap, (int) ViewUtils.dpToPx(DEFAULT_GAP_DP, getResources()));
            categoryNameColor = typedArray.getColor(R.styleable.CategoryView_categoryName_color, ContextCompat.getColor(context, R.color.md_dark_text_87));
        } finally {
            typedArray.recycle();
        }

        if (!isInEditMode()) {
            initUI(context);
        }
    }

    private void initUI(Context context) {
        view = LayoutInflater.from(context).inflate(
                R.layout.layout_category, this);

        categoryName = (TextView) view.findViewById(R.id.category_name);
        categoryName.setTextColor(categoryNameColor);
        LinearLayout categoryContainer = (LinearLayout) view.findViewById(R.id.category_container);
        doChangeCategory(Category.LEARNING);

        final Category[] categories = Category.values();
        for (int i = 0; i < categoryContainer.getChildCount(); i++) {
            final ImageView iv = (ImageView) categoryContainer.getChildAt(i);
            if (i < categoryContainer.getChildCount() - 1) {
                LinearLayout.LayoutParams lp = (LayoutParams) iv.getLayoutParams();
                lp.setMarginEnd((int) gap);
                iv.setLayoutParams(lp);
            }
            GradientDrawable drawable = (GradientDrawable) iv.getBackground();
            drawable.setColor(ContextCompat.getColor(context, categories[i].color500));

            final Category category = categories[i];
            iv.setOnClickListener(view -> {
                removeSelectedCategoryCheck();
                doChangeCategory(category);
            });
        }
    }

    public void changeCategory(Category category) {
        removeSelectedCategoryCheck();
        doChangeCategory(category);
    }

    public Category getSelectedCategory() {
        return category;
    }


    private void doChangeCategory(Category category) {
        this.category = category;
        setSelectedCategory();
        for (OnCategoryChangedListener listener : categoryChangedListeners) {
            listener.onCategoryChanged(category);
        }
    }

    private void setSelectedCategory() {
        getCurrentCategoryImageView().setImageResource(category.whiteImage);
        setCategoryName();
    }

    private void removeSelectedCategoryCheck() {
        getCurrentCategoryImageView().setImageDrawable(null);
    }

    private ImageView getCurrentCategoryImageView() {
        switch (category) {
            case LEARNING:
                return extractImageView(R.id.category_learning);

            case WELLNESS:
                return extractImageView(R.id.category_wellness);

            case PERSONAL:
                return extractImageView(R.id.category_personal);

            case WORK:
                return extractImageView(R.id.category_work);

            case FUN:
                return extractImageView(R.id.category_fun);
        }
        return extractImageView(R.id.category_chores);
    }

    private ImageView extractImageView(int categoryViewId) {
        return (ImageView) view.findViewById(categoryViewId);
    }

    private void setCategoryName() {
        categoryName.setText(StringUtils.capitalize(category.name()));
    }


    public void addCategoryChangedListener(OnCategoryChangedListener listener) {
        categoryChangedListeners.add(listener);
    }

    public void removeCategoryChangedListener(OnCategoryChangedListener listener) {
        categoryChangedListeners.remove(listener);
    }

}

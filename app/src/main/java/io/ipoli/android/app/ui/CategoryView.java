package io.ipoli.android.app.ui;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.Unbinder;
import io.ipoli.android.R;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.quest.Category;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 6/27/16.
 */
public class CategoryView extends LinearLayout {

    //    @BindView(R.id.challenge_category_name)
    TextView categoryName;

    //    @BindView(R.id.challenge_category_container)
    LinearLayout categoryContainer;

    private Unbinder unbinder;
    private Category category;

    public CategoryView(Context context) {
        super(context);
        if (!isInEditMode()) {
            initUI(context);
        }
    }

    public CategoryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            initUI(context);
        }
    }

    private void initUI(Context context) {
        View v = LayoutInflater.from(context).inflate(
                R.layout.layout_category, this);

//        unbinder = ButterKnife.bind(this, v);

        categoryName = (TextView) v.findViewById(R.id.category_name);
        categoryContainer = (LinearLayout) v.findViewById(R.id.category_container);
        changeCategory(Category.LEARNING);

        final Category[] categories = Category.values();
        for (int i = 0; i < categoryContainer.getChildCount(); i++) {
            final ImageView iv = (ImageView) categoryContainer.getChildAt(i);
            GradientDrawable drawable = (GradientDrawable) iv.getBackground();
            drawable.setColor(ContextCompat.getColor(context, categories[i].resLightColor));

            final Category category = categories[i];
            iv.setOnClickListener(view -> {
                removeSelectedCategoryCheck();
                changeCategory(category);
//                eventBus.post(new NewQuestCategoryChangedEvent(category));
            });
        }
    }

    private void changeCategory(Category category) {
        colorLayout(category);
        this.category = category;
        setSelectedCategory();
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
                return extractImageView(R.id.challenge_category_learning);

            case WELLNESS:
                return extractImageView(R.id.challenge_category_wellness);

            case PERSONAL:
                return extractImageView(R.id.challenge_category_personal);

            case WORK:
                return extractImageView(R.id.challenge_category_work);

            case FUN:
                return extractImageView(R.id.challenge_category_fun);
        }
        return extractImageView(R.id.challenge_category_chores);
    }

    private ImageView extractImageView(int categoryViewId) {
        return (ImageView) findViewById(categoryViewId);
    }

    private void setCategoryName() {
        categoryName.setText(StringUtils.capitalize(category.name()));
    }

    private void colorLayout(Category category) {
//        appBar.setBackgroundColor(ContextCompat.getColor(this, category.resLightColor));
//        toolbar.setBackgroundColor(ContextCompat.getColor(this, category.resLightColor));
//        collapsingToolbarLayout.setContentScrimColor(ContextCompat.getColor(this, category.resLightColor));
//        getWindow().setNavigationBarColor(ContextCompat.getColor(this, category.resLightColor));
//        getWindow().setStatusBarColor(ContextCompat.getColor(this, category.resDarkColor));
    }


}

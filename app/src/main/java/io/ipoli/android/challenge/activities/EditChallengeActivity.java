package io.ipoli.android.challenge.activities;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.otto.Bus;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.quest.Category;
import io.ipoli.android.quest.events.NewQuestContextChangedEvent;

public class EditChallengeActivity extends AppCompatActivity {
    @Inject
    Bus eventBus;

    @BindView(R.id.appbar)
    AppBarLayout appBar;

    @BindView(R.id.toolbar_collapsing_container)
    CollapsingToolbarLayout collapsingToolbarLayout;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;

    @BindView(R.id.challenge_category_name)
    TextView categoryName;

    @BindView(R.id.challenge_category_container)
    LinearLayout contextContainer;

    private Category category;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_challenge);
        App.getAppComponent(this).inject(this);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        toolbarTitle.setText(R.string.title_activity_add_challenge);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayShowTitleEnabled(false);
            ab.setDisplayHomeAsUpEnabled(true);
        }

        initContextUI();
    }

    private void initContextUI() {
        changeContext(Category.LEARNING);

        final Category[] ctxs = Category.values();
        for (int i = 0; i < contextContainer.getChildCount(); i++) {
            final ImageView iv = (ImageView) contextContainer.getChildAt(i);
            GradientDrawable drawable = (GradientDrawable) iv.getBackground();
            drawable.setColor(ContextCompat.getColor(this, ctxs[i].resLightColor));

            final Category ctx = ctxs[i];
            iv.setOnClickListener(v -> {
                removeSelectedContextCheck();
                changeContext(ctx);
                eventBus.post(new NewQuestContextChangedEvent(ctx));
            });
        }
    }

    private void changeContext(Category ctx) {
        colorLayout(ctx);
        category = ctx;
        setSelectedContext();
    }

    private void setSelectedContext() {
        getCurrentContextImageView().setImageResource(category.whiteImage);
        setContextName();
    }

    private void removeSelectedContextCheck() {
        getCurrentContextImageView().setImageDrawable(null);
    }

    private ImageView getCurrentContextImageView() {
        String ctxId = "challenge_category_" + category.name().toLowerCase();
        int ctxResId = getResources().getIdentifier(ctxId, "id", getPackageName());
        return (ImageView) findViewById(ctxResId);
    }

    private void setContextName() {
        categoryName.setText(StringUtils.capitalize(category.name()));
    }

    private void colorLayout(Category context) {
        appBar.setBackgroundColor(ContextCompat.getColor(this, context.resLightColor));
        toolbar.setBackgroundColor(ContextCompat.getColor(this, context.resLightColor));
        collapsingToolbarLayout.setContentScrimColor(ContextCompat.getColor(this, context.resLightColor));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, context.resLightColor));
        getWindow().setStatusBarColor(ContextCompat.getColor(this, context.resDarkColor));
    }
}

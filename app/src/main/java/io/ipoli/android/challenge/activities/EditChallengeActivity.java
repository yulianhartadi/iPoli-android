package io.ipoli.android.challenge.activities;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.otto.Bus;

import org.joda.time.LocalDate;

import java.util.Date;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.BaseActivity;
import io.ipoli.android.app.help.HelpDialog;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.challenge.data.Difficulty;
import io.ipoli.android.challenge.ui.dialogs.DifficultyPickerFragment;
import io.ipoli.android.quest.Category;
import io.ipoli.android.quest.events.NewQuestContextChangedEvent;
import io.ipoli.android.quest.ui.dialogs.DatePickerFragment;
import io.ipoli.android.quest.ui.formatters.DateFormatter;

public class EditChallengeActivity extends BaseActivity implements DatePickerFragment.OnDatePickedListener, DifficultyPickerFragment.OnDifficultyPickedListener {
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

    @BindView(R.id.challenge_end_date_value)
    TextView endDateText;

    @BindView(R.id.challenge_difficulty_value)
    TextView difficultyText;

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

        populateEndDate(LocalDate.now().plusDays(Constants.DEFAULT_CHALLENGE_DEADLINE_DAY_OFFSET).toDateTimeAtStartOfDay().toDate());
        populateDifficulty(Difficulty.NORMAL);
    }

    private void initContextUI() {
        changeContext(Category.LEARNING);

        final Category[] categories = Category.values();
        for (int i = 0; i < contextContainer.getChildCount(); i++) {
            final ImageView iv = (ImageView) contextContainer.getChildAt(i);
            GradientDrawable drawable = (GradientDrawable) iv.getBackground();
            drawable.setColor(ContextCompat.getColor(this, categories[i].resLightColor));

            final Category ctx = categories[i];
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_challenge_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
//        menu.findItem(R.id.action_save).setTitle(editMode == EditMode.ADD ? R.string.done : R.string.save);
//        menu.findItem(R.id.action_delete).setVisible(!(editMode == EditMode.ADD || editMode == EditMode.EDIT_NEW_QUEST));
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
//                onSaveTap(EventSource.TOOLBAR);
                return true;
            case R.id.action_delete:
                return true;
            case R.id.action_help:
                HelpDialog.newInstance(R.layout.fragment_help_dialog_add_quest, R.string.help_dialog_add_quest_title, "add_quest").show(getSupportFragmentManager());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.challenge_end_date_container)
    public void onEndDateClicked(View view) {
        DatePickerFragment f = DatePickerFragment.newInstance((Date) endDateText.getTag(), true, false, this);
        f.show(this.getSupportFragmentManager());
    }

    @OnClick(R.id.challenge_difficulty_container)
    public void onDifficultyClicked(View view) {
        DifficultyPickerFragment f = DifficultyPickerFragment.newInstance((Difficulty) difficultyText.getTag(), this);
        f.show(this.getSupportFragmentManager());
    }

    @Override
    public void onDatePicked(Date date) {
        populateEndDate(date);
    }

    private void populateEndDate(Date date) {
        endDateText.setText(DateFormatter.format(date));
        endDateText.setTag(date);
    }

    private void populateDifficulty(Difficulty difficulty) {
        difficultyText.setText(StringUtils.capitalize(difficulty.name()));
        difficultyText.setTag(difficulty);
    }

    @Override
    public void onDifficultyPicked(Difficulty difficulty) {
        populateDifficulty(difficulty);
    }
}

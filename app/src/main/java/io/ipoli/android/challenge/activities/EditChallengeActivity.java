package io.ipoli.android.challenge.activities;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Bus;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.BaseActivity;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.help.HelpDialog;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.challenge.data.Challenge;
import io.ipoli.android.challenge.data.Difficulty;
import io.ipoli.android.challenge.events.NewChallengeEvent;
import io.ipoli.android.challenge.ui.dialogs.DifficultyPickerFragment;
import io.ipoli.android.challenge.ui.dialogs.MultiTextPickerFragment;
import io.ipoli.android.quest.Category;
import io.ipoli.android.quest.events.NewQuestContextChangedEvent;
import io.ipoli.android.quest.generators.CoinsRewardGenerator;
import io.ipoli.android.quest.generators.ExperienceRewardGenerator;
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

    @BindView(R.id.challenge_name)
    TextInputEditText name;

    @BindView(R.id.challenge_category_name)
    TextView categoryName;

    @BindView(R.id.challenge_category_container)
    LinearLayout contextContainer;

    @BindView(R.id.challenge_end_date_value)
    TextView endDateText;

    @BindView(R.id.challenge_difficulty_value)
    TextView difficultyText;

    List<TextView> expectedResultTextViews;

    List<TextView> reasonTextViews;

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

        expectedResultTextViews = new ArrayList<>();
        expectedResultTextViews.add((TextView) findViewById(R.id.challenge_expected_result_1_value));
        expectedResultTextViews.add((TextView) findViewById(R.id.challenge_expected_result_2_value));
        expectedResultTextViews.add((TextView) findViewById(R.id.challenge_expected_result_3_value));

        reasonTextViews = new ArrayList<>();
        reasonTextViews.add((TextView) findViewById(R.id.challenge_reason_1_value));
        reasonTextViews.add((TextView) findViewById(R.id.challenge_reason_2_value));
        reasonTextViews.add((TextView) findViewById(R.id.challenge_reason_3_value));

        populateExpectedResults(new ArrayList<>());
        populateReasons(new ArrayList<>());
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
                onSaveTap(EventSource.TOOLBAR);
                return true;
            case R.id.action_delete:
                return true;
            case R.id.action_help:
                HelpDialog.newInstance(R.layout.fragment_help_dialog_add_quest, R.string.help_dialog_add_quest_title, "add_quest").show(getSupportFragmentManager());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onSaveTap(EventSource toolbar) {
        if (StringUtils.isEmpty(name.getText().toString())) {
            Toast.makeText(this, R.string.add_challenge_name, Toast.LENGTH_LONG).show();
            return;
        }

        if (StringUtils.isEmpty((String) expectedResultTextViews.get(0).getTag())) {
            Toast.makeText(this, R.string.add_challenge_expected_result, Toast.LENGTH_LONG).show();
            return;
        }

        if (StringUtils.isEmpty((String) reasonTextViews.get(0).getTag())) {
            Toast.makeText(this, R.string.add_challenge_reason, Toast.LENGTH_LONG).show();
            return;
        }

        Challenge challenge = new Challenge(name.getText().toString());
        challenge.setCategory(category);

        challenge.setExpectedResult1((String) expectedResultTextViews.get(0).getTag());
        challenge.setExpectedResult2((String) expectedResultTextViews.get(1).getTag());
        challenge.setExpectedResult3((String) expectedResultTextViews.get(2).getTag());

        challenge.setReason1((String) reasonTextViews.get(0).getTag());
        challenge.setReason2((String) reasonTextViews.get(1).getTag());
        challenge.setReason3((String) reasonTextViews.get(2).getTag());

        challenge.setEndDate(DateUtils.getDate((Date) endDateText.getTag()));
        challenge.setDifficulty(((Difficulty) difficultyText.getTag()).getValue());

        challenge.setExperience(new ExperienceRewardGenerator().generate(challenge));
        challenge.setCoins(new CoinsRewardGenerator().generate(challenge));
        eventBus.post(new NewChallengeEvent(challenge));
        Toast.makeText(this, R.string.challenge_saved, Toast.LENGTH_SHORT).show();
        finish();
    }

    @OnClick(R.id.challenge_expected_results_container)
    public void onExpectedResultsClicked(View view) {
        ArrayList<String> texts = new ArrayList<>();
        for (TextView textView : expectedResultTextViews) {
            texts.add((String) textView.getTag());
        }

        ArrayList<String> hints = new ArrayList<>();
        hints.add("1st result");
        hints.add("2nd result");
        hints.add("3rd result");
        MultiTextPickerFragment f = MultiTextPickerFragment.newInstance(texts, hints, R.string.challenge_expected_results_question, this::populateExpectedResults);
        f.show(getSupportFragmentManager());
    }

    @OnClick(R.id.challenge_reasons_container)
    public void onReasonsClicked(View view) {
        ArrayList<String> texts = new ArrayList<>();
        for (TextView textView : reasonTextViews) {
            texts.add((String) textView.getTag());
        }

        ArrayList<String> hints = new ArrayList<>();
        hints.add("1st reason");
        hints.add("2nd reason");
        hints.add("3rd reason");
        MultiTextPickerFragment f = MultiTextPickerFragment.newInstance(texts, hints, R.string.challenge_reasons_question, this::populateReasons);
        f.show(getSupportFragmentManager());
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

    private void populateExpectedResults(List<String> expectedResults) {
        populateTextList(expectedResults, expectedResultTextViews);
    }

    private void populateReasons(List<String> reasons) {
        populateTextList(reasons, reasonTextViews);
    }

    private void populateTextList(List<String> texts, List<TextView> textViews) {
        List<String> nonEmptyTexts = new ArrayList<>();

        for (String text : texts) {
            if (!StringUtils.isEmpty(text)) {
                nonEmptyTexts.add(text);
            }
        }

        for (TextView textView : textViews) {
            textView.setText("");
            textView.setTag("");
            textView.setVisibility(View.GONE);
        }

        if (nonEmptyTexts.isEmpty()) {
            TextView textView = textViews.get(0);
            textView.setText(R.string.do_not_know);
            textView.setVisibility(View.VISIBLE);
            return;
        }

        for (int i = 0; i < nonEmptyTexts.size(); i++) {
            TextView textView = textViews.get(i);
            String text = nonEmptyTexts.get(i);
            textView.setText(text);
            textView.setTag(text);
            textView.setVisibility(View.VISIBLE);
        }
    }
}

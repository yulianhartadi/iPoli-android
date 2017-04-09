package io.ipoli.android.challenge.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Bus;

import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.activities.BaseActivity;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.ui.CategoryView;
import io.ipoli.android.app.ui.dialogs.DatePickerFragment;
import io.ipoli.android.app.ui.formatters.DateFormatter;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.challenge.data.Challenge;
import io.ipoli.android.challenge.data.Difficulty;
import io.ipoli.android.challenge.events.NewChallengeCategoryChangedEvent;
import io.ipoli.android.challenge.events.NewChallengeEvent;
import io.ipoli.android.challenge.persistence.ChallengePersistenceService;
import io.ipoli.android.challenge.ui.dialogs.DifficultyPickerFragment;
import io.ipoli.android.challenge.ui.dialogs.MultiTextPickerFragment;
import io.ipoli.android.challenge.ui.events.CancelDeleteChallengeEvent;
import io.ipoli.android.challenge.ui.events.DeleteChallengeRequestEvent;
import io.ipoli.android.challenge.ui.events.UpdateChallengeEvent;
import io.ipoli.android.quest.data.Category;

public class EditChallengeActivity extends BaseActivity implements DatePickerFragment.OnDatePickedListener,
        DifficultyPickerFragment.OnDifficultyPickedListener,
        CategoryView.OnCategoryChangedListener {

    enum EditMode {ADD, EDIT}

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
    TextInputEditText nameText;

    @BindView(R.id.challenge_category)
    CategoryView categoryView;

    @BindView(R.id.challenge_end_date_value)
    TextView endDateText;

    @BindView(R.id.challenge_difficulty_value)
    TextView difficultyText;

    List<TextView> expectedResultTextViews;

    List<TextView> reasonTextViews;

    private EditMode editMode;

    @Inject
    ChallengePersistenceService challengePersistenceService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_challenge);
        App.getAppComponent(this).inject(this);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayShowTitleEnabled(false);
            ab.setDisplayHomeAsUpEnabled(true);
        }

        initUI();

        if (getIntent() != null && !TextUtils.isEmpty(getIntent().getStringExtra(Constants.CHALLENGE_ID_EXTRA_KEY))) {
            onEditChallenge();
        } else {
            onAddNewChallenge();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        eventBus.register(this);
    }

    @Override
    public void onPause() {
        eventBus.unregister(this);
        super.onPause();
    }

    private void initUI() {
        categoryView.addCategoryChangedListener(this);

        expectedResultTextViews = new ArrayList<>();
        expectedResultTextViews.add((TextView) findViewById(R.id.challenge_expected_result_1_value));
        expectedResultTextViews.add((TextView) findViewById(R.id.challenge_expected_result_2_value));
        expectedResultTextViews.add((TextView) findViewById(R.id.challenge_expected_result_3_value));

        reasonTextViews = new ArrayList<>();
        reasonTextViews.add((TextView) findViewById(R.id.challenge_reason_1_value));
        reasonTextViews.add((TextView) findViewById(R.id.challenge_reason_2_value));
        reasonTextViews.add((TextView) findViewById(R.id.challenge_reason_3_value));
    }

    private void onAddNewChallenge() {
        editMode = EditMode.ADD;
        toolbarTitle.setText(R.string.title_activity_add_challenge);
        nameText.requestFocus();
        showKeyboard();
        populateExpectedResults(new ArrayList<>());
        populateReasons(new ArrayList<>());
        populateEndDate(LocalDate.now().plusDays(Constants.DEFAULT_CHALLENGE_DEADLINE_DAY_DURATION));
        populateDifficulty(Difficulty.NORMAL);
    }

    private void onEditChallenge() {
        editMode = EditMode.EDIT;
        toolbarTitle.setText(R.string.title_edit_challenge);

        String challengeId = getIntent().getStringExtra(Constants.CHALLENGE_ID_EXTRA_KEY);
        challengePersistenceService.findById(challengeId, challenge -> {

            nameText.setText(challenge.getName());
            nameText.setSelection(challenge.getName().length());
            categoryView.changeCategory(Challenge.getCategory(challenge));
            populateExpectedResults(new ArrayList<>(Arrays.asList(new String[]{
                    challenge.getExpectedResult1(),
                    challenge.getExpectedResult2(),
                    challenge.getExpectedResult3()
            })));
            populateReasons(new ArrayList<>(Arrays.asList(new String[]{
                    challenge.getReason1(),
                    challenge.getReason2(),
                    challenge.getReason3()
            })));
            populateEndDate(challenge.getEndDate());
            populateDifficulty(Difficulty.getByValue(challenge.getDifficulty()));
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_challenge_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_delete).setVisible(editMode == EditMode.EDIT);
        return true;
    }

    @Override
    protected boolean useParentOptionsMenu() {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_save:
                onSaveTap(EventSource.TOOLBAR);
                return true;
            case R.id.action_delete:
                AlertDialog d = new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.dialog_delete_challenge_title))
                        .setMessage(getString(R.string.dialog_delete_challenge_message))
                        .create();
                String challengeId = getIntent().getStringExtra(Constants.CHALLENGE_ID_EXTRA_KEY);
                challengePersistenceService.findById(challengeId, challenge -> {
                    d.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.dialog_yes), (dialogInterface, i) -> {
                        eventBus.post(new DeleteChallengeRequestEvent(challenge, true, EventSource.EDIT_CHALLENGE));
                        Toast.makeText(this, R.string.challenge_with_quests_deleted, Toast.LENGTH_SHORT).show();
                        setResult(Constants.RESULT_REMOVED);
                        finish();
                    });
                    d.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.dialog_no), (dialog, which) -> {
                        eventBus.post(new DeleteChallengeRequestEvent(challenge, false, EventSource.EDIT_CHALLENGE));
                        Toast.makeText(this, R.string.challenge_deleted, Toast.LENGTH_SHORT).show();
                        setResult(Constants.RESULT_REMOVED);
                        finish();
                    });
                    d.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.cancel), (dialogInterface, i) -> {
                        eventBus.post(new CancelDeleteChallengeEvent(challenge, EventSource.EDIT_CHALLENGE));
                    });
                    d.show();
                });
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnEditorAction(R.id.challenge_name)
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        int result = actionId & EditorInfo.IME_MASK_ACTION;
        if (result == EditorInfo.IME_ACTION_DONE) {
            onSaveTap(EventSource.KEYBOARD);
            return true;
        } else {
            return false;
        }
    }

    private void onSaveTap(EventSource source) {
        if (!isChallengeValid()) {
            return;
        }

        if (editMode == EditMode.ADD) {
            addNewChallenge(source);
        } else {
            updateChallenge(source);
        }

        Toast.makeText(this, R.string.challenge_saved, Toast.LENGTH_SHORT).show();
        finish();
    }

    private void addNewChallenge(EventSource source) {
        Challenge challenge = new Challenge(nameText.getText().toString().trim());
        populateChallengeFromForm(challenge);
        eventBus.post(new NewChallengeEvent(challenge, source));
    }

    private void updateChallenge(EventSource source) {
        String challengeId = getIntent().getStringExtra(Constants.CHALLENGE_ID_EXTRA_KEY);
        challengePersistenceService.findById(challengeId, challenge -> {
            challenge.setName(nameText.getText().toString().trim());
            populateChallengeFromForm(challenge);
            eventBus.post(new UpdateChallengeEvent(challenge, source));
        });

    }

    private void populateChallengeFromForm(Challenge challenge) {
        challenge.setCategory(categoryView.getSelectedCategory().name());

        challenge.setExpectedResult1((String) expectedResultTextViews.get(0).getTag());
        challenge.setExpectedResult2((String) expectedResultTextViews.get(1).getTag());
        challenge.setExpectedResult3((String) expectedResultTextViews.get(2).getTag());

        challenge.setReason1((String) reasonTextViews.get(0).getTag());
        challenge.setReason2((String) reasonTextViews.get(1).getTag());
        challenge.setReason3((String) reasonTextViews.get(2).getTag());

        challenge.setEndDate((LocalDate) endDateText.getTag());
        challenge.setDifficulty(((Difficulty) difficultyText.getTag()).getValue());
    }

    private boolean isChallengeValid() {
        if (StringUtils.isEmpty(nameText.getText().toString())) {
            Toast.makeText(this, R.string.add_challenge_name, Toast.LENGTH_LONG).show();
            return false;
        }

        if (StringUtils.isEmpty((String) expectedResultTextViews.get(0).getTag())) {
            Toast.makeText(this, R.string.add_challenge_expected_result, Toast.LENGTH_LONG).show();
            return false;
        }

        if (StringUtils.isEmpty((String) reasonTextViews.get(0).getTag())) {
            Toast.makeText(this, R.string.add_challenge_reason, Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
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
        DatePickerFragment f = DatePickerFragment.newInstance((LocalDate) endDateText.getTag(), true, false, this);
        f.show(this.getSupportFragmentManager());
    }

    @OnClick(R.id.challenge_difficulty_container)
    public void onDifficultyClicked(View view) {
        DifficultyPickerFragment f = DifficultyPickerFragment.newInstance((Difficulty) difficultyText.getTag(), this);
        f.show(this.getSupportFragmentManager());
    }

    @Override
    public void onDatePicked(LocalDate date) {
        populateEndDate(date);
    }

    private void populateEndDate(LocalDate date) {
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

    @Override
    public void onCategoryChanged(Category category) {
        colorLayout(category);
        if (editMode == EditMode.ADD) {
            eventBus.post(new NewChallengeCategoryChangedEvent(category));
        }
    }

    private void colorLayout(Category category) {
        appBar.setBackgroundColor(ContextCompat.getColor(this, category.color500));
        toolbar.setBackgroundColor(ContextCompat.getColor(this, category.color500));
        collapsingToolbarLayout.setContentScrimColor(ContextCompat.getColor(this, category.color500));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, category.color500));
        getWindow().setStatusBarColor(ContextCompat.getColor(this, category.color700));
    }

    @Override
    protected void onDestroy() {
        categoryView.removeCategoryChangedListener(this);
        super.onDestroy();
    }
}

package io.ipoli.android.quest.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Bus;

import org.ocpsoft.prettytime.nlp.PrettyTimeParser;

import java.util.ArrayList;
import java.util.regex.Pattern;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import io.ipoli.android.R;
import io.ipoli.android.app.BaseActivity;
import io.ipoli.android.quest.Quest;
import io.ipoli.android.quest.QuestParser;
import io.ipoli.android.quest.events.NewQuestEvent;
import io.ipoli.android.quest.parsers.DueDateMatcher;
import io.ipoli.android.quest.parsers.DurationMatcher;
import io.ipoli.android.quest.parsers.StartTimeMatcher;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/18/16.
 */
public class AddQuestActivity extends BaseActivity implements AdapterView.OnItemClickListener, TextWatcher {

    @Inject
    Bus eventBus;

    @Bind(R.id.quest_text)
    AutoCompleteTextView questText;

    @Bind(R.id.due_date)
    ImageButton dueDate;

    @Bind(R.id.start_time)
    ImageButton startTime;

    @Bind(R.id.duration)
    ImageButton duration;

    private ArrayAdapter<SpannableString> adapter;
    private SpannableString[] durationAutoCompletes;

    private SpannableString[] dueDateAutoCompletes;

    private SpannableString[] startTimeAutoCompletes;

    private final PrettyTimeParser parser = new PrettyTimeParser();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_quest);

        ButterKnife.bind(this);
        appComponent().inject(this);
        setFinishOnTouchOutside(false);

        disableButtons();

        dueDateAutoCompletes = new SpannableString[]{
                new SpannableString("today"),
                new SpannableString("tomorrow"),
                createSpannableString("on ", "12 Feb"),
                createSpannableString("next ", "Monday"),
                createSpannableString("after ", "3 days"),
                createSpannableString("in ", "2 months")
        };

        startTimeAutoCompletes = new SpannableString[]{
                createSpannableString("at ", "19:30"),
                createSpannableString("at ", "7 pm"),
                new SpannableString("at 9:00"),
                new SpannableString("at 12:00"),
                new SpannableString("at 20:00"),
                new SpannableString("at 22:00")
        };

        durationAutoCompletes = new SpannableString[]{
                new SpannableString("for 10 min"),
                new SpannableString("for 15 min"),
                new SpannableString("for 30 min"),
                new SpannableString("for 1h"),
                new SpannableString("for 1h and 30m"),
                createSpannableString("for ", "5m")
        };

        adapter = new ArrayAdapter<>(this,
                R.layout.add_quest_autocomplete_item, new ArrayList<SpannableString>());
        questText.setAdapter(adapter);
        questText.setOnItemClickListener(this);
        questText.addTextChangedListener(this);
        questText.requestFocus();
    }

    private void disableButtons() {
        duration.setBackgroundResource(R.drawable.circle_disable);
        startTime.setBackgroundResource(R.drawable.circle_disable);
        dueDate.setBackgroundResource(R.drawable.circle_disable);
        duration.setEnabled(false);
        startTime.setEnabled(false);
        dueDate.setEnabled(false);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        SpannableString ss = adapter.getItem(position);
        String selectedText = ss.toString();
        ForegroundColorSpan[] spans = ss.getSpans(0, ss.length(), ForegroundColorSpan.class);
        for (int i = spans.length - 1; i >= 0; i--) {
            ForegroundColorSpan span = spans[i];
            int start = ss.getSpanStart(span);
            int end = ss.getSpanEnd(span);
            if (start > 0) {
                selectedText = selectedText.substring(0, start);
            } else {
                selectedText = selectedText.substring(end, selectedText.length());
            }
        }

        String text = this.questText.getText().toString();
        if (!text.endsWith(" ")) {
            text += " ";
        }
        questText.setText(text + selectedText);
        questText.setThreshold(1000);
        questText.setSelection(this.questText.getText().length());
    }


    @OnClick(R.id.due_date)
    public void onDueDateClick(View v) {
        adapter = new ArrayAdapter<>(this,
                R.layout.add_quest_autocomplete_item, dueDateAutoCompletes);
        questText.setAdapter(adapter);
        questText.setThreshold(1);
        questText.showDropDown();
    }

    @NonNull
    private SpannableString createSpannableString(String text, String hintText) {
        int hintColor = ContextCompat.getColor(this, R.color.md_dark_text_26);
        SpannableString spannableString = new SpannableString(text + hintText);
        spannableString.setSpan(new ForegroundColorSpan(hintColor), spannableString.toString().indexOf(hintText), spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableString;
    }

    @OnClick(R.id.start_time)
    public void onStartTimeClick(View v) {
        adapter = new ArrayAdapter<>(this,
                R.layout.add_quest_autocomplete_item, startTimeAutoCompletes);
        questText.setAdapter(adapter);
        questText.setThreshold(1);
        questText.showDropDown();
    }

    @OnClick(R.id.duration)
    public void onDurationClick(View v) {
        adapter = new ArrayAdapter<>(this,
                R.layout.add_quest_autocomplete_item, durationAutoCompletes);
        questText.setAdapter(adapter);
        questText.setThreshold(1);
        questText.showDropDown();
    }

    @OnClick(R.id.save_quest)
    public void onSaveQuestClick(View v) {
        String text = questText.getText().toString().trim();

        Quest q = new QuestParser(parser).parse(text);
        if (TextUtils.isEmpty(q.getName())) {
            Toast.makeText(this, "Please, add quest name", Toast.LENGTH_LONG).show();
            return;
        }
        eventBus.post(new NewQuestEvent(q.getName(), q.getStartTime(), q.getDuration(), q.getDue()));
        Toast.makeText(this, R.string.quest_added, Toast.LENGTH_SHORT).show();
        finish();
        overridePendingTransition(0, R.anim.slide_down_interpolate);
    }

    @OnEditorAction(R.id.quest_text)
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        int result = actionId & EditorInfo.IME_MASK_ACTION;
        if (result == EditorInfo.IME_ACTION_DONE) {
            onSaveQuestClick(v);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, R.anim.slide_down_interpolate);
    }

    @OnClick(R.id.cancel_save)
    public void onCancelClick(View v) {
        finish();
        overridePendingTransition(0, R.anim.slide_down_interpolate);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        String text = editable.toString();

        Pattern namePattern = Pattern.compile("\\w+\\s", Pattern.CASE_INSENSITIVE);
        if (!namePattern.matcher(text).find()) {
            disableButtons();
            return;
        }

        String matchedDuration = new DurationMatcher().match(text);
        if (!TextUtils.isEmpty(matchedDuration)) {
            text = text.replace(matchedDuration, " ");
            duration.setBackgroundResource(R.drawable.circle_disable);
            duration.setEnabled(false);
        } else {
            duration.setBackgroundResource(R.drawable.circle_normal);
            duration.setEnabled(true);
        }

        String matchedStartTime = new StartTimeMatcher(parser).match(text);
        if (!TextUtils.isEmpty(matchedStartTime)) {
            text = text.replace(matchedStartTime, " ");
            startTime.setBackgroundResource(R.drawable.circle_disable);
            startTime.setEnabled(false);
        } else {
            startTime.setBackgroundResource(R.drawable.circle_normal);
            startTime.setEnabled(true);
        }

        String matchedDueDate = new DueDateMatcher(parser).match(text);
        if (!TextUtils.isEmpty(matchedDueDate)) {
            dueDate.setBackgroundResource(R.drawable.circle_disable);
            dueDate.setEnabled(false);
        } else {
            dueDate.setBackgroundResource(R.drawable.circle_normal);
            dueDate.setEnabled(true);
        }

        if (TextUtils.isEmpty(matchedDueDate)) {
            dueDate.setBackgroundResource(R.drawable.circle_accent);
        } else if (TextUtils.isEmpty(matchedStartTime)) {
            startTime.setBackgroundResource(R.drawable.circle_accent);
        } else if (TextUtils.isEmpty(matchedDuration)) {
            duration.setBackgroundResource(R.drawable.circle_accent);
        }
    }
}

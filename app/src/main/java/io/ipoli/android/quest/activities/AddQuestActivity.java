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
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.Toast;

import com.squareup.otto.Bus;

import org.ocpsoft.prettytime.nlp.PrettyTimeParser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.ipoli.android.R;
import io.ipoli.android.app.BaseActivity;
import io.ipoli.android.quest.events.NewQuestEvent;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/18/16.
 */
public class AddQuestActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    @Inject
    Bus eventBus;

    @Bind(R.id.quest_name)
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

    private final DueDateMatcher dueDateMatcher = new DueDateMatcher(parser);
    private final StartTimeMatcher startTimeMatcher = new StartTimeMatcher(parser);
    private final DurationMatcher durationMatcher = new DurationMatcher();

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

        questText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                String text = s.toString();

                Pattern namePattern = Pattern.compile("\\w+\\s", Pattern.CASE_INSENSITIVE);
                if (!namePattern.matcher(text).find()) {
                    disableButtons();
                    return;
                }

                String matchedDuration = durationMatcher.match(text);
                if (!TextUtils.isEmpty(matchedDuration)) {
                    text = text.replace(matchedDuration, " ");
                    duration.setBackgroundResource(R.drawable.circle_disable);
                    duration.setEnabled(false);
                } else {
                    duration.setBackgroundResource(R.drawable.circle_normal);
                    duration.setEnabled(true);
                }

                String matchedStartTime = startTimeMatcher.match(text);
                if (!TextUtils.isEmpty(matchedStartTime)) {
                    text = text.replace(matchedStartTime, " ");
                    startTime.setBackgroundResource(R.drawable.circle_disable);
                    startTime.setEnabled(false);
                } else {
                    startTime.setBackgroundResource(R.drawable.circle_normal);
                    startTime.setEnabled(true);
                }

                String matchedDueDate = dueDateMatcher.match(text);
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
        });
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

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

        String matchedDurationText = durationMatcher.match(text);
        int duration = durationMatcher.parse(text);
        text = text.replace(matchedDurationText, " ");

        String matchedStartTimeText = startTimeMatcher.match(text);
        Date startTime = startTimeMatcher.parse(text);
        text = text.replace(matchedStartTimeText, " ");

        String matchedDueDateText = dueDateMatcher.match(text);
        Date dueDate = dueDateMatcher.parse(text);
        text = text.replace(matchedDueDateText, " ");

        if (TextUtils.isEmpty(text)) {
            Toast.makeText(this, "Please, add quest name", Toast.LENGTH_LONG).show();
            return;
        }

        String name = text;

        eventBus.post(new NewQuestEvent(name, startTime, duration, dueDate));
        Toast.makeText(this, R.string.quest_added, Toast.LENGTH_SHORT).show();
        finish();
        overridePendingTransition(0, R.anim.slide_down);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, R.anim.slide_down);
    }

    @OnClick(R.id.cancel_save)
    public void onCancelClick(View v) {
        finish();
        overridePendingTransition(0, R.anim.slide_down);
    }

    public interface QuestTextMatcher<R> {
        String match(String text);

        R parse(String text);
    }

    public static class DurationMatcher implements QuestTextMatcher<Integer> {

        private static final String DURATION_PATTERN = " for (\\d{1,3})\\s?(hours|hour|h|minutes|minute|mins|min|m)(?: and (\\d{1,3})\\s?(minutes|minute|mins|min|m))?";
        Pattern pattern = Pattern.compile(DURATION_PATTERN, Pattern.CASE_INSENSITIVE);

        @Override
        public String match(String text) {

            Matcher dm = createMatcher(text);
            if (dm.find()) {
                return dm.group();
            }
            return "";
        }

        @NonNull
        private Matcher createMatcher(String text) {
            return pattern.matcher(text);
        }

        @Override
        public Integer parse(String text) {
            Matcher dm = createMatcher(text);
            if (dm.find()) {
                int fd = Integer.valueOf(dm.group(1));
                String fUnit = dm.group(2);
                int duration = fd;
                if (fUnit.startsWith("h")) {
                    duration = (int) TimeUnit.HOURS.toMinutes(fd);
                }

                if (dm.group(3) != null && dm.group(4) != null) {
                    duration += Integer.valueOf(dm.group(3));
                }
                return duration;
            }
            return -1;
        }
    }

    public static class StartTimeMatcher implements QuestTextMatcher<Date> {

        private static final String PATTERN = " at (\\d{1,2}[:|\\.]?(\\d{2})?\\s?(am|pm)?)";
        private final PrettyTimeParser parser;
        private Pattern pattern = Pattern.compile(PATTERN, Pattern.CASE_INSENSITIVE);

        public StartTimeMatcher(PrettyTimeParser parser) {
            this.parser = parser;
        }

        @Override
        public String match(String text) {
            Matcher m = pattern.matcher(text);
            if (m.find()) {
                return m.group();
            }
            return "";
        }

        @Override
        public Date parse(String text) {
            Matcher stm = pattern.matcher(text);
            if (stm.find()) {
                List<Date> dates = parser.parse(stm.group());
                if (!dates.isEmpty()) {
                    return dates.get(0);
                }
            }
            return null;
        }
    }

    public static class DueDateMatcher implements QuestTextMatcher<Date> {

        private static final String DUE_TODAY_TOMORROW_PATTERN = "today|tomorrow";
        private static final String DUE_MONTH_PATTERN = "(\\son)?\\s(\\d){1,2}(\\s)?(st|th)?\\s(of\\s)?(next month|this month|January|February|March|April|May|June|July|August|September|October|November|December|Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec){1}";
        private static final String DUE_AFTER_IN_PATTERN = "(after|in)\\s\\w+\\s(day|week|month|year)s?";
        private static final String DUE_FROM_NOW_PATTERN = "\\w+\\s(day|week|month|year)s?\\sfrom\\snow";
        private static final String DUE_THIS_NEXT_PATTERN = "(this|next)\\s(Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday|Mon|Tue|Wed|Thur|Fri|Sat|Sun)";
        private static final String DUE_THIS_MONTH_PATTERN = "on\\s?(\\d{1,2})\\s?(st|th)$";

        private static final Pattern[] dueDatePatterns = {
                Pattern.compile(DUE_TODAY_TOMORROW_PATTERN, Pattern.CASE_INSENSITIVE),
                Pattern.compile(DUE_MONTH_PATTERN, Pattern.CASE_INSENSITIVE),
                Pattern.compile(DUE_THIS_NEXT_PATTERN, Pattern.CASE_INSENSITIVE),
                Pattern.compile(DUE_AFTER_IN_PATTERN, Pattern.CASE_INSENSITIVE),
                Pattern.compile(DUE_FROM_NOW_PATTERN, Pattern.CASE_INSENSITIVE)
        };

        private static final Pattern dueThisMonthPattern = Pattern.compile(DUE_THIS_MONTH_PATTERN, Pattern.CASE_INSENSITIVE);
        private final PrettyTimeParser parser;

        public DueDateMatcher(PrettyTimeParser parser) {
            this.parser = parser;
        }

        @Override
        public String match(String text) {

            Matcher tmm = dueThisMonthPattern.matcher(text);
            if (tmm.find()) {
                int day = Integer.parseInt(tmm.group(1));
                Calendar c = Calendar.getInstance();
                int maxDaysInMoth = c.getActualMaximum(Calendar.DAY_OF_MONTH);
                if (day > maxDaysInMoth) {
                    return "";
                }
                return tmm.group();
            }

            for (Pattern p : dueDatePatterns) {
                Matcher matcher = p.matcher(text);
                if (matcher.find()) {
                    return matcher.group();
                }
            }
            return "";
        }

        @Override
        public Date parse(String text) {
            Matcher tmm = dueThisMonthPattern.matcher(text);
            if (tmm.find()) {
                int day = Integer.parseInt(tmm.group(1));
                Calendar c = Calendar.getInstance();
                int maxDaysInMoth = c.getActualMaximum(Calendar.DAY_OF_MONTH);
                if (day > maxDaysInMoth) {
                    return null;
                }
                c.set(Calendar.DAY_OF_MONTH, day);
                return c.getTime();
            }

            for (Pattern p : dueDatePatterns) {
                Matcher matcher = p.matcher(text);
                if (matcher.find()) {
                    List<Date> dueResult = parser.parse(matcher.group());
                    if (dueResult.size() != 1) {
                        return null;
                    }
                    return dueResult.get(0);
                }
            }
            return null;
        }

    }
}

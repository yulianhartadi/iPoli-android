package io.ipoli.android.quest.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;

import org.ocpsoft.prettytime.nlp.PrettyTimeParser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.ipoli.android.R;
import io.ipoli.android.app.BaseActivity;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/18/16.
 */
public class AddQuestActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    @Bind(R.id.quest_name)
    AutoCompleteTextView questName;

    @Bind(R.id.due_date)
    ImageButton dueDate;

    @Bind(R.id.start_time)
    ImageButton startTime;

    @Bind(R.id.duration)
    ImageButton duration;
    private ArrayAdapter<SpannableString> adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_quest);

        ButterKnife.bind(this);
        appComponent().inject(this);
        setFinishOnTouchOutside(false);

        adapter = new ArrayAdapter<>(this,
                R.layout.add_quest_autocomplete_item, new ArrayList());
        questName.setAdapter(adapter);
        questName.setOnItemClickListener(this);

        final PrettyTimeParser parser = new PrettyTimeParser();

        final DueDateMatcher dueDateMatcher = new DueDateMatcher();
        final StartTimeMatcher startTimeMatcher = new StartTimeMatcher();
        final DurationMatcher durationMatcher = new DurationMatcher();

        questName.addTextChangedListener(new TextWatcher() {

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
                    duration.setBackgroundResource(R.drawable.circle_disable);
                    startTime.setBackgroundResource(R.drawable.circle_disable);
                    dueDate.setBackgroundResource(R.drawable.circle_disable);
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
                    text = text.replace(matchedDueDate, " ");
                    dueDate.setBackgroundResource(R.drawable.circle_disable);
                    dueDate.setEnabled(false);
                } else {
                    dueDate.setBackgroundResource(R.drawable.circle_normal);
                    dueDate.setEnabled(true);
                }

                String name = text;
                if (TextUtils.isEmpty(matchedDueDate)) {
                    dueDate.setBackgroundResource(R.drawable.circle_accent);
                } else if (TextUtils.isEmpty(matchedStartTime)) {
                    startTime.setBackgroundResource(R.drawable.circle_accent);
                } else if (TextUtils.isEmpty(matchedDuration)) {
                    duration.setBackgroundResource(R.drawable.circle_accent);
                }
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        SpannableString ss = adapter.getItem(position);
        String selectedText = ss.toString();
        ForegroundColorSpan[] spans = ss.getSpans(0, ss.length(), ForegroundColorSpan.class);
        for(int i = spans.length - 1; i >=0; i--) {
            ForegroundColorSpan span = spans[i];
            int start = ss.getSpanStart(span);
            int end = ss.getSpanEnd(span);
            if(start > 0) {
                selectedText = selectedText.substring(0, start);
            } else {
                selectedText = selectedText.substring(end, selectedText.length());
            }
        }

        String questText = questName.getText().toString();
        if(!questText.endsWith(" ")) {
            questText += " ";
        }
        questName.setText(questText + selectedText);
        questName.setThreshold(1000);
        questName.setSelection(questName.getText().length());
    }


    @OnClick(R.id.due_date)
    public void onDueDateClick(View v) {
        int hintColor = ContextCompat.getColor(this, R.color.md_dark_text_26);
        SpannableString s1 = new SpannableString("... on 15 Feb");
        s1.setSpan(new ForegroundColorSpan(hintColor), 0, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        s1.setSpan(new ForegroundColorSpan(hintColor), 6, s1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        SpannableString s2 = new SpannableString("after 3 days");
        s2.setSpan(new ForegroundColorSpan(hintColor), 5, s2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        SpannableString s3 = new SpannableString("after 2 months");
        s3.setSpan(new ForegroundColorSpan(hintColor), 5, s3.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        SpannableString[] items = {s1, new SpannableString("today"), new SpannableString("tommorrow"), s2, s3};
        adapter = new ArrayAdapter<>(this,
                R.layout.add_quest_autocomplete_item, items);
        questName.setAdapter(adapter);
        questName.setThreshold(1);
        questName.showDropDown();
    }

    @OnClick(R.id.start_time)
    public void onStartTimeClick(View v) {
        String[] items = {"... at 19:00", "... at 7pm"};
//        adapter = new ArrayAdapter<>(this,
//                R.layout.add_quest_autocomplete_item, items);
//        questName.setAdapter(adapter);
//        questName.setThreshold(1);
//        questName.showDropDown();
    }

    @OnClick(R.id.duration)
    public void onDurationClick(View v) {
        String[] items = {"for 30 min", "for 1 hour", "for 15 min", "for 1 h and 30 m"};
//        adapter = new ArrayAdapter<>(this,
//                R.layout.add_quest_autocomplete_item, items);
//        questName.setAdapter(adapter);
//        questName.setThreshold(1);
//        questName.showDropDown();
    }


    public interface QuestTextMatcher {
        String match(String text);
    }

    public static class DurationMatcher implements QuestTextMatcher {

        private static final String DURATION_PATTERN = " for (\\d{1,3})\\s?(hours|hour|h|minutes|minute|mins|min|m)(?: and (\\d{1,3})\\s?(minutes|minute|mins|min|m))?";
        Pattern dp = Pattern.compile(DURATION_PATTERN, Pattern.CASE_INSENSITIVE);

        @Override
        public String match(String text) {

            Matcher dm = dp.matcher(text);
            if (dm.find()) {
                return dm.group();
            }
            return "";
        }
    }

    public static class StartTimeMatcher implements QuestTextMatcher {

        private static final String PATTERN = " at (\\d{1,2}[:|\\.]?(\\d{2})?\\s?(am|pm)?)";
        private Pattern pattern = Pattern.compile(PATTERN, Pattern.CASE_INSENSITIVE);

        @Override
        public String match(String txt) {
            Matcher m = pattern.matcher(txt);
            if (m.find()) {
                return m.group();
            }
            return "";
        }
    }

    public static class DueDateMatcher implements QuestTextMatcher {

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

        @Override
        public String match(String txt) {

            Matcher tmm = dueThisMonthPattern.matcher(txt);
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
                Matcher matcher = p.matcher(txt);
                if (matcher.find()) {
                    return matcher.group();
                }
            }
            return "";
        }

    }
}

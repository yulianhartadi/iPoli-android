package io.ipoli.android.quest.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;

import org.ocpsoft.prettytime.nlp.PrettyTimeParser;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.app.BaseActivity;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/18/16.
 */
public class AddQuestActivity extends BaseActivity {

    @Bind(R.id.quest_name)
    AutoCompleteTextView questName;

    @Bind(R.id.due_date)
    ImageButton dueDate;

    @Bind(R.id.start_time)
    ImageButton startTime;

    @Bind(R.id.duration)
    ImageButton duration;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_quest);

        ButterKnife.bind(this);
        appComponent().inject(this);
        setFinishOnTouchOutside(false);
        String[] COUNTRIES = new String[]{
                "for 30 min", "for 1 hour", "for 15 min", "for 1 h and 30 m"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, COUNTRIES);
        questName.setAdapter(adapter);

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

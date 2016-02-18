package io.ipoli.android.quest.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import org.ocpsoft.prettytime.nlp.PrettyTimeParser;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
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

        questName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int removedCount, int addedCount) {
//                Log.d("Text", s + " " + start + " " + addedCount + " " + removedCount);
                Log.d("Parsed Date", DueDateParser.parse(s.toString()));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public static class DueDateParser {
        private static Date dueDate;
        private static final String DUE_TODAY_TOMORROW_PATTERN = "today|tomorrow";
        private static final String DUE_MONTH_PATTERN = "(\\son)?\\s(\\d){1,2}(\\s)?(st|th)?\\s(of\\s)?(next month|this month|January|February|March|April|May|June|July|August|September|October|November|December|Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec){1}";
        private static final String DUE_AFTER_IN_PATTERN = "(after|in)\\s\\w+\\s(day|week|month|year)s?";
        private static final String DUE_FROM_NOW_PATTERN = "\\w+\\s(day|week|month|year)s?\\sfrom\\snow";
        private static final String DUE_THIS_NEXT_PATTERN = "(this|next)\\s(Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday|Mon|Tue|Wed|Thur|Fri|Sat|Sun)";
        private static final String DUE_THIS_MONTH_PATTERN = "on\\s?(\\d{1,2})\\s?(st|th)$";

        static Pattern[] dueDatePatterns = {
                Pattern.compile(DUE_TODAY_TOMORROW_PATTERN, Pattern.CASE_INSENSITIVE),
                Pattern.compile(DUE_MONTH_PATTERN, Pattern.CASE_INSENSITIVE),
                Pattern.compile(DUE_THIS_NEXT_PATTERN, Pattern.CASE_INSENSITIVE),
                Pattern.compile(DUE_AFTER_IN_PATTERN, Pattern.CASE_INSENSITIVE),
                Pattern.compile(DUE_FROM_NOW_PATTERN, Pattern.CASE_INSENSITIVE)
        };

        public static String parse(String txt) {
            Pattern tmp = Pattern.compile(DUE_THIS_MONTH_PATTERN, Pattern.CASE_INSENSITIVE);
            Matcher tmm = tmp.matcher(txt);
            if (tmm.find()) {
                int day = Integer.parseInt(tmm.group(1));
                Calendar c = Calendar.getInstance();
                int maxDaysInMoth = c.getActualMaximum(Calendar.DAY_OF_MONTH);
                if (day > maxDaysInMoth) {
                    return "";
                }
                c.set(Calendar.DAY_OF_MONTH, day);
                dueDate = c.getTime();
                return tmm.group();
            }


            PrettyTimeParser parser = new PrettyTimeParser();
            for (Pattern p : dueDatePatterns) {
                Matcher matcher = p.matcher(txt);
                if (matcher.find()) {
                    List<Date> dueResult = parser.parse(matcher.group());
                    if (dueResult.size() != 1) {
                        return "";
                    }
                    dueDate = dueResult.get(0);
                    return matcher.group();
                }
            }
            return "";
        }

    }
}

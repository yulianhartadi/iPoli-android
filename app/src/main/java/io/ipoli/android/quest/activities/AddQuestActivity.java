package io.ipoli.android.quest.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

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
    }
}

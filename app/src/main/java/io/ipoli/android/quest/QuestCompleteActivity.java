package io.ipoli.android.quest;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.BaseActivity;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/22/16.
 */
public class QuestCompleteActivity extends BaseActivity {

    public static final String DIFFICULTY_EXTRA_KEY = "difficulty";
    public static final String LOG_EXTRA_KEY = "log";
    @Bind(R.id.quest_complete_log)
    EditText log;

    @Bind(R.id.quest_complete_difficulty_group)
    RadioGroup difficultyGroup;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quest_complete);
        setFinishOnTouchOutside(false);
        ButterKnife.bind(this);
        appComponent().inject(this);
    }

    @OnClick(R.id.quest_complete_done)
    public void onDoneTap(View v) {
        Intent data = new Intent();
        data.putExtras(getIntent());
        Difficulty difficulty = getDifficulty();
        data.putExtra(DIFFICULTY_EXTRA_KEY, difficulty);
        data.putExtra(LOG_EXTRA_KEY, log.getText().toString());
        setResult(Constants.SUCCESS_RESULT_CODE, data);
        finish();
    }

    private Difficulty getDifficulty() {
        int radioButtonID = difficultyGroup.getCheckedRadioButtonId();
        View radioButton = difficultyGroup.findViewById(radioButtonID);
        int idx = difficultyGroup.indexOfChild(radioButton);
        RadioButton r = (RadioButton) difficultyGroup.getChildAt(idx);
        try {
            return Difficulty.valueOf(r.getText().toString().toUpperCase());
        } catch (Exception ignored) {
            return Difficulty.UNKNOWN;
        }
    }
}

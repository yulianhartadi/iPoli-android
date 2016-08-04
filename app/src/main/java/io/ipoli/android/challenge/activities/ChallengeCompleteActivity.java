package io.ipoli.android.challenge.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.ipoli.android.R;
import io.ipoli.android.app.activities.BaseActivity;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/28/16.
 */
public class ChallengeCompleteActivity extends BaseActivity {

    public static final String TITLE = "title";
    public static final String EXPERIENCE = "experience";
    public static final String COINS = "coins";

    @BindView(R.id.dialog_title)
    TextView title;

    @BindView(R.id.experience_text)
    TextView experienceText;

    @BindView(R.id.coins_text)
    TextView coinsText;

    private long experience;
    private long coins;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_challenge_complete);
        ButterKnife.bind(this);

        title.setText(getIntent().getStringExtra(TITLE));
        experienceText.setText(String.valueOf(getIntent().getLongExtra(EXPERIENCE, 0)));
        coinsText.setText(String.valueOf(getIntent().getLongExtra(COINS, 0)));
    }

    @OnClick(R.id.done)
    public void onDoneClick(View view) {
        finish();
    }
}

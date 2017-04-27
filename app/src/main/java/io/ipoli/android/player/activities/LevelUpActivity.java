package io.ipoli.android.player.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
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
public class LevelUpActivity extends BaseActivity {

    public static final String LEVEL = "level";

    @BindView(R.id.message)
    TextView message;

    @BindView(R.id.title)
    TextView title;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_level_up);
        ButterKnife.bind(this);

        int level = getIntent().getIntExtra(LEVEL, 100);

        String[] playerTitles = getResources().getStringArray(R.array.player_titles);

        if (level % 10 == 0 && level <= (playerTitles.length - 1) * 10) {
            String playerTitle = playerTitles[level / 10];
            title.setText("You've become " + playerTitle);
        }

        message.setText(Html.fromHtml(getString(R.string.level_up_message, level)));
    }

    @OnClick(R.id.done)
    public void onDoneClick(View view) {
        finish();
    }
}

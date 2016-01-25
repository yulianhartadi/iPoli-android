package io.ipoli.android.player;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.ipoli.android.R;
import io.ipoli.android.app.BaseActivity;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/24/16.
 */
public class LevelUpActivity extends BaseActivity {

    public static final int REWARD_WIDTH = 400;
    public static final int REWARD_HEIGHT = 250;

    public static final String LEVEL_EXTRA_KEY = "level";

    @Bind(R.id.level_up_reward)
    ImageView reward;

    @Bind(R.id.level_up_text)
    TextView rewardText;

    @Bind(R.id.level_up_attribution)
    TextView attribution;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_up);
        setFinishOnTouchOutside(false);
        ButterKnife.bind(this);
        appComponent().inject(this);

        int level = getIntent().getIntExtra(LEVEL_EXTRA_KEY, -1);
        if (level < 0) {
            finish();
            return;
        }

        rewardText.setText(getString(R.string.level_up_reward, level));

        int width = dp2Px(REWARD_WIDTH);
        int height = dp2Px(REWARD_HEIGHT);

        int rewardIdx = level - 1;
        if (level > Reward.REWARDS.length) {
            Random rand = new Random();
            rewardIdx = rand.nextInt(Reward.REWARDS.length);
        }
        Reward r = Reward.REWARDS[rewardIdx];
        Glide.with(this).load(r.getUrl()).override(width, height).fitCenter()
                .into(reward);
        if (!TextUtils.isEmpty(r.getSource())) {
            attribution.setText(getString(R.string.attribution, r.getSource()));
        }
    }

    private int dp2Px(int dps) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dps, getResources().getDisplayMetrics());
    }

    @OnClick(R.id.level_up_cool)
    public void onCoolTap(View v) {
        finish();
    }

    @OnClick(R.id.level_up_bad)
    public void onBadTap(View v) {
        finish();
    }
}

package io.ipoli.android.quest;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.ipoli.android.R;
import io.ipoli.android.app.BaseActivity;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/1/16.
 */
public class QuestActivity extends BaseActivity {

    @Bind(R.id.quest_details_progress)
    ProgressBar timerProgress;

    @Bind(R.id.quest_details_timer)
    FloatingActionButton timerButton;

    @Bind(R.id.quest_details_time)
    TextView timer;

    private CountDownTimer questTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quest);
        ButterKnife.bind(this);
        appComponent().inject(this);

        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.md_blue_800));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (questTimer != null) {
            questTimer.cancel();
        }
    }

    @OnClick(R.id.quest_details_timer)
    public void onTimerTap(View v) {
        final long totalTime = 30 * 60 * 1000;

        timerProgress.setMax(30 * 60);
        timerProgress.setSecondaryProgress(30 * 60);

        questTimer = createQuestTimer(totalTime);
        questTimer.start();
        timerButton.setImageResource(R.drawable.ic_stop_white_32dp);
    }

    CountDownTimer createQuestTimer(final long totalTime) {
        return new CountDownTimer(totalTime + 1000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                long elapsedSeconds = (totalTime - millisUntilFinished) / 1000;
                timerProgress.setProgress((int) elapsedSeconds);
                Date d = new Date(millisUntilFinished);
                SimpleDateFormat sdf = new SimpleDateFormat("mm:ss", Locale.getDefault());
                timer.setText(sdf.format(d));
            }

            @Override
            public void onFinish() {

            }
        };
    }
}

package io.ipoli.android.challenge.activities;

import android.os.Bundle;

import com.squareup.otto.Bus;

import javax.inject.Inject;

import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.app.BaseActivity;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 6/22/16.
 */
public class PickDailyChallengeQuestsActivity extends BaseActivity {

    @Inject
    Bus eventBus;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_daily_challenge_quests);
        ButterKnife.bind(this);
        appComponent().inject(this);
    }
}

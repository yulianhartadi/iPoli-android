package io.ipoli.android.quest.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;

import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.app.activities.BaseActivity;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/26/17.
 */
public class EisenhowerMatrixActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        appComponent().inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eisenhower_matrix);
        ButterKnife.bind(this);

    }
}

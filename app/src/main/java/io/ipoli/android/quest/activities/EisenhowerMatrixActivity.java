package io.ipoli.android.quest.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.app.activities.BaseActivity;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/26/17.
 */
public class EisenhowerMatrixActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        appComponent().inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eisenhower_matrix);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
        }
    }
}

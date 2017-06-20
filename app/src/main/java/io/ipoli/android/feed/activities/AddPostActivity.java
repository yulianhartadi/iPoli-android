package io.ipoli.android.feed.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.activities.BaseActivity;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/20/17.
 */
public class AddPostActivity extends BaseActivity {

    @BindView(R.id.post_header)
    ViewGroup header;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getAppComponent(this).inject(this);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_add_post);
        ButterKnife.bind(this);

        TextView headerTitle = (TextView) header.findViewById(R.id.fancy_dialog_title);
        ImageView headerIcon = (ImageView) header.findViewById(R.id.fancy_dialog_image);

        headerTitle.setText("Share your achievement");
        headerIcon.setImageResource(R.drawable.ic_share_white_24dp);
    }
}

package io.ipoli.android.assistant;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.BaseActivity;
import io.ipoli.android.app.utils.ResourceUtils;
import io.ipoli.android.assistant.events.AvatarSelectedEvent;

public class PickAvatarActivity extends BaseActivity {

    @Bind(R.id.avatar_list)
    RecyclerView avatarList;

    @Inject
    Bus eventBus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_avatar);
        appComponent().inject(this);
        ButterKnife.bind(this);

        setTitle(getIntent().getStringExtra("title"));

        avatarList.setLayoutManager(new GridLayoutManager(this, 2));
        List<String> avatars = new ArrayList<>();
        for (int i = Constants.AVATAR_COUNT; i >= 1; i--) {
            avatars.add(String.format(Locale.getDefault(), "avatar_%02d", i));
        }
        avatarList.setAdapter(new AvatarAdapter(this, avatars, eventBus));
    }

    @Override
    public void onResume() {
        super.onResume();
        eventBus.register(this);
    }

    @Override
    public void onPause() {
        eventBus.unregister(this);
        super.onPause();
    }

    @Subscribe
    public void onAvatarSelected(AvatarSelectedEvent e) {
        Intent data = new Intent();
        data.putExtra("avatar", ResourceUtils.extractDrawableName(this, e.avatarRes));
        setResult(0, data);
        finish();
    }

}

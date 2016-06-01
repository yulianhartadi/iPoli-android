package io.ipoli.android.player.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.BaseActivity;
import io.ipoli.android.player.adapters.AvatarAdapter;
import io.ipoli.android.player.events.AvatarSelectedEvent;

public class PickAvatarActivity extends BaseActivity {

    @BindView(R.id.avatar_list)
    RecyclerView avatarList;

    @Inject
    Bus eventBus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_avatar);
        appComponent().inject(this);
        ButterKnife.bind(this);

        avatarList.setLayoutManager(new GridLayoutManager(this, 2));
        List<String> avatars = new ArrayList<>();
        for (int i = Constants.AVATAR_COUNT; i >= 1; i--) {
            avatars.add(String.format(Locale.getDefault(), "avatar_%02d", i));
        }
        avatarList.setAdapter(new AvatarAdapter(avatars, eventBus));
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
        data.putExtra(Constants.AVATAR_NAME_EXTRA_KEY, e.avatarName);
        setResult(0, data);
        finish();
    }

}

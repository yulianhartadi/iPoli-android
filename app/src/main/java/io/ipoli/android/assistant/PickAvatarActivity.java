package io.ipoli.android.assistant;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.app.BaseActivity;
import io.ipoli.android.assistant.events.OnAvatarSelectedEvent;

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
        List<Integer> avatars = new ArrayList<>();
        avatars.add(R.drawable.avatar_01);
        avatars.add(R.drawable.avatar_02);
        avatars.add(R.drawable.avatar_01);
        avatars.add(R.drawable.avatar_02);
        avatars.add(R.drawable.avatar_01);
        avatars.add(R.drawable.avatar_02);
        avatars.add(R.drawable.avatar_01);
        avatars.add(R.drawable.avatar_02);
        avatars.add(R.drawable.avatar_01);
        avatars.add(R.drawable.avatar_02);
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
    public void onAvatarSelected(OnAvatarSelectedEvent e) {
        Log.d("Avatar", "selected");
        Intent data = new Intent();
        data.putExtra("avatarRes", e.avatarRes);
        setResult(0, data);
        finish();
    }

}

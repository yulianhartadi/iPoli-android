package io.ipoli.android.feed.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.activities.BaseActivity;
import io.ipoli.android.app.persistence.OnDataChangedListener;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.feed.data.PlayerProfile;
import io.ipoli.android.feed.data.Post;
import io.ipoli.android.feed.persistence.FeedPersistenceService;
import io.ipoli.android.player.Player;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.persistence.QuestPersistenceService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/20/17.
 */
public class AddPostActivity extends BaseActivity implements OnDataChangedListener<Quest> {

    @Inject
    QuestPersistenceService questPersistenceService;

    @Inject
    FeedPersistenceService feedPersistenceService;

    @BindView(R.id.fancy_dialog_title)
    TextView headerTitle;

    @BindView(R.id.fancy_dialog_image)
    ImageView headerIcon;

    @BindView(R.id.player_avatar)
    ImageView playerAvatar;

    @BindView(R.id.player_username)
    TextView playerUsername;

    @BindView(R.id.player_title)
    TextView playerTitle;

    @BindView(R.id.post_image)
    ImageView postImage;

    @BindView(R.id.post_title)
    TextView postTitle;

    @BindView(R.id.post_message)
    EditText postMessage;

    private Quest quest;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        App.getAppComponent(this).inject(this);
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_add_post);
        ButterKnife.bind(this);

        String questId = getIntent().getStringExtra(Constants.QUEST_ID_EXTRA_KEY);

        if (StringUtils.isEmpty(questId)) {
            throw new IllegalArgumentException("Quest id is required");
        }

        questPersistenceService.findById(questId, this);

        feedPersistenceService.findPlayerProfile(App.getPlayerId(), profile -> {
            if(profile == null) {
                feedPersistenceService.createPlayerProfile(new PlayerProfile(getPlayer()));
            }
        });
    }

    @Override
    public void onDataChanged(Quest quest) {
        this.quest = quest;
        Player player = getPlayer();

        playerUsername.setText("@" + player.getUsername());
        playerTitle.setText(player.getTitle(getResources().getStringArray(R.array.player_titles)));
        playerAvatar.setImageResource(player.getCurrentAvatar().picture);

        headerTitle.setText("Share your achievement");
        headerIcon.setImageResource(R.drawable.ic_share_white_24dp);

        postTitle.setText(quest.getName());
        postImage.setImageResource(quest.getCategoryType().colorfulImage);
    }

    @OnClick(R.id.post_add)
    public void addPostClicked(View v) {
        Post post = new Post(postTitle.getText().toString(), postMessage.getText().toString(),
                playerTitle.getText().toString(), getPlayer(), quest);
        feedPersistenceService.addPost(post);
        finish();
    }

    @OnClick(R.id.post_cancel)
    public void cancelClicked(View v) {
        finish();
    }
}

package io.ipoli.android.chat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.ipoli.android.R;
import io.ipoli.android.app.BaseActivity;
import io.ipoli.android.app.services.CommandParserService;
import io.ipoli.android.app.services.ReminderIntentService;
import io.ipoli.android.assistant.Assistant;
import io.ipoli.android.assistant.PickAvatarActivity;
import io.ipoli.android.assistant.events.AssistantReplyEvent;
import io.ipoli.android.assistant.events.AssistantStartActivityEvent;
import io.ipoli.android.assistant.events.ReviewTodayEvent;
import io.ipoli.android.assistant.persistence.AssistantPersistenceService;
import io.ipoli.android.chat.events.AvatarChangedEvent;
import io.ipoli.android.chat.events.RequestAvatarChangeEvent;
import io.ipoli.android.chat.persistence.MessagePersistenceService;
import io.ipoli.android.player.Player;
import io.ipoli.android.player.persistence.PlayerPersistenceService;

public class ChatActivity extends BaseActivity {

    public static final int PICK_PLAYER_AVATAR = 101;
    public static final int PICK_ASSISTANT_AVATAR = 102;

    @Bind(R.id.experience_bar)
    ProgressBar experienceBar;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Inject
    Bus eventBus;

    @Bind(R.id.conversation)
    RecyclerView chatView;

    @Bind(R.id.command_text)
    EditText commandText;

    private MessageAdapter messageAdapter;

    @Inject
    CommandParserService commandParserService;

    @Inject
    MessagePersistenceService messagePersistenceService;

    @Inject
    PlayerPersistenceService playerPersistenceService;

    @Inject
    AssistantPersistenceService assistantPersistenceService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appComponent().inject(this);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setStackFromEnd(true);

        chatView.setLayoutManager(layoutManager);
        Player p = playerPersistenceService.find();
        Assistant a = assistantPersistenceService.find();

        messageAdapter = new MessageAdapter(this, messagePersistenceService.findAll(), eventBus, p.getAvatar(), a.getAvatar());
        chatView.setAdapter(messageAdapter);
        chatView.scrollToPosition(messageAdapter.getItemCount() - 1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        eventBus.register(this);
        if (getIntent().getAction().equals(ReminderIntentService.ACTION_REMIND_REVIEW_DAY)) {
            eventBus.post(new ReviewTodayEvent());
        }
    }

    @Override
    protected void onPause() {
        eventBus.unregister(this);
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PLAYER_AVATAR || requestCode == PICK_ASSISTANT_AVATAR) {
            String avatar = data.getStringExtra("avatar");
            if (!TextUtils.isEmpty(avatar)) {
                Message.MessageAuthor author = requestCode == PICK_ASSISTANT_AVATAR ?
                        Message.MessageAuthor.ASSISTANT : Message.MessageAuthor.PLAYER;
                messageAdapter.changeAvatar(avatar, author);
                if (requestCode == PICK_ASSISTANT_AVATAR) {
                    Assistant assistant = assistantPersistenceService.find();
                    assistant.setAvatar(avatar);
                    assistantPersistenceService.save(assistant);
                } else {
                    Player player = playerPersistenceService.find();
                    player.setAvatar(avatar);
                    playerPersistenceService.save(player);
                }
                eventBus.post(new AvatarChangedEvent(author, avatar));
            }
        }
    }

    @OnClick(R.id.send_command)
    public void onSendCommand(View v) {
        String command = commandText.getText().toString();
        if (TextUtils.isEmpty(command)) {
            return;
        }
        Message m = new Message(command, Message.MessageAuthor.PLAYER.name());
        addMessage(m);
        chatView.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
        commandText.setText("");
        commandParserService.parse(command);
    }

    @Subscribe
    public void onAssistantStartActivity(AssistantStartActivityEvent e) {
        startActivity(e.clazz);
    }

    private void startActivity(Class<?> clazz) {
        Intent intent = new Intent(this, clazz);
        startActivity(intent);
    }

    @Subscribe
    public void onAssistantReply(AssistantReplyEvent e) {
        Message m = new Message(e.message, Message.MessageAuthor.ASSISTANT.name());
        addMessage(m);
        chatView.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
    }

    @Subscribe
    public void onRequestAvatarChange(RequestAvatarChangeEvent e) {
        Message.MessageAuthor author = e.messageAuthor;
        Intent i = new Intent(this, PickAvatarActivity.class);
        String title = author == Message.MessageAuthor.ASSISTANT ?
                getString(R.string.pick_assistant_avatar_title) : getString(R.string.pick_player_avatar_title);
        int requestCode = author == Message.MessageAuthor.ASSISTANT ?
                PICK_ASSISTANT_AVATAR : PICK_PLAYER_AVATAR;
        i.putExtra("title", title);
        startActivityForResult(i, requestCode);
    }

    private void addMessage(Message message) {
        messageAdapter.addMessage(message);
        messagePersistenceService.save(message);
    }
}
package io.ipoli.android.chat;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.LinearInterpolator;
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
import io.ipoli.android.assistant.events.AssistantReplyEvent;
import io.ipoli.android.assistant.events.PlanTodayEvent;
import io.ipoli.android.assistant.events.RenameAssistantEvent;
import io.ipoli.android.assistant.events.ReviewTodayEvent;
import io.ipoli.android.assistant.events.ShowQuestsEvent;
import io.ipoli.android.assistant.persistence.AssistantPersistenceService;
import io.ipoli.android.chat.persistence.MessagePersistenceService;
import io.ipoli.android.quest.PlanDayActivity;
import io.ipoli.android.quest.QuestListActivity;

public class ChatActivity extends BaseActivity {

    @Bind(R.id.experience_bar)
    ProgressBar experienceBar;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Inject
    Bus eventBus;

    @Inject
    AssistantPersistenceService assistantPersistenceService;


    private Assistant assistant;

    @Bind(R.id.conversation)
    RecyclerView chatView;

    @Bind(R.id.command_text)
    EditText commandText;

    private MessageAdapter messageAdapter;

    @Inject
    CommandParserService commandParserService;

    @Inject
    MessagePersistenceService messagePersistenceService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appComponent().inject(this);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);

        assistant = assistantPersistenceService.find();
        toolbar.setTitle(assistant.getName());

        setSupportActionBar(toolbar);

        ObjectAnimator progressAnimator = ObjectAnimator.ofInt(experienceBar, "progress", experienceBar.getProgress(), experienceBar.getMax());
        progressAnimator.setDuration(android.R.integer.config_shortAnimTime);
        progressAnimator.setInterpolator(new LinearInterpolator());
        progressAnimator.start();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setStackFromEnd(true);

        chatView.setLayoutManager(layoutManager);
        messageAdapter = new MessageAdapter(messagePersistenceService.findAll(), eventBus);
        chatView.setAdapter(messageAdapter);
        chatView.scrollToPosition(messageAdapter.getItemCount() - 1);
    }

    @Subscribe
    public void onRenameAssistant(RenameAssistantEvent e) {
        toolbar.setTitle(e.name);
        assistant.setName(e.name);
        assistantPersistenceService.save(assistant);
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

    @OnClick(R.id.send_command)
    public void onSendCommand(View v) {
        String command = commandText.getText().toString();
        if (TextUtils.isEmpty(command)) {
            return;
        }
        Message m = new Message(command, Message.MessageType.USER.name(), R.drawable.avatar_02);
        addMessage(m);
        chatView.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
        commandText.setText("");
        commandParserService.parse(command);
    }


    @Subscribe
    public void onPlanToday(PlanTodayEvent e) {
        startActivity(PlanDayActivity.class);
    }

    @Subscribe
    public void onShowQuests(ShowQuestsEvent e) {
        startActivity(QuestListActivity.class);
    }

    private void startActivity(Class<?> clazz) {
        Intent intent = new Intent(this, clazz);
        startActivity(intent);
    }

    @Subscribe
    public void onAssistantReply(AssistantReplyEvent e) {
        Message m = new Message(e.message, Message.MessageType.ASSISTANT.name(), R.drawable.avatar_01);
        addMessage(m);
        chatView.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
    }

    private void addMessage(Message message) {
        messageAdapter.addMessage(message);
        messagePersistenceService.save(message);
    }
}
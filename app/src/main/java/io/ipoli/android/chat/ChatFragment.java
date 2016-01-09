package io.ipoli.android.chat;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.ipoli.android.R;
import io.ipoli.android.app.BaseFragment;
import io.ipoli.android.app.services.CommandParserService;
import io.ipoli.android.assistant.events.AssistantReplyEvent;
import io.ipoli.android.chat.persistence.MessagePersistenceService;

public class ChatFragment extends BaseFragment {

    @Bind(R.id.conversation)
    RecyclerView chatView;

    @Bind(R.id.command_text)
    EditText commandText;

    private MessageAdapter messageAdapter;

    @Inject
    Bus eventBus;

    @Inject
    CommandParserService commandParserService;

    @Inject
    MessagePersistenceService messagePersistenceService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        appComponent().inject(this);
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        ButterKnife.bind(this, view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setStackFromEnd(true);

        chatView.setLayoutManager(layoutManager);
        messageAdapter = new MessageAdapter(messagePersistenceService.findAll());
        chatView.setAdapter(messageAdapter);
        chatView.scrollToPosition(messageAdapter.getItemCount() - 1);
        return view;
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
    public void onAssistantReply(AssistantReplyEvent e) {
        Message m = new Message(e.message, Message.MessageType.ASSISTANT.name(), R.drawable.avatar_01);
        addMessage(m);
        chatView.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
    }

    private void addMessage(Message message) {
        messageAdapter.addMessage(message);
        messagePersistenceService.save(message);
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
}

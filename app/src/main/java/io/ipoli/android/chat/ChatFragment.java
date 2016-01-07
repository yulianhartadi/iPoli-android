package io.ipoli.android.chat;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.ipoli.android.BaseFragment;
import io.ipoli.android.R;
import io.ipoli.android.assistant.events.AssistantReplyEvent;
import io.ipoli.android.services.CommandParserService;

public class ChatFragment extends BaseFragment {

    @Bind(R.id.conversation)
    RecyclerView chatView;

    @Bind(R.id.command_text)
    EditText commandText;

    private ChatAdapter chatAdapter;

    @Inject
    Bus eventBus;

    @Inject
    CommandParserService commandParserService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        appComponent().inject(this);
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setStackFromEnd(true);

        chatView.setLayoutManager(layoutManager);

        List<Message> messageList = new ArrayList<>();
        messageList.add(new Message("Hello, it's me!", Message.MessageType.ASSISTANT, R.drawable.avatar_01));
        messageList.add(new Message("Who are you?", Message.MessageType.USER, R.drawable.avatar_02));
        messageList.add(new Message("I am your personal assistant", Message.MessageType.ASSISTANT, R.drawable.avatar_01));
        messageList.add(new Message("Great! Add quest buy toilet paper", Message.MessageType.USER, R.drawable.avatar_02));
        messageList.add(new Message("Add quest feed Vihar", Message.MessageType.USER, R.drawable.avatar_02));
        messageList.add(new Message("Add quest feed Vihar", Message.MessageType.USER, R.drawable.avatar_02));
        messageList.add(new Message("Add quest feed Vihar every day morning afternoon and evening with 1 granuli cups and 2 konservi from tuna and chicken", Message.MessageType.USER, R.drawable.avatar_02));
        chatAdapter = new ChatAdapter(messageList);
        chatView.setAdapter(chatAdapter);
        chatView.scrollToPosition(chatAdapter.getItemCount() - 1);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @OnClick(R.id.send_command)
    public void onSendCommand(View v) {
        String command = commandText.getText().toString();
        if (TextUtils.isEmpty(command)) {
            return;
        }
        Message m = new Message(command, Message.MessageType.USER, R.drawable.avatar_02);
        chatAdapter.addMessage(m);
        chatView.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
        commandText.setText("");
        commandText.clearFocus();

        InputMethodManager inputManager =
                (InputMethodManager) getContext().
                        getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(
                commandText.getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);

        commandParserService.parse(command);
    }

    @Subscribe
    public void onAssistantReply(AssistantReplyEvent e) {
        Message m = new Message(e.message, Message.MessageType.ASSISTANT, R.drawable.avatar_01);
        chatAdapter.addMessage(m);
        chatView.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
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

package io.ipoli.assistant;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.ipoli.assistant.chat.ChatAdapter;
import io.ipoli.assistant.chat.Message;

public class MainActivityFragment extends Fragment {

    @Bind(R.id.conversation)
    RecyclerView chatView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

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
        ChatAdapter adapter = new ChatAdapter(messageList);
        chatView.setAdapter(adapter);

        chatView.scrollToPosition(messageList.size() - 1);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}

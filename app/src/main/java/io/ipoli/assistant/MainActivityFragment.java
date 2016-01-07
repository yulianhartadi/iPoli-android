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

/**
 * A placeholder fragment containing a simple view.
 */
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

        chatView.setLayoutManager(layoutManager);

        List<Message> messageList = new ArrayList<>();
        messageList.add(new Message("Hello, it's me!", Message.MessageType.ASSISTANT));
        messageList.add(new Message("Who are you?", Message.MessageType.USER));
        ChatAdapter adapter = new ChatAdapter(messageList);
        chatView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}

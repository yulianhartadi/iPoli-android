package com.curiousily.ipoli;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.curiousily.ipoli.events.Author;
import com.curiousily.ipoli.events.NewMessageEvent;
import com.curiousily.ipoli.models.Message;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/12/15.
 */
public class ConversationFragment extends Fragment {

    private MessageViewAdapter adapter;
    private RecyclerView view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = (RecyclerView) inflater.inflate(
                R.layout.fragment_conversation, container, false);
        setupRecyclerView();
        return view;
    }

    private void setupRecyclerView() {
        view.setLayoutManager(new LinearLayoutManager(view.getContext()));
        adapter = new MessageViewAdapter(new ArrayList<Message>());
        view.setAdapter(adapter);
    }

    @Subscribe
    public void onNewMessage(NewMessageEvent e) {
        adapter.addItem(e.getMessage());
        view.scrollToPosition(adapter.getItemCount() - 1);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.get().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.get().register(this);
    }

    public static class MessageViewAdapter
            extends RecyclerView.Adapter<MessageViewAdapter.ViewHolder> {
        private List<Message> values;

        public static class ViewHolder extends RecyclerView.ViewHolder {

            public final TextView textView;

            public ViewHolder(View view) {
                super(view);
                textView = (TextView) view.findViewById(R.id.message_text);
            }
        }

        public void addItem(Message item) {
            values.add(item);
            notifyDataSetChanged();
        }

        public MessageViewAdapter(List<Message> items) {
            values = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_conversation, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            Message message = values.get(position);
            holder.textView.setText(message.getText());
            Author author = message.getAuthor();
            switch (author) {
                case User:
                    break;
                default:
            }
        }

        @Override
        public int getItemCount() {
            return values.size();
        }
    }
}

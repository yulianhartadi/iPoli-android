package io.ipoli.assistant.chat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.ipoli.assistant.R;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 12/28/15.
 */
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
    private List<Message> messages;
    private Map<Integer, RoundedBitmapDrawable> avatarCache;


    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View view;

        public ViewHolder(View v) {
            super(v);
            view = v;
        }
    }

    public ChatAdapter(List<Message> messages) {
        this.messages = messages;
        avatarCache = new HashMap<>();
    }

    @Override
    public ChatAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        Message.MessageType mt = Message.MessageType.values()[viewType];
        View v;
        if (mt == Message.MessageType.ASSISTANT) {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.assistant_message_item, parent, false);
        } else {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.user_message_item, parent, false);
        }
        return new ViewHolder(v);
    }


    public void addMessage(Message message) {
        messages.add(message);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        Message m = messages.get(position);
        return m.type.ordinal();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Message m = messages.get(position);
        TextView tv = (TextView) holder.view.findViewById(R.id.info_text);
        tv.setText(m.text);

        ImageView avatar = (ImageView) holder.view.findViewById(R.id.avatar);
        RoundedBitmapDrawable dr;
        if (avatarCache.containsKey(m.avatarRes)) {
            dr = avatarCache.get(m.avatarRes);
        } else {
            Bitmap src = BitmapFactory.decodeResource(holder.view.getResources(), m.avatarRes);
            dr = RoundedBitmapDrawableFactory.create(holder.view.getResources(), src);
            dr.setCornerRadius(Math.max(src.getWidth(), src.getHeight()) / 2.0f);
            avatarCache.put(m.avatarRes, dr);
        }
        avatar.setImageDrawable(dr);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }
}
package io.ipoli.android.chat;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.otto.Bus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.ipoli.android.R;
import io.ipoli.android.chat.events.NewMessageEvent;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 12/28/15.
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    private List<Message> messages;
    private final Bus eventBus;
    private Map<Integer, RoundedBitmapDrawable> avatarCache;

    public MessageAdapter(List<Message> messages, Bus eventBus) {
        this.messages = messages;
        this.eventBus = eventBus;
        avatarCache = new HashMap<>();
    }

    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                        int viewType) {
        Message.MessageType mt = Message.MessageType.values()[viewType];
        int layout = mt == Message.MessageType.ASSISTANT ?
                R.layout.assistant_message_item : R.layout.user_message_item;
        View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new ViewHolder(v);
    }

    public void addMessage(Message message) {
        messages.add(message);
        notifyDataSetChanged();
        eventBus.post(new NewMessageEvent(message));
    }

    @Override
    public int getItemViewType(int position) {
        Message m = messages.get(position);
        return Message.MessageType.valueOf(m.getType()).ordinal();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Message m = messages.get(position);
        TextView tv = (TextView) holder.itemView.findViewById(R.id.info_text);
        tv.setText(Html.fromHtml(m.getText()));
        ImageView avatar = (ImageView) holder.itemView.findViewById(R.id.avatar);
        avatar.setImageDrawable(getRoundedBitmapDrawable(holder.itemView.getResources(), m));
    }

    private RoundedBitmapDrawable getRoundedBitmapDrawable(Resources resources, Message m) {
        if (avatarCache.containsKey(m.getAvatarRes())) {
            return avatarCache.get(m.getAvatarRes());
        }
        Bitmap src = BitmapFactory.decodeResource(resources, m.getAvatarRes());
        RoundedBitmapDrawable rbd = RoundedBitmapDrawableFactory.create(resources, src);
        rbd.setCornerRadius(Math.max(src.getWidth(), src.getHeight()) / 2.0f);
        avatarCache.put(m.getAvatarRes(), rbd);
        return rbd;
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View v) {
            super(v);
        }
    }
}
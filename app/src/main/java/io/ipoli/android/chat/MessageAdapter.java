package io.ipoli.android.chat;

import android.content.Context;
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
import io.ipoli.android.app.utils.ResourceUtils;
import io.ipoli.android.chat.events.NewMessageEvent;
import io.ipoli.android.chat.events.RequestAvatarChangeEvent;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 12/28/15.
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    private final Context context;
    private List<Message> messages;
    private final Bus eventBus;
    private int assistantAvatarRes;
    private int playerAvatarRes;
    private Map<Integer, RoundedBitmapDrawable> avatarCache;

    public MessageAdapter(Context context, List<Message> messages, Bus eventBus, String playerAvatarRes, String assistantAvatarRes) {
        this.context = context;
        this.assistantAvatarRes = ResourceUtils.extractDrawableResource(context, assistantAvatarRes);
        this.playerAvatarRes = ResourceUtils.extractDrawableResource(context, playerAvatarRes);
        this.messages = messages;
        this.eventBus = eventBus;
        avatarCache = new HashMap<>();
    }

    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                        int viewType) {
        Message.MessageAuthor mt = Message.MessageAuthor.values()[viewType];
        int layout = mt == Message.MessageAuthor.ASSISTANT ?
                R.layout.assistant_message_item : R.layout.player_message_item;
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
        return Message.MessageAuthor.valueOf(m.getAuthor()).ordinal();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Message m = messages.get(position);
        TextView tv = (TextView) holder.itemView.findViewById(R.id.info_text);
        tv.setText(Html.fromHtml(m.getText()));
        ImageView avatar = (ImageView) holder.itemView.findViewById(R.id.avatar);
        avatar.setImageDrawable(getRoundedBitmapDrawable(holder.itemView.getResources(), m));
        avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eventBus.post(new RequestAvatarChangeEvent(Message.MessageAuthor.valueOf(m.getAuthor())));
            }
        });
    }

    private RoundedBitmapDrawable getRoundedBitmapDrawable(Resources resources, Message m) {
        Message.MessageAuthor mAuthor = Message.MessageAuthor.valueOf(m.getAuthor());
        int avatarRes = mAuthor == Message.MessageAuthor.ASSISTANT ?
                assistantAvatarRes : playerAvatarRes;
        if (avatarCache.containsKey(avatarRes)) {
            return avatarCache.get(avatarRes);
        }
        Bitmap src = BitmapFactory.decodeResource(resources, avatarRes);
        RoundedBitmapDrawable rbd = RoundedBitmapDrawableFactory.create(resources, src);
        rbd.setCornerRadius(Math.max(src.getWidth(), src.getHeight()) / 2.0f);
        avatarCache.put(avatarRes, rbd);
        return rbd;
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void changeAvatar(String avatar, Message.MessageAuthor author) {
        clearAvatarCache();
        int avatarRes = ResourceUtils.extractDrawableResource(context, avatar);
        if (author == Message.MessageAuthor.ASSISTANT) {
            assistantAvatarRes = avatarRes;
        } else {
            playerAvatarRes = avatarRes;
        }
        notifyDataSetChanged();
    }

    private void clearAvatarCache() {
        avatarCache.clear();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View v) {
            super(v);
        }
    }
}
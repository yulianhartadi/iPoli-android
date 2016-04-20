package io.ipoli.android.assistant;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import io.ipoli.android.R;
import io.ipoli.android.app.utils.ResourceUtils;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/13/16.
 */
public class AvatarAdapter extends RecyclerView.Adapter {
    private final List<Integer> avatars;

    public AvatarAdapter(Context context, List<String> avatars) {
        this.avatars = new ArrayList<>();
        for (String a : avatars) {
            this.avatars.add(ResourceUtils.extractDrawableResource(context, a));
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.avatar_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final Integer avatarRes = avatars.get(position);
        ImageView avatar = (ImageView) holder.itemView.findViewById(R.id.avatar_image);
        avatar.setImageResource(avatarRes);
    }

    @Override
    public int getItemCount() {
        return avatars.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View v) {
            super(v);

        }
    }
}

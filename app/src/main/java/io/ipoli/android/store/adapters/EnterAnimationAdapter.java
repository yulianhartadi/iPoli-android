package io.ipoli.android.store.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import io.ipoli.android.R;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/29/17.
 */

public abstract class EnterAnimationAdapter<V extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<V> {
    protected int lastAnimatedPosition = -1;

    @Override
    public void onBindViewHolder(V holder, int position) {
        doOnBindViewHolder(holder, position);
        playEnterAnimation(holder.itemView, holder.getAdapterPosition());
    }

    protected abstract void doOnBindViewHolder(V holder, int position);

    protected void playEnterAnimation(View viewToAnimate, int position) {
        if (position > lastAnimatedPosition) {
            Animation anim = AnimationUtils.loadAnimation(viewToAnimate.getContext(), R.anim.fade_in);
            anim.setStartOffset(position * 50);
            viewToAnimate.startAnimation(anim);
            lastAnimatedPosition = position;
        }
    }

}

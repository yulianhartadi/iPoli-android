package io.ipoli.android.app.ui;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import io.ipoli.android.R;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 6/1/16.
 */
public class EmptyStateRecyclerView extends RecyclerView {
    @Nullable
    private View emptyView;

    public EmptyStateRecyclerView(Context context) {
        super(context);
    }

    public EmptyStateRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EmptyStateRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    void checkIfEmpty() {
        if (getAdapter() == null) {
            return;
        }
        if (emptyView != null) {
            emptyView.setVisibility(getAdapter().getItemCount() > 0 ? GONE : VISIBLE);
        }
    }

    @NonNull
    final AdapterDataObserver observer = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            super.onChanged();
            checkIfEmpty();
        }
    };

    @Override
    public void setAdapter(@Nullable Adapter adapter) {
        final Adapter oldAdapter = getAdapter();
        if (oldAdapter != null) {
            oldAdapter.unregisterAdapterDataObserver(observer);
        }
        super.setAdapter(adapter);
        if (adapter != null) {
            adapter.registerAdapterDataObserver(observer);
        }
        checkIfEmpty();
    }

    public void setEmptyView(@Nullable View emptyView) {
        this.emptyView = emptyView;
    }

    public void setEmptyView(ViewGroup rootView, @StringRes int text, @DrawableRes int image) {
        View emptyView = LayoutInflater.from(getContext()).inflate(R.layout.list_empty_view, rootView, false);
        TextView textView = (TextView) emptyView.findViewById(R.id.empty_text);
        ImageView imageView = (ImageView) emptyView.findViewById(R.id.empty_image);
        textView.setText(text);
        imageView.setImageResource(image);

        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        emptyView.setLayoutParams(layoutParams);

        rootView.addView(emptyView);
        setEmptyView(emptyView);
    }
}

package io.ipoli.android.feed.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;

import io.ipoli.android.R;
import io.ipoli.android.app.BaseFragment;
import io.ipoli.android.app.ui.EmptyStateRecyclerView;
import io.ipoli.android.app.ui.LayoutManagerFactory;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.feed.data.Post;
import io.ipoli.android.feed.events.AddQuestFromPostEvent;
import io.ipoli.android.feed.events.DeletePostEvent;
import io.ipoli.android.feed.events.GiveKudosEvent;
import io.ipoli.android.feed.events.ShowProfileEvent;
import io.ipoli.android.feed.ui.PostBinder;
import io.ipoli.android.feed.ui.PostViewHolder;

import static io.ipoli.android.feed.persistence.FirebaseFeedPersistenceService.postsPath;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/2/17.
 */
public class PostListFragment extends BaseFragment {

    private static final String PLAYER_ID = "player_id";
    private FirebaseRecyclerAdapter<Post, PostViewHolder> adapter;
    private String playerId;

    public static PostListFragment newInstance(String playerId) {
        PostListFragment fragment = new PostListFragment();
        Bundle args = new Bundle();
        args.putString(PLAYER_ID, playerId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() == null || StringUtils.isEmpty(getArguments().getString(PLAYER_ID))) {
            throw new IllegalArgumentException("Player id is required");
        }
        playerId = getArguments().getString(PLAYER_ID);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        EmptyStateRecyclerView postList = (EmptyStateRecyclerView) inflater.inflate(R.layout.fragment_profile_list, container, false);

        postList.setLayoutManager(LayoutManagerFactory.createReverseLayoutManager(getContext()));

        DatabaseReference postsReference = postsPath().toReference();
        adapter = new FirebaseRecyclerAdapter<Post, PostViewHolder>(Post.class,
                R.layout.feed_post_item,
                PostViewHolder.class,
                postsReference.orderByChild("playerId").equalTo(playerId).limitToLast(100)) {
            @Override
            protected void populateViewHolder(PostViewHolder holder, Post post, int position) {
                PostBinder.bind(holder, post, playerId);
                holder.itemView.setOnClickListener(v -> postEvent(new ShowProfileEvent(post.getPlayerId())));
                holder.giveKudos.setOnClickListener(v -> postEvent(new GiveKudosEvent(post)));
                holder.addQuest.setOnClickListener(v -> postEvent(new AddQuestFromPostEvent(post)));
                if (playerId.equals(getPlayerId())) {
                    holder.delete.setVisibility(View.VISIBLE);
                    holder.delete.setOnClickListener(v -> {
                        showDeleteConfirmationDialog(post);
                    });
                } else {
                    holder.delete.setVisibility(View.GONE);
                }
            }
        };

        postList.setAdapter(adapter);
        return postList;
    }

    private void showDeleteConfirmationDialog(Post post) {
        AlertDialog d = new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.dialog_delete_post_title))
                .setMessage(getString(R.string.dialog_delete_post_message))
                .setPositiveButton(getString(R.string.dialog_yes), (dialog, which) -> {
                    postEvent(new DeletePostEvent(post));
                })
                .setNegativeButton(getString(R.string.dialog_no), (dialog, which) -> {
                })
                .create();
        d.show();
    }

    @Override
    public void onDestroyView() {
        adapter.cleanup();
        super.onDestroyView();
    }
}
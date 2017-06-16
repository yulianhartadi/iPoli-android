package io.ipoli.android.feed.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.activities.BaseActivity;
import io.ipoli.android.feed.data.Post;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/16/17.
 */
public class FeedActivity extends BaseActivity {

    @BindView(R.id.feed_list)
    RecyclerView feedList;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private FirebaseRecyclerAdapter<Post, PostViewHolder> adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        App.getAppComponent(this).inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        feedList.setLayoutManager(layoutManager);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("/posts");
        adapter = new FirebaseRecyclerAdapter<Post, PostViewHolder>(Post.class,
                R.layout.feed_post_item,
                PostViewHolder.class,
                ref) {
            @Override
            protected void populateViewHolder(PostViewHolder holder, Post post, int position) {
                holder.title.setText(post.getTitle());
            }
        };

        feedList.setAdapter(adapter);

        Post post = new Post();
        post.setTitle("Hello world, mirizlivke!");

        ref.push().setValue(post);

        Post post1 = new Post();
        post1.setTitle("Poli is selqnka!");

        ref.push().setValue(post1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        adapter.cleanup();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.post_title)
        public TextView title;

        public PostViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    @Override
    protected boolean useParentOptionsMenu() {
        return false;
    }
}

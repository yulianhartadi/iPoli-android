package io.ipoli.android.feed.persistence;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.feed.data.Post;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/25/17.
 */
public class FirebaseFeedPersistenceService implements FeedPersistenceService {

    private final FirebaseDatabase database;

    public FirebaseFeedPersistenceService(FirebaseDatabase database) {
        this.database = database;
    }

    @Override
    public void addPost(Post post) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference postsRef = database.getReference("/posts");
        DatabaseReference ref = postsRef.push();
        post.setId(ref.getKey());
        ref.setValue(post);
    }

    @Override
    public void updatePost(Post post) {
        post.setUpdatedAt(DateUtils.nowUTC().getTime());
        DatabaseReference postRef = database.getReference("/posts/" + post.getId());
        postRef.setValue(post);
    }
}

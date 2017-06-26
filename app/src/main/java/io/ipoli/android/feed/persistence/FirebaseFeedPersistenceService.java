package io.ipoli.android.feed.persistence;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import io.ipoli.android.app.persistence.OnDataChangedListener;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.feed.data.PlayerProfile;
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

    @Override
    public void createPlayerProfile(PlayerProfile playerProfile) {
        DatabaseReference profileRef = database.getReference("/profiles/" + playerProfile.getId());
        profileRef.setValue(playerProfile);
    }

    @Override
    public void findPlayerProfile(String playerId, OnDataChangedListener<PlayerProfile> listener) {
        DatabaseReference profileRef = database.getReference("/profiles/" + playerId);
        profileRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listener.onDataChanged(dataSnapshot.getValue(PlayerProfile.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}

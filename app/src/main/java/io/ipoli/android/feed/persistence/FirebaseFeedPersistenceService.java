package io.ipoli.android.feed.persistence;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

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
    private final Map<ValueEventListener, Query> valueListeners;

    public FirebaseFeedPersistenceService(FirebaseDatabase database) {
        this.database = database;
        valueListeners = new HashMap<>();
    }

    @Override
    public void addPost(Post post, String playerId) {
        DatabaseReference postsRef = database.getReference("/posts");
        DatabaseReference ref = postsRef.push();
        post.setId(ref.getKey());

        Map<String, Object> update = new HashMap<>();
        update.put("/posts/" + post.getId(), post);
        update.put("/profiles/" + playerId + "/posts/" + post.getId(), post);
        database.getReference().updateChildren(update);
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

    @Override
    public void listenForPlayerProfile(String playerId, OnDataChangedListener<PlayerProfile> listener) {
        DatabaseReference profileRef = database.getReference("/profiles/" + playerId);
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listener.onDataChanged(dataSnapshot.getValue(PlayerProfile.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        valueListeners.put(valueEventListener, profileRef);
        profileRef.addValueEventListener(valueEventListener);
    }

    @Override
    public void removeAllListeners() {
        for (ValueEventListener valueEventListener : valueListeners.keySet()) {
            Query query = valueListeners.get(valueEventListener);
            query.removeEventListener(valueEventListener);
        }
        valueListeners.clear();
    }

    @Override
    public void removeLike(Post post, String playerId) {
        Map<String, Object> update = new HashMap<>();
        update.put("/posts/" + post.getId() + "/likes/" + playerId, null);
        update.put("/profiles/" + post.getPlayerId() + "/posts/" + post.getId() + "/likes/" + playerId, null);
        database.getReference().updateChildren(update);
    }

    @Override
    public void addLike(Post post, String playerId) {
        Map<String, Object> update = new HashMap<>();
        update.put("/posts/" + post.getId() + "/likes/" + playerId, true);
        update.put("/profiles/" + post.getPlayerId() + "/posts/" + post.getId() + "/likes/" + playerId, true);
        database.getReference().updateChildren(update);
    }

    @Override
    public void addPostToPlayer(Post post, String playerId) {
        Map<String, Object> update = new HashMap<>();
        update.put("/posts/" + post.getId() + "/addedBy/" + playerId, true);
        update.put("/profiles/" + post.getPlayerId() + "/posts/" + post.getId() + "/addedBy/" + playerId, true);
        database.getReference().updateChildren(update);
    }
}
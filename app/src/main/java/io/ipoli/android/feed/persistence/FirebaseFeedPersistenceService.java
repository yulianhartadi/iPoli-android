package io.ipoli.android.feed.persistence;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import io.ipoli.android.app.persistence.OnDataChangedListener;
import io.ipoli.android.feed.data.Post;
import io.ipoli.android.feed.data.Profile;

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
    public void addPost(Post post) {
        DatabaseReference postsRef = database.getReference("/posts");
        DatabaseReference ref = postsRef.push();
        post.setId(ref.getKey());

        Map<String, Object> update = new HashMap<>();
        update.put("/posts/" + post.getId(), post);
        update.put("/profiles/" + post.getPlayerId() + "/postedQuests/" + post.getQuestId(), true);
        database.getReference().updateChildren(update);
    }

    @Override
    public void createProfile(Profile profile) {
        Map<String, Object> update = new HashMap<>();
        update.put("/profiles/" + profile.getId(), profile);
        update.put("/usernames/" + profile.getUsername().toLowerCase(), profile.getId());
        database.getReference().updateChildren(update);
    }

    @Override
    public void findProfile(String playerId, OnDataChangedListener<Profile> listener) {
        DatabaseReference profileRef = database.getReference("/profiles/" + playerId);
        profileRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listener.onDataChanged(dataSnapshot.getValue(Profile.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void listenForProfile(String playerId, OnDataChangedListener<Profile> listener) {
        DatabaseReference profileRef = database.getReference("/profiles/" + playerId);
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listener.onDataChanged(dataSnapshot.getValue(Profile.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        valueListeners.put(valueEventListener, profileRef);
        profileRef.addValueEventListener(valueEventListener);
    }

    @Override
    public void isUsernameAvailable(String username, OnDataChangedListener<Boolean> listener) {
        DatabaseReference usernamesRef = database.getReference("/usernames/" + username.toLowerCase());
        usernamesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Object result = dataSnapshot.getValue();
                if (result == null) {
                    listener.onDataChanged(true);
                } else {
                    listener.onDataChanged(false);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("Tag", "Taggy");
            }
        });
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
        database.getReference().updateChildren(update);
    }

    @Override
    public void addLike(Post post, String playerId) {
        Map<String, Object> update = new HashMap<>();
        update.put("/posts/" + post.getId() + "/likes/" + playerId, true);
        database.getReference().updateChildren(update);
    }

    @Override
    public void addPostToPlayer(Post post, String playerId) {
        Map<String, Object> update = new HashMap<>();
        update.put("/posts/" + post.getId() + "/addedBy/" + playerId, true);
        database.getReference().updateChildren(update);
    }

    @Override
    public void unfollow(Profile profile, String playerId) {
        Map<String, Object> update = new HashMap<>();
        update.put("/profiles/" + profile.getId() + "/followers/" + playerId, null);
        update.put("/profiles/" + playerId + "/following/" + profile.getId(), null);
        database.getReference().updateChildren(update);
    }

    @Override
    public void follow(Profile profile, String playerId) {
        Map<String, Object> update = new HashMap<>();
        update.put("/profiles/" + profile.getId() + "/followers/" + playerId, true);
        update.put("/profiles/" + playerId + "/following/" + profile.getId(), true);
        database.getReference().updateChildren(update);
    }
}
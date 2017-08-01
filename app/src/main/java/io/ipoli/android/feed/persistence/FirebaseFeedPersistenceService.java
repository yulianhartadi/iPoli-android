package io.ipoli.android.feed.persistence;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.otto.Bus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.ipoli.android.achievement.Achievement;
import io.ipoli.android.app.events.AppErrorEvent;
import io.ipoli.android.app.persistence.FirebasePath;
import io.ipoli.android.app.persistence.OnDataChangedListener;
import io.ipoli.android.feed.data.Post;
import io.ipoli.android.feed.data.Profile;
import io.ipoli.android.player.data.Player;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/25/17.
 */
public class FirebaseFeedPersistenceService implements FeedPersistenceService {

    public static final String ROOT_PATH = "/v2";

    private final FirebaseDatabase database;
    private final Bus eventBus;
    private final Map<ValueEventListener, Query> valueListeners;

    public static FirebasePath rootPath() {
        return new FirebasePath(ROOT_PATH);
    }

    public static FirebasePath postsPath() {
        return rootPath().add("posts");
    }

    public static FirebasePath profilesPath() {
        return rootPath().add("profiles");
    }

    public static FirebasePath achievementsPath() {
        return rootPath().add("achievements");
    }

    public static FirebasePath postPath(String postId) {
        return postsPath().add(postId);
    }

    public static FirebasePath profilePath(String profileId) {
        return rootPath().add("profiles").add(profileId);
    }

    public static FirebasePath usernamePath(String username) {
        return rootPath().add("usernames").add(username);
    }

    public static FirebasePath achievementPath(Integer code) {
        return rootPath().add("achievements").add(code);
    }

    public FirebaseFeedPersistenceService(FirebaseDatabase database, Bus eventBus) {
        this.database = database;
        this.eventBus = eventBus;
        valueListeners = new HashMap<>();
    }

    @Override
    public void addPost(Post post) {
        DatabaseReference postsReference = postsPath().toReference(database);
        DatabaseReference ref = postsReference.push();
        post.setId(ref.getKey());

        Map<String, Object> update = new HashMap<>();
        postPath(post.getId()).update(update).withValue(post);
        profilePath(post.getPlayerId()).add("posts").add(post.getId()).update(update).withValue(post.getQuestId());
        database.getReference().updateChildren(update);
    }

    @Override
    public void createProfile(Profile profile) {
        Map<String, Object> update = new HashMap<>();
        profilePath(profile.getId()).update(update).withValue(profile);
        usernamePath(profile.getUsername().toLowerCase()).update(update).withValue(profile.getId());
        database.getReference().updateChildren(update);
    }

    @Override
    public void findProfile(String playerId, OnDataChangedListener<Profile> listener) {
        DatabaseReference profileRef = profilePath(playerId).toReference(database);
        profileRef.addListenerForSingleValueEvent(new FirebaseValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listener.onDataChanged(dataSnapshot.getValue(Profile.class));
            }
        });
    }

    @Override
    public void listenForProfile(String playerId, OnDataChangedListener<Profile> listener) {
        DatabaseReference profileRef = profilePath(playerId).toReference(database);
        ValueEventListener valueEventListener = new FirebaseValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listener.onDataChanged(dataSnapshot.getValue(Profile.class));
            }
        };
        valueListeners.put(valueEventListener, profileRef);
        profileRef.addValueEventListener(valueEventListener);
    }

    @Override
    public void isUsernameAvailable(String username, OnDataChangedListener<Boolean> listener) {
        DatabaseReference usernameRef = usernamePath(username.toLowerCase()).toReference(database);
        usernameRef.addListenerForSingleValueEvent(new FirebaseValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Object result = dataSnapshot.getValue();
                if (result == null) {
                    listener.onDataChanged(true);
                } else {
                    listener.onDataChanged(false);
                }
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
    public void removeKudos(Post post, String playerId) {
        Map<String, Object> update = new HashMap<>();
        postPath(post.getId()).add("kudos").add(playerId).update(update).withValue(null);
        database.getReference().updateChildren(update);
    }

    @Override
    public void addKudos(Post post, String playerId) {
        Map<String, Object> update = new HashMap<>();
        postPath(post.getId()).add("kudos").add(playerId).update(update).withValue(true);
        database.getReference().updateChildren(update);
    }

    @Override
    public void addPostToPlayer(Post post, String playerId) {
        Map<String, Object> update = new HashMap<>();
        postPath(post.getId()).add("addedBy").add(playerId).update(update).withValue(true);
        database.getReference().updateChildren(update);
    }

    @Override
    public void unfollow(String profileId, String playerId) {
        Map<String, Object> update = new HashMap<>();
        profilePath(profileId).add("followers").add(playerId).update(update).withValue(null);
        profilePath(playerId).add("following").add(profileId).update(update).withValue(null);
        database.getReference().updateChildren(update);
    }

    @Override
    public void follow(String profileId, String playerId) {
        Map<String, Object> update = new HashMap<>();
        profilePath(profileId).add("followers").add(playerId).update(update).withValue(true);
        profilePath(playerId).add("following").add(profileId).update(update).withValue(true);
        database.getReference().updateChildren(update);
    }

    @Override
    public void updateProfile(Profile profile, Player player) {
        Map<String, Object> update = new HashMap<>();
        FirebasePath profilePath = profilePath(profile.getId());
        profilePath.add("displayName").update(update).withValue(player.getFullDisplayName());
        profilePath.add("bio").update(update).withValue(player.getBio());
        profilePath.add("level").update(update).withValue(player.getLevel());
        profilePath.add("experience").update(update).withValue(player.getExperience());
        profilePath.add("avatarCode").update(update).withValue(player.getAvatarCode());
        profilePath.add("petName").update(update).withValue(player.getPet().getName());
        profilePath.add("petAvatarCode").update(update).withValue(player.getPet().getAvatarCode());
        profilePath.add("petState").update(update).withValue(player.getPet().getState().name());
        for (String postId : profile.getPosts().keySet()) {
            postPath(postId).add("playerLevel").update(update).withValue(player.getLevel());
            postPath(postId).add("playerDisplayName").update(update).withValue(player.getFullDisplayName());
            postPath(postId).add("playerAvatarCode").update(update).withValue(player.getAvatarCode());
        }
        for (Map.Entry<Integer, Long> entry : player.getAchievements().entrySet()) {
            profilePath.add("achievements").add(Achievement.get(entry.getKey()).name()).update(update).withValue(entry.getValue());
        }
        database.getReference().updateChildren(update);
    }

    @Override
    public void deletePost(Post post) {
        Map<String, Object> update = new HashMap<>();
        postPath(post.getId()).update(update).withValue(null);
        profilePath(post.getPlayerId()).add("posts").add(post.getId()).update(update).withValue(null);
        database.getReference().updateChildren(update);
    }

    @Override
    public void incrementAchievements(List<Achievement> unlockedAchievements) {
        for (Achievement achievement : unlockedAchievements) {
            DatabaseReference ref = achievementPath(achievement.code).toReference(database);
            ref.addListenerForSingleValueEvent(new FirebaseValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Long result = dataSnapshot.getValue(Long.class);
                    long count = result != null ? result + 1 : 1;
                    ref.setValue(count);
                }
            });
        }
    }

    private abstract class FirebaseValueEventListener implements ValueEventListener {

        @Override
        public void onCancelled(DatabaseError databaseError) {
            eventBus.post(new AppErrorEvent(databaseError.toException()));
        }
    }
}
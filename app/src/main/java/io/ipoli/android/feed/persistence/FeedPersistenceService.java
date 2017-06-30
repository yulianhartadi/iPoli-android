package io.ipoli.android.feed.persistence;

import io.ipoli.android.app.persistence.OnDataChangedListener;
import io.ipoli.android.feed.data.Post;
import io.ipoli.android.feed.data.Profile;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/25/17.
 */
public interface FeedPersistenceService {

    void addPost(Post post);

    void createProfile(Profile profile);

    void findProfile(String playerId, OnDataChangedListener<Profile> listener);

    void listenForProfile(String playerId, OnDataChangedListener<Profile> listener);

    void isUsernameAvailable(String username, OnDataChangedListener<Boolean> listener);

    void removeAllListeners();

    void removeLike(Post post, String playerId);

    void addLike(Post post, String playerId);

    void addPostToPlayer(Post post, String playerId);

    void unfollow(Profile profile, String playerId);

    void follow(Profile profile, String playerId);
}

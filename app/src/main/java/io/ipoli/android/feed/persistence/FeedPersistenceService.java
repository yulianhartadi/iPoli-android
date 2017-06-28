package io.ipoli.android.feed.persistence;

import io.ipoli.android.app.persistence.OnDataChangedListener;
import io.ipoli.android.feed.data.PlayerProfile;
import io.ipoli.android.feed.data.Post;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/25/17.
 */
public interface FeedPersistenceService {

    void addPost(Post post);

    void createPlayerProfile(PlayerProfile playerProfile);

    void findPlayerProfile(String playerId, OnDataChangedListener<PlayerProfile> listener);

    void listenForPlayerProfile(String playerId, OnDataChangedListener<PlayerProfile> listener);

    void removeAllListeners();

    void removeLike(Post post, String playerId);

    void addLike(Post post, String playerId);

    void addPostToPlayer(Post post, String playerId);

    void unfollow(PlayerProfile playerProfile, String playerId);

    void follow(PlayerProfile playerProfile, String playerId);
}

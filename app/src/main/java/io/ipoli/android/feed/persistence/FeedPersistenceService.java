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

    void updatePost(Post post);

    void createPlayerProfile(PlayerProfile playerProfile);

    void findPlayerProfile(String playerId, OnDataChangedListener<PlayerProfile> listener);
}

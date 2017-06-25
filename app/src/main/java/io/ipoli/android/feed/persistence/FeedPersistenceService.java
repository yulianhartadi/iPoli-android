package io.ipoli.android.feed.persistence;

import io.ipoli.android.feed.data.Post;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/25/17.
 */
public interface FeedPersistenceService {

    void addPost(Post post);
}

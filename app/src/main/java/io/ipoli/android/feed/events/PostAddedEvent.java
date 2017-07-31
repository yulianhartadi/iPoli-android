package io.ipoli.android.feed.events;

import io.ipoli.android.feed.data.Post;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/25/17.
 */
public class PostAddedEvent {

    public final Post post;

    public PostAddedEvent(Post post) {
        this.post = post;
    }
}

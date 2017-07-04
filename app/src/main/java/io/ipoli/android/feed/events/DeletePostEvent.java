package io.ipoli.android.feed.events;

import io.ipoli.android.feed.data.Post;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/4/17.
 */
public class DeletePostEvent {

    public final Post post;

    public DeletePostEvent(Post post) {
        this.post = post;
    }
}

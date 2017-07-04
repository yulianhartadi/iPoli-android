package io.ipoli.android.feed.events;

import io.ipoli.android.feed.data.Post;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/2/17.
 */
public class GiveKudosEvent {
    public final Post post;

    public GiveKudosEvent(Post post) {
        this.post = post;
    }
}

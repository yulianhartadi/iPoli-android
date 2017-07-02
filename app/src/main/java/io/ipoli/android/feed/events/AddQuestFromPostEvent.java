package io.ipoli.android.feed.events;

import io.ipoli.android.feed.data.Post;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/2/17.
 */
public class AddQuestFromPostEvent {
    public final Post post;

    public AddQuestFromPostEvent(Post post) {
        this.post = post;
    }
}

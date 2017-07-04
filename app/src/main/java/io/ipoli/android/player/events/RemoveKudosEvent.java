package io.ipoli.android.player.events;

import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.feed.data.Post;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 7/4/17.
 */

public class RemoveKudosEvent {
    public final Post post;
    public final EventSource source;

    public RemoveKudosEvent(Post post, EventSource source) {
        this.post = post;
        this.source = source;
    }
}

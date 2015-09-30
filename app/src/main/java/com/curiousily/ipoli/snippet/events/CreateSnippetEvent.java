package com.curiousily.ipoli.snippet.events;

import com.curiousily.ipoli.snippet.Snippet;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/10/15.
 */
public class CreateSnippetEvent {
    public final Snippet snippet;

    public CreateSnippetEvent(Snippet snippet) {
        this.snippet = snippet;
    }
}

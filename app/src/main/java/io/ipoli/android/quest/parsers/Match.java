package io.ipoli.android.quest.parsers;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/4/16.
 */
public class Match {
    public String text;
    public int start;
    public int end;

    public Match(String text, int start, int end) {
        this.text = text;
        this.start = start;
        this.end = end;
    }
}

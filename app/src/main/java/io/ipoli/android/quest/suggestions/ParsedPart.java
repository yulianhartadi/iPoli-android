package io.ipoli.android.quest.suggestions;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/29/16.
 */
public class ParsedPart {
    public int startIdx = 0;
    public int endIdx = 0;
    public TextEntityType type;
    public boolean isPartial = false;

    public ParsedPart() {
    }

    public ParsedPart(int startIdx, int endIdx, TextEntityType type, boolean isPartial) {
        this.startIdx = startIdx;
        this.endIdx = endIdx;
        this.type = type;
        this.isPartial = isPartial;
    }
}

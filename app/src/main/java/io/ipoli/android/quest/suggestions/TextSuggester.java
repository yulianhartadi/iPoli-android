package io.ipoli.android.quest.suggestions;

import java.util.List;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/27/16.
 */
public interface TextSuggester {

    SuggesterResult parse(String text);

    List<AddQuestSuggestion> getSuggestions();
}

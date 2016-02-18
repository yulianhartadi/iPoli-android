package io.ipoli.android.app.ui;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/18/16.
 */
public class AddQuestAutocompleteTextView extends AutoCompleteTextView {
    public AddQuestAutocompleteTextView(Context context) {
        super(context);
        initUI();
    }

    public AddQuestAutocompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initUI();
    }

    public AddQuestAutocompleteTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initUI();
    }

    public AddQuestAutocompleteTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initUI();
    }

    private void initUI() {
        int removed = this.getInputType() ^ InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE;
        this.setInputType(removed);
    }

    @Override
    protected void replaceText(CharSequence text) {

    }
}

package io.ipoli.android.quest.ui;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/18/16.
 */
public class AddQuestAutocompleteTextView extends AutoCompleteTextView {
    private List<OnSelectionChangedListener> selectionChangedListeners = new ArrayList<>();

    public AddQuestAutocompleteTextView(Context context) {
        super(context);
        selectionChangedListeners = new ArrayList<>();
        initUI();
    }

    public AddQuestAutocompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        selectionChangedListeners = new ArrayList<>();
        initUI();
    }

    public AddQuestAutocompleteTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        selectionChangedListeners = new ArrayList<>();
        initUI();
    }

    public AddQuestAutocompleteTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        selectionChangedListeners = new ArrayList<>();
        initUI();
    }

    private void initUI() {
        int removed = this.getInputType() ^ InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE;
        this.setInputType(removed);
    }

    @Override
    protected void replaceText(CharSequence text) {

    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        if(selectionChangedListeners == null) {
            return;
        }
        for(OnSelectionChangedListener l : selectionChangedListeners) {
            l.onSelectionChanged(selStart, selEnd);
        }
    }

    public void addOnSelectionChangedListener(OnSelectionChangedListener listener) {
        selectionChangedListeners.add(listener);
    }

    public void removeOnSelectionChangedListener(OnSelectionChangedListener listener) {
        selectionChangedListeners.remove(listener);
    }

    public interface OnSelectionChangedListener {
        void onSelectionChanged(int selStart, int selEnd);
    }
}

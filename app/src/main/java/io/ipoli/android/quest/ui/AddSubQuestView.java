package io.ipoli.android.quest.ui;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.ipoli.android.R;
import io.ipoli.android.app.utils.KeyboardUtils;
import io.ipoli.android.app.utils.StringUtils;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 6/27/16.
 */
public class AddSubQuestView extends RelativeLayout implements View.OnFocusChangeListener, View.OnClickListener {
    private List<OnSubQuestAddedListener> subQuestAddedListeners = new ArrayList<>();

    public interface OnSubQuestAddedListener {
        void onSubQuestAdded(String name);
    }

    private ViewGroup container;
    private TextInputEditText editText;
    private ImageButton clearAddSubQuest;
    private TextView addButton;

    public AddSubQuestView(Context context) {
        super(context);
        if (!isInEditMode()) {
            initUI(context);
        }
    }

    public AddSubQuestView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            initUI(context);
        }
    }

    private void initUI(Context context) {
        View view = LayoutInflater.from(context).inflate(
                R.layout.layout_add_sub_quest, this);

        container = (ViewGroup) view.findViewById(R.id.add_sub_quest_container);
        editText = (TextInputEditText) view.findViewById(R.id.add_sub_quest);
        addButton = (TextView) view.findViewById(R.id.add_sub_quest_button);
        clearAddSubQuest = (ImageButton) view.findViewById(R.id.add_sub_quest_clear);

        hideUnderline(editText);
//        editText.setOnFocusChangeListener(this);
        addButton.setOnClickListener(this);
        editText.setOnEditorActionListener((v, actionId, event) -> onEditorAction(actionId));
        clearAddSubQuest.setOnClickListener(v -> setInViewMode());
//        container.requestFocus();
    }

    private boolean onEditorAction(int actionId) {
        int result = actionId & EditorInfo.IME_MASK_ACTION;
        if (result == EditorInfo.IME_ACTION_DONE) {
            String name = editText.getText().toString();
            Log.d("AAA focusable", String.valueOf(editText.isFocusableInTouchMode()));
            if (StringUtils.isEmpty(name)) {
                setInViewMode();
            } else {
                setInEditMode();
                for (OnSubQuestAddedListener l : subQuestAddedListeners) {
                    l.onSubQuestAdded(name);
                }
            }
            return true;
        } else {
            return false;
        }
    }

    private void showUnderline(View view) {
        view.getBackground().clearColorFilter();
    }

    private void hideUnderline(View view) {
        view.getBackground().setColorFilter(ContextCompat.getColor(getContext(), android.R.color.transparent), PorterDuff.Mode.SRC_IN);
    }

    @Override
    public void onFocusChange(View view, boolean isFocused) {
//        if (editText == null) {
//            return;
//        }
//        String text = editText.getText().toString();
//        if (isFocused) {
//            showUnderline(editText);
//            if (text.equals(getContext().getString(R.string.add_sub_quest))) {
//                setInEditMode();
//            }
//            editText.requestFocus();
//        } else {
//            hideUnderline(editText);
//            if (StringUtils.isEmpty(text)) {
//                setInViewMode();
//            }
//        }
    }

    @Override
    public void onClick(View v) {
        setInEditMode();
    }


    private void setInViewMode() {
        editText.setText(getContext().getString(R.string.add_sub_quest));
        hideUnderline(editText);
        KeyboardUtils.hideKeyboard(getContext(), editText);
        container.requestFocus();
        addButton.setVisibility(VISIBLE);
        editText.setVisibility(GONE);
        clearAddSubQuest.setVisibility(View.INVISIBLE);
    }

    public void setInEditMode() {
                KeyboardUtils.showKeyboard(getContext());
        editText.postDelayed(() -> {
            if(!editText.isFocused()) {
                editText.requestFocus();
            }
        }, 100);
        editText.setText("");
        showUnderline(editText);
        addButton.setVisibility(GONE);
        editText.setVisibility(VISIBLE);
        clearAddSubQuest.setVisibility(View.VISIBLE);
    }

    public void addSubQuestAddedListener(OnSubQuestAddedListener listener) {
        subQuestAddedListeners.add(listener);
    }

    public void removeSubQuestAddedListener(OnSubQuestAddedListener listener) {
        subQuestAddedListeners.remove(listener);
    }

    public void setSubQuestAddedListener(OnSubQuestAddedListener listener) {
        if (!subQuestAddedListeners.contains(listener)) {
            subQuestAddedListeners.add(listener);
        }
    }

}

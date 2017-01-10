package io.ipoli.android.quest.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
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
public class AddSubQuestView extends RelativeLayout implements View.OnClickListener {
    private List<OnSubQuestAddedListener> subQuestAddedListeners = new ArrayList<>();
    private List<OnAddSubQuestClosedListener> addSubQuestClosedListeners = new ArrayList<>();

    public interface OnSubQuestAddedListener {
        void onSubQuestAdded(String name);
    }

    public interface OnAddSubQuestClosedListener {
        void onAddSubQuestClosed();
    }

    private ViewGroup container;
    private TextInputEditText editText;
    private ImageButton clearAddSubQuest;
    private TextView addButton;

    private boolean showIcon = true;
    private Drawable closeIcon;
    private int editTextLayout;


    public AddSubQuestView(Context context) {
        super(context);
        if (!isInEditMode()) {
            initUI(context);
        }
    }

    public AddSubQuestView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray typedArray = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.AddSubQuestView,
                0, 0);

        try {
            showIcon = typedArray.getBoolean(R.styleable.AddSubQuestView_showIcon, true);
            closeIcon = typedArray.getDrawable(R.styleable.AddSubQuestView_closeIcon);
            editTextLayout = typedArray.getResourceId(R.styleable.AddSubQuestView_editTextLayout, R.layout.sub_quest_item_accent_edit_text);
        } finally {
            typedArray.recycle();
        }

        if (!isInEditMode()) {
            initUI(context);
        }
    }

    private void initUI(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(
                R.layout.layout_add_sub_quest, this);

        container = (ViewGroup) view.findViewById(R.id.add_sub_quest_container);
        ViewGroup editTextContainer = (ViewGroup) view.findViewById(R.id.edit_text_container);
        editTextContainer = (ViewGroup) inflater.inflate(editTextLayout, editTextContainer, true);
        editText = (TextInputEditText) editTextContainer.findViewById(R.id.add_sub_quest);
        addButton = (TextView) view.findViewById(R.id.add_sub_quest_button);
        clearAddSubQuest = (ImageButton) view.findViewById(R.id.add_sub_quest_clear);

        if(!showIcon) {
            view.findViewById(R.id.add_icon).setVisibility(INVISIBLE);
        }
        if(closeIcon != null) {
            clearAddSubQuest.setImageDrawable(closeIcon);
        }

        addButton.setOnClickListener(this);
        editText.setOnEditorActionListener((v, actionId, event) -> onEditorAction(actionId));
        clearAddSubQuest.setOnClickListener(v -> {
            setInViewMode();
            for(OnAddSubQuestClosedListener l : addSubQuestClosedListeners) {
                l.onAddSubQuestClosed();
            }
        });
        setInViewMode();
    }

    private boolean onEditorAction(int actionId) {
        int result = actionId & EditorInfo.IME_MASK_ACTION;
        if (result == EditorInfo.IME_ACTION_DONE) {
            String name = editText.getText().toString();
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
    public void onClick(View v) {
        KeyboardUtils.showKeyboard(getContext());
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

    public void addOnClosedListener(OnAddSubQuestClosedListener listener) {
        addSubQuestClosedListeners.add(listener);
    }

    public void removeOnClosedListener(OnAddSubQuestClosedListener listener) {
        addSubQuestClosedListeners.remove(listener);
    }

    public void setOnClosedListener(OnAddSubQuestClosedListener listener) {
        if (!addSubQuestClosedListeners.contains(listener)) {
            addSubQuestClosedListeners.add(listener);
        }
    }

}

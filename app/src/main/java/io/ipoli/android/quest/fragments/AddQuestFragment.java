package io.ipoli.android.quest.fragments;


import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.ocpsoft.prettytime.nlp.PrettyTimeParser;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnEditorAction;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.utils.NetworkConnectivityUtils;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.quest.QuestContext;
import io.ipoli.android.quest.QuestParser;
import io.ipoli.android.quest.adapters.BaseSuggestionsAdapter;
import io.ipoli.android.quest.adapters.SuggestionsAdapter;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.RecurrentQuest;
import io.ipoli.android.quest.events.ColorLayoutEvent;
import io.ipoli.android.quest.events.NewQuestEvent;
import io.ipoli.android.quest.events.NewRecurrentQuestEvent;
import io.ipoli.android.quest.events.SuggestionAdapterItemClickEvent;
import io.ipoli.android.quest.suggestions.OnSuggestionsUpdatedListener;
import io.ipoli.android.quest.suggestions.ParsedPart;
import io.ipoli.android.quest.suggestions.SuggestionDropDownItem;
import io.ipoli.android.quest.suggestions.SuggestionsManager;
import io.ipoli.android.quest.ui.AddQuestAutocompleteTextView;

public class AddQuestFragment extends Fragment implements TextWatcher, OnSuggestionsUpdatedListener {
    @Inject
    Bus eventBus;

    @Bind(R.id.quest_text)
    AddQuestAutocompleteTextView questText;

    @Bind(R.id.quest_context_name)
    TextView contextName;

    @Bind(R.id.quest_context_container)
    LinearLayout contextContainer;

    private BaseSuggestionsAdapter adapter;

    private final PrettyTimeParser parser = new PrettyTimeParser();

    private QuestContext questContext;

    private SuggestionsManager suggestionsManager;
    private int selectionStartIdx = 0;

    enum TextWatcherState {GUI_CHANGE, FROM_DELETE, AFTER_DELETE, FROM_DROP_DOWN}

    TextWatcherState textWatcherState = TextWatcherState.GUI_CHANGE;

    private boolean isForToday;

    public AddQuestFragment() {
    }

    public static AddQuestFragment newInstance(boolean isToday) {
        AddQuestFragment fragment = new AddQuestFragment();
        Bundle args = new Bundle();
        args.putBoolean(Constants.IS_TODAY_QUEST_EXTRA_KEY, isToday);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isForToday = getArguments().getBoolean(Constants.IS_TODAY_QUEST_EXTRA_KEY);
        }
        setHasOptionsMenu(true);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_quest, container, false);

        ButterKnife.bind(this, view);
        App.getAppComponent(getContext()).inject(this);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.title_activity_add_quest));


        suggestionsManager = new SuggestionsManager(parser);
        suggestionsManager.setSuggestionsUpdatedListener(this);

        questText.addTextChangedListener(this);

        questText.setShowSoftInputOnFocus(true);
        questText.requestFocus();

        if (isForToday) {
            questText.setText(" " + getString(R.string.add_quest_today));
        }
        initUI();
        initContextUI(view);

        questText.setOnClickListener(v -> {
            int selStart = questText.getSelectionStart();
            String text = questText.getText().toString();
            int newSel = suggestionsManager.getSelectionIndex(text, selStart);
            if (newSel != selStart) {
                selectionStartIdx = newSel;
                questText.setSelection(selectionStartIdx);
                colorParsedParts(suggestionsManager.parse(text, 0));
            } else if (Math.abs(selStart - selectionStartIdx) > 1) {
                selectionStartIdx = selStart;
                questText.setSelection(selectionStartIdx);
                colorParsedParts(suggestionsManager.parse(text, selectionStartIdx));
            }
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        eventBus.register(this);
        InputMethodManager mgr = (InputMethodManager) getActivity().getSystemService(getContext().INPUT_METHOD_SERVICE);
        mgr.showSoftInput(questText, InputMethodManager.SHOW_IMPLICIT);
    }

    @Override
    public void onPause() {
        eventBus.unregister(this);
        super.onPause();
    }

    private void initUI() {
        adapter = new SuggestionsAdapter(getContext(), eventBus, suggestionsManager.getSuggestions());
        questText.setAdapter(adapter);
        questText.setThreshold(1);
    }

    private void initContextUI(View view) {
        changeContext(QuestContext.PERSONAL, view);

        final QuestContext[] ctxs = QuestContext.values();
        for (int i = 0; i < contextContainer.getChildCount(); i++) {
            final ImageView iv = (ImageView) contextContainer.getChildAt(i);
            GradientDrawable drawable = (GradientDrawable) iv.getBackground();
            drawable.setColor(ContextCompat.getColor(getContext(), ctxs[i].resLightColor));

            final QuestContext ctx = ctxs[i];
            iv.setOnClickListener(v -> {
                removeSelectedContextCheck(view);
                changeContext(ctx, view);
            });
        }
    }

    private void changeContext(QuestContext ctx, View view) {
        setBackgroundColors(ctx);
        questContext = ctx;
        setSelectedContext(view);
    }

    private void setSelectedContext(View view) {
        getCurrentContextImageView(view).setImageResource(questContext.whiteImage);
        setContextName();
    }

    private void removeSelectedContextCheck(View view) {
        getCurrentContextImageView(view).setImageDrawable(null);
    }

    private ImageView getCurrentContextImageView(View view) {
        String ctxId = "quest_context_" + questContext.name().toLowerCase();
        int ctxResId = getResources().getIdentifier(ctxId, "id", getActivity().getPackageName());
        return (ImageView) view.findViewById(ctxResId);
    }

    private void setBackgroundColors(QuestContext ctx) {
        eventBus.post(new ColorLayoutEvent(ctx));
    }

    private void setContextName() {
        contextName.setText(questContext.name().substring(0, 1) + questContext.name().substring(1).toLowerCase());
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (menu != null) {
            menu.removeItem(R.id.action_feedback);
            menu.removeItem(R.id.action_contact_us);
        }
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.add_quest_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveQuest();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void saveQuest() {
        String text = questText.getText().toString().trim();

        QuestParser qParser = new QuestParser(parser);
        if (qParser.isRecurrent(text)) {
            RecurrentQuest recurrentQuest = qParser.parseRecurrent(text);
            if (recurrentQuest == null) {
                resetQuestText();
                Toast.makeText(getContext(), "Please, add quest name", Toast.LENGTH_LONG).show();
                return;
            }
            recurrentQuest.setContext(questContext.name());
            eventBus.post(new NewRecurrentQuestEvent(recurrentQuest));
            if (!NetworkConnectivityUtils.isConnectedToInternet(getContext())) {
                Toast.makeText(getContext(), R.string.no_internet_habit_added, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), R.string.habit_added, Toast.LENGTH_SHORT).show();
            }
        } else {
            Quest q = qParser.parse(text);
            if (q == null) {
                resetQuestText();
                Toast.makeText(getContext(), "Please, add quest name", Toast.LENGTH_LONG).show();
                return;
            }
            Quest.setContext(q, questContext);
            eventBus.post(new NewQuestEvent(q));
            Toast.makeText(getContext(), R.string.quest_added, Toast.LENGTH_SHORT).show();
        }
        resetQuestText();
    }

    private void resetQuestText() {
        suggestionsManager.setSuggestionsUpdatedListener(null);
        suggestionsManager = new SuggestionsManager(parser);
        suggestionsManager.setSuggestionsUpdatedListener(this);
        questText.setText("");
    }

    @OnEditorAction(R.id.quest_text)
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        int result = actionId & EditorInfo.IME_MASK_ACTION;
        if (result == EditorInfo.IME_ACTION_DONE) {
            saveQuest();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        if (isInsert(count, after) || textWatcherState == TextWatcherState.FROM_DELETE) {
            return;
        }

        SuggestionsManager.TextTransformResult result = suggestionsManager.deleteText(s.toString(), start);
        setTransformedText(result, TextWatcherState.FROM_DELETE);
        textWatcherState = TextWatcherState.AFTER_DELETE;

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        switch (textWatcherState) {
            case FROM_DELETE:
            case FROM_DROP_DOWN:
                List<ParsedPart> parsedParts = suggestionsManager.onTextChange(s.toString(), selectionStartIdx);
                colorParsedParts(parsedParts);
                break;

            case GUI_CHANGE:
                parsedParts = suggestionsManager.onTextChange(s.toString(), questText.getSelectionStart());
                colorParsedParts(parsedParts);
                break;
            case AFTER_DELETE:
                break;
        }

        textWatcherState = TextWatcherState.GUI_CHANGE;
    }

    private boolean isInsert(int replacedLen, int newLen) {
        return newLen >= replacedLen;
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    private void colorParsedParts(List<ParsedPart> parsedParts) {
        Editable editable = questText.getText();
        clearSpans(editable);
        for (ParsedPart p : parsedParts) {
            int color = p.isPartial ? R.color.md_red_A200 : R.color.md_blue_500;
            markText(editable, p.startIdx, p.endIdx, color);
        }
    }

    private void clearSpans(Editable editable) {
        BackgroundColorSpan[] backgroundSpansToRemove = editable.getSpans(0, editable.toString().length(), BackgroundColorSpan.class);
        for (BackgroundColorSpan span : backgroundSpansToRemove) {
            editable.removeSpan(span);
        }
        ForegroundColorSpan[] foregroundSpansToRemove = editable.getSpans(0, editable.toString().length(), ForegroundColorSpan.class);
        for (ForegroundColorSpan span : foregroundSpansToRemove) {
            editable.removeSpan(span);
        }
    }

    private void markText(Editable text, int startIdx, int endIdx, int colorRes) {
        text.setSpan(new BackgroundColorSpan(ContextCompat.getColor(getContext(), colorRes)), startIdx, endIdx + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.md_white)), startIdx, endIdx + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    @Subscribe
    public void onAdapterItemClick(SuggestionAdapterItemClickEvent e) {
        SuggestionDropDownItem suggestion = e.suggestionItem;
        String text = questText.getText().toString();
        int selectionIndex = questText.getSelectionStart();
        SuggestionsManager.TextTransformResult result = suggestionsManager.onSuggestionItemClick(text, suggestion, selectionIndex);
        setTransformedText(result, TextWatcherState.FROM_DROP_DOWN);
        if (suggestion.nextTextEntityType != null) {
            List<ParsedPart> parsedParts = suggestionsManager.parse(result.text, result.selectionIndex);
            ParsedPart partialPart = suggestionsManager.findPartialPart(parsedParts);
            String parsedText = partialPart == null ? "" : StringUtils.substring(result.text, partialPart.startIdx, partialPart.endIdx);
            suggestionsManager.changeCurrentSuggestionsProvider(suggestion.nextTextEntityType, parsedText);
        }
    }

    private void setTransformedText(SuggestionsManager.TextTransformResult result, TextWatcherState state) {
        textWatcherState = state;
        selectionStartIdx = result.selectionIndex;
        questText.setText(result.text);
        questText.setSelection(result.selectionIndex);
    }

    @Override
    public void onSuggestionsUpdated() {
        if (adapter != null) {
            adapter.setSuggestions(suggestionsManager.getSuggestions());
        }
    }
}

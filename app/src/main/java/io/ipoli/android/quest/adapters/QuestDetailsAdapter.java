package io.ipoli.android.quest.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.events.ItemActionsShownEvent;
import io.ipoli.android.app.utils.KeyboardUtils;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.note.data.Note;
import io.ipoli.android.note.events.OpenNoteEvent;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.SubQuest;
import io.ipoli.android.quest.events.EditNoteRequestEvent;
import io.ipoli.android.quest.events.subquests.CompleteSubQuestEvent;
import io.ipoli.android.quest.events.subquests.DeleteSubQuestEvent;
import io.ipoli.android.quest.events.subquests.NewSubQuestEvent;
import io.ipoli.android.quest.events.subquests.UndoCompleteSubQuestEvent;
import io.ipoli.android.quest.events.subquests.UpdateSubQuestNameEvent;
import io.ipoli.android.quest.ui.AddSubQuestView;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/26/16.
 */

public class QuestDetailsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements AddSubQuestView.OnSubQuestAddedListener {

    public static final int HEADER_ITEM_VIEW_TYPE = 0;
    public static final int SUB_QUEST_ITEM_VIEW_TYPE = 1;
    public static final int ADD_SUB_QUEST_ITEM_VIEW_TYPE = 2;
    public static final int NOTE_ITEM_VIEW_TYPE = 3;
    public static final int NOTE_LINK_ITEM_VIEW_TYPE = 4;
    public static final int EMPTY_NOTE_HINT_VIEW_TYPE = 5;

    private final Context context;
    private final Bus eventBus;

    private List<Object> items;

    public QuestDetailsAdapter(Context context, Quest quest, Bus eventBus) {
        this.context = context;
        this.eventBus = eventBus;
        createItems(quest);
    }

    private void createItems(Quest quest) {
        items = new ArrayList<>();
        items.add("Sub Quests");
        for (SubQuest sq : quest.getSubQuests()) {
            items.add(sq);
        }

        items.add(new AddSubQuestButton(context.getString(R.string.add_sub_quest)));
        items.add("Notes");
        if (quest.getTextNotes().isEmpty()) {
            items.add(new EmptyNoteHint("Tap to add a note"));
        }
        for (Note note : quest.getNotes()) {
            items.add(note);
        }
    }

    public void updateData(Quest quest) {
        createItems(quest);
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {

            case HEADER_ITEM_VIEW_TYPE:
                return new HeaderViewHolder(inflater.inflate(R.layout.quest_header_item, parent, false));

            case SUB_QUEST_ITEM_VIEW_TYPE:
                return new SubQuestViewHolder(inflater.inflate(R.layout.sub_quest_list_item, parent, false));

            case NOTE_ITEM_VIEW_TYPE:
                return new NoteViewHolder(inflater.inflate(R.layout.note_text_list_item, parent, false));

            case NOTE_LINK_ITEM_VIEW_TYPE:
                return new NoteLinkViewHolder(inflater.inflate(R.layout.note_link_list_item, parent, false));

            case ADD_SUB_QUEST_ITEM_VIEW_TYPE:
                return new AddSubQuestViewHolder(inflater.inflate(R.layout.add_sub_quest_item, parent, false));

            case EMPTY_NOTE_HINT_VIEW_TYPE:
                return new EmptyNoteViewHolder(inflater.inflate(R.layout.note_text_list_item, parent, false));
        }
        throw new IllegalArgumentException("Unknown view type: " + viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == SUB_QUEST_ITEM_VIEW_TYPE) {
            final SubQuest sq = (SubQuest) items.get(holder.getAdapterPosition());
            bindSubQuestViewHolder(sq, (SubQuestViewHolder) holder);
        } else if (holder.getItemViewType() == NOTE_ITEM_VIEW_TYPE) {
            final Note n = (Note) items.get(holder.getAdapterPosition());
            bindNote(n, (NoteViewHolder) holder);
        } else if (holder.getItemViewType() == NOTE_LINK_ITEM_VIEW_TYPE) {
            final Note n = (Note) items.get(holder.getAdapterPosition());
            NoteLinkViewHolder h = (NoteLinkViewHolder) holder;
            h.button.setText(n.getText());
            h.button.setSupportBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.md_white)));
            h.button.setOnClickListener(v -> eventBus.post(new OpenNoteEvent(n)));
        } else if (holder.getItemViewType() == HEADER_ITEM_VIEW_TYPE) {
            TextView header = (TextView) holder.itemView;
            header.setFocusable(true);
            header.setFocusableInTouchMode(true);
            String text = (String) items.get(position);
            header.setText(text);
        } else if (holder.getItemViewType() == ADD_SUB_QUEST_ITEM_VIEW_TYPE) {
            AddSubQuestViewHolder h = (AddSubQuestViewHolder) holder;
            h.addSubQuestView.setSubQuestAddedListener(this);
        } else if (holder.getItemViewType() == EMPTY_NOTE_HINT_VIEW_TYPE) {
            EmptyNoteViewHolder h = (EmptyNoteViewHolder) holder;
            EmptyNoteHint hint = (EmptyNoteHint) items.get(holder.getAdapterPosition());
            h.text.setHint(hint.textHint);
            h.text.setOnClickListener(v -> eventBus.post(new EditNoteRequestEvent()));
        }
    }

    private void bindNote(Note note, NoteViewHolder holder) {
        String text = note.getText();
        holder.text.setText(text);
        holder.text.setOnClickListener(v -> eventBus.post(new EditNoteRequestEvent(note.getText())));
    }

    private void bindSubQuestViewHolder(SubQuest sq, SubQuestViewHolder holder) {

        holder.moreMenu.setOnClickListener(v -> {
            eventBus.post(new ItemActionsShownEvent(EventSource.SUBQUESTS));
            PopupMenu popupMenu = new PopupMenu(context, v);
            popupMenu.inflate(R.menu.sub_quest_actions_menu);
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.delete_sub_quest:
                        deleteSubQuest(sq);
                        return true;
                }
                return false;
            });
            popupMenu.show();
        });

        holder.name.setText(sq.getName());
        holder.check.setOnCheckedChangeListener(null);
        holder.check.setChecked(sq.isCompleted());
        if (sq.isCompleted()) {
            strike(holder);
        } else {
            unstrike(holder);
        }
        holder.check.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                holder.name.setFocusable(false);
                holder.name.setFocusableInTouchMode(false);
                sq.setCompletedAtDate(new Date());
                sq.setCompletedAtMinute(Time.now().toMinuteOfDay());
                eventBus.post(new CompleteSubQuestEvent(sq));
                strike(holder);
            } else {
                holder.name.setFocusable(true);
                holder.name.setFocusableInTouchMode(true);
                sq.setCompletedAtDate(null);
                sq.setCompletedAtMinute(null);
                eventBus.post(new UndoCompleteSubQuestEvent(sq));
                unstrike(holder);
            }
        });

        hideUnderline(holder.name);
        holder.name.setOnFocusChangeListener((view, isFocused) -> {
            if (isFocused) {
                if (sq.isCompleted()) {
                    return;
                }
                showUnderline(holder.name);
                holder.name.requestFocus();
            } else {
                hideUnderline(holder.name);
            }
        });

        holder.name.setOnEditorActionListener((v, actionId, event) -> {
            int result = actionId & EditorInfo.IME_MASK_ACTION;
            if (result == EditorInfo.IME_ACTION_DONE) {
                String name = holder.name.getText().toString();
                if (StringUtils.isEmpty(name)) {
                    deleteSubQuest(sq);
                } else {
                    updateSubQuest(sq, holder);
                }
                return true;
            } else {
                return false;
            }
        });
    }

    private void deleteSubQuest(SubQuest sq) {
        eventBus.post(new DeleteSubQuestEvent(sq, EventSource.QUEST));
    }

    @Override
    public void onSubQuestAdded(String name) {
        SubQuest subQuest = new SubQuest(name);
        eventBus.post(new NewSubQuestEvent(subQuest, EventSource.QUEST));
    }

    private void updateSubQuest(SubQuest sq, SubQuestViewHolder holder) {
        KeyboardUtils.showKeyboard(context);
        sq.setName(holder.name.getText().toString());
        eventBus.post(new UpdateSubQuestNameEvent(sq, EventSource.QUEST));
    }

    private void unstrike(SubQuestViewHolder holder) {
        holder.name.setPaintFlags(holder.name.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        holder.name.setEnabled(true);
    }

    private void strike(SubQuestViewHolder holder) {
        holder.name.setPaintFlags(holder.name.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        holder.name.setEnabled(false);
    }

    private void showUnderline(TextInputEditText editText) {
        editText.getBackground().clearColorFilter();
    }

    private void hideUnderline(TextInputEditText editText) {
        editText.getBackground().setColorFilter(ContextCompat.getColor(context, android.R.color.transparent), PorterDuff.Mode.SRC_IN);
    }

    @Override
    public int getItemViewType(int position) {
        Object item = items.get(position);
        if (item instanceof String) {
            return HEADER_ITEM_VIEW_TYPE;
        }
        if (item instanceof AddSubQuestButton) {
            return ADD_SUB_QUEST_ITEM_VIEW_TYPE;
        }
        if (item instanceof SubQuest) {
            return SUB_QUEST_ITEM_VIEW_TYPE;
        }
        if (item instanceof EmptyNoteHint) {
            return EMPTY_NOTE_HINT_VIEW_TYPE;
        }
        Note note = (Note) item;
        if (note.getNoteTypeValue() == Note.NoteType.TEXT) {
            return NOTE_ITEM_VIEW_TYPE;
        }
        return NOTE_LINK_ITEM_VIEW_TYPE;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {

        HeaderViewHolder(View itemView) {
            super(itemView);
        }
    }

    static class EmptyNoteViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.note_text)
        TextView text;

        EmptyNoteViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.note_text)
        TextView text;

        NoteViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    static class NoteLinkViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.note_text)
        AppCompatButton button;

        NoteLinkViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    static class SubQuestViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.sub_quest_check)
        CheckBox check;

        @BindView(R.id.sub_quest_name)
        TextInputEditText name;

        @BindView(R.id.sub_quest_more_menu)
        ImageButton moreMenu;

        SubQuestViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }

    static class AddSubQuestViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.add_sub_quest_layout)
        AddSubQuestView addSubQuestView;

        AddSubQuestViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }

    private class EmptyNoteHint {
        public final String textHint;

        public EmptyNoteHint(String textHint) {
            this.textHint = textHint;
        }
    }

    private class AddSubQuestButton {
        public final String text;

        public AddSubQuestButton(String text) {
            this.text = text;
        }
    }
}

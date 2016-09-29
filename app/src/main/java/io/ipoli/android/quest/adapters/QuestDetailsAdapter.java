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
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.events.ItemActionsShownEvent;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.note.data.Note;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.SubQuest;
import io.ipoli.android.quest.events.EditNoteRequestEvent;
import io.ipoli.android.quest.events.subquests.CompleteSubQuestEvent;
import io.ipoli.android.quest.events.subquests.DeleteSubQuestEvent;
import io.ipoli.android.quest.events.subquests.UndoCompleteSubQuestEvent;
import io.ipoli.android.quest.events.subquests.UpdateSubQuestNameEvent;

import static io.ipoli.android.quest.adapters.OverviewAdapter.QUEST_ITEM_VIEW_TYPE;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/26/16.
 */

public class QuestDetailsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int HEADER_COUNT = 3;

    public static final int HEADER_ITEM_VIEW_TYPE = 0;
    public static final int SUB_QUEST_ITEM_VIEW_TYPE = 1;
    public static final int ADD_SUB_QUEST_ITEM_VIEW_TYPE = 4;
    public static final int NOTE_ITEM_VIEW_TYPE = 2;
    public static final int NOTE_LINK_ITEM_VIEW_TYPE = 3;

    private final Quest quest;
    private final Context context;
    private final Bus eventBus;

    private List<Object> items;

    public QuestDetailsAdapter(Context context, Quest quest, Bus eventBus) {
        this.context = context;
        this.quest = quest;
        this.eventBus = eventBus;
        items = new ArrayList<>();
        items.add("Sub Quests");
        for (SubQuest sq : quest.getSubQuests()) {
            items.add(sq);
        }
        items.add("Add sub-quest");
        items.add("Notes");
        for (Note note : quest.getNotes()) {
            items.add(note);
        }
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
                return new AddSubQuestViewHolder(inflater.inflate(R.layout.layout_add_sub_quest, parent, false));
        }
        throw new IllegalArgumentException("Unknown view type: " + viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder.getItemViewType() == QUEST_ITEM_VIEW_TYPE) {
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
        } else if (holder.getItemViewType() == HEADER_ITEM_VIEW_TYPE) {
            TextView header = (TextView) holder.itemView;
            String text = (String) items.get(position);
            header.setText(text);
        } else if (holder.getItemViewType() == ADD_SUB_QUEST_ITEM_VIEW_TYPE) {
            AddSubQuestViewHolder h = (AddSubQuestViewHolder) holder;
            h.addSubQuestContainer.setOnClickListener(v -> {
                h.addSubQuest.setTextColor(ContextCompat.getColor(context, R.color.md_dark_text_87));
                h.addSubQuest.setText("");
                showUnderline(h.addSubQuest);
                h.addSubQuest.requestFocus();
            });
        }
    }

    private void bindNote(Note note, NoteViewHolder holder) {
        String text = note.getText();
        if (StringUtils.isEmpty(text)) {
            holder.text.setHint("Tap to add a note");
        }
        holder.text.setText(text);
        holder.text.setOnClickListener(v -> eventBus.post(new EditNoteRequestEvent(note)));
    }

    private void bindSubQuestViewHolder(SubQuest sq, SubQuestViewHolder holder) {

        holder.moreMenu.setOnClickListener(v -> {
            eventBus.post(new ItemActionsShownEvent(EventSource.SUBQUESTS));
            PopupMenu popupMenu = new PopupMenu(context, v);
            popupMenu.inflate(R.menu.sub_quest_actions_menu);
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.delete_sub_quest:
                        removeSubquest(holder.getAdapterPosition());
                        eventBus.post(new DeleteSubQuestEvent(sq, EventSource.SUBQUESTS));
                        return true;
                }
                return false;
            });
            popupMenu.show();
        });

        holder.name.setText(sq.getName());
        holder.check.setOnCheckedChangeListener(null);
        holder.check.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                sq.setCompletedAtDate(new Date());
                sq.setCompletedAtMinute(Time.now().toMinutesAfterMidnight());
                holder.name.setPaintFlags(holder.name.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                holder.name.setEnabled(false);
                eventBus.post(new CompleteSubQuestEvent(sq));
            } else {
                sq.setCompletedAtDate(null);
                sq.setCompletedAtMinute(null);
                holder.name.setPaintFlags(holder.name.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                holder.name.setEnabled(true);
                eventBus.post(new UndoCompleteSubQuestEvent(sq));
            }
        });
        holder.check.setChecked(sq.isCompleted());

        hideUnderline(holder.name);
        holder.name.setOnFocusChangeListener((view, isFocused) -> {
            if (isFocused) {
                if (sq.isCompleted()) {
                    holder.name.clearFocus();
                    return;
                }
                showUnderline(holder.name);
                holder.name.requestFocus();
            } else {
                hideUnderline(holder.name);
                sq.setName(holder.name.getText().toString());
                eventBus.post(new UpdateSubQuestNameEvent(sq, EventSource.SUBQUESTS));
            }
        });
    }

    private void removeSubquest(int position) {
//        subQuests.remove(position);
        notifyItemRemoved(position);
    }

    public void updateNotes(List<Note> notes) {
        Iterator i = items.iterator();
        while (i.hasNext()) {
            if (i.next() instanceof Note) {
                i.remove();
            }
        }
        items.addAll(notes);
        notifyDataSetChanged();
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
            if (item.equals("Add sub-quest")) {
                return ADD_SUB_QUEST_ITEM_VIEW_TYPE;
            }
            return HEADER_ITEM_VIEW_TYPE;
        }
        if (item instanceof SubQuest) {
            return SUB_QUEST_ITEM_VIEW_TYPE;
        }
        Note note = (Note) item;
        if (note.getNoteType() == Note.Type.TEXT) {
            return NOTE_ITEM_VIEW_TYPE;
        }
        return NOTE_LINK_ITEM_VIEW_TYPE;
    }

    @Override
    public int getItemCount() {
        return quest.getSubQuests().size() + quest.getNotes().size() + HEADER_COUNT;
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {

        HeaderViewHolder(View itemView) {
            super(itemView);
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
        @BindView(R.id.add_sub_quest_container)
        ViewGroup addSubQuestContainer;

        @BindView(R.id.add_sub_quest)
        TextInputEditText addSubQuest;

        AddSubQuestViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }
}

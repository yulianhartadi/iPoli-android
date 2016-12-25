package io.ipoli.android.quest.data;

import io.ipoli.android.app.persistence.PersistedObject;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 12/22/16.
 */
public class InboxQuest extends PersistedObject {

    private String questId;
    private String name;
    private String category;
    private Long questCreatedAt;

    public InboxQuest() {

    }

    public InboxQuest(Quest quest) {
        setQuestId(quest.getId());
        setName(quest.getName());
        setCategory(quest.getCategory());
        setQuestCreatedAt(quest.getCreatedAt());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Long getQuestCreatedAt() {
        return questCreatedAt;
    }

    public void setQuestCreatedAt(Long questCreatedAt) {
        this.questCreatedAt = questCreatedAt;
    }

    public static Category getCategory(InboxQuest quest) {
        return Category.valueOf(quest.getCategory());
    }

    public String getQuestId() {
        return questId;
    }

    public void setQuestId(String questId) {
        this.questId = questId;
    }

    @Override
    public void setId(String id) {

    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public void setCreatedAt(Long createdAt) {

    }

    @Override
    public void setUpdatedAt(Long updatedAt) {

    }

    @Override
    public Long getCreatedAt() {
        return null;
    }

    @Override
    public Long getUpdatedAt() {
        return null;
    }
}

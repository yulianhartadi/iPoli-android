package io.ipoli.android.quest.data;

import io.ipoli.android.app.persistence.PersistedObject;
import io.ipoli.android.app.utils.DateUtils;

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
        setCreatedAt(DateUtils.nowUTC().getTime());
        setUpdatedAt(DateUtils.nowUTC().getTime());
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public Long getCreatedAt() {
        return createdAt;
    }

    @Override
    public Long getUpdatedAt() {
        return updatedAt;
    }

    public String getQuestId() {
        return questId;
    }

    public void setQuestId(String questId) {
        this.questId = questId;
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
}

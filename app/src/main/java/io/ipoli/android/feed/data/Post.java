package io.ipoli.android.feed.data;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.player.Avatar;
import io.ipoli.android.player.Player;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.Quest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/16/17.
 */
public class Post {

    private String id;
    private Long createdAt;
    private Long updatedAt;
    private String title;
    private String message;
    private String category;
    private Long completedAt;
    private Integer coins;
    private Integer experience;
    private Integer rewardPoints;
    private String questId;
    private String playerId;
    private Integer playerLevel;
    private String playerUsername;
    private Integer playerAvatarCode;
    private Map<String, Boolean> likes;
    private Map<String, Boolean> addedBy;

    public Post() {

    }

    public Post(String title, String message, Player player, Quest quest) {
        setCreatedAt(DateUtils.nowUTC().getTime());
        setTitle(title);
        setMessage(message);
        setCategory(quest.getCategory());
        setQuestId(quest.getId());
        setCompletedAt(quest.getCompletedAt());
        setCoins(quest.getCoins().intValue());
        setExperience(quest.getExperience().intValue());
        setRewardPoints(quest.getRewardPoints().intValue());
        setPlayerId(player.getId());
        setPlayerUsername(player.getUsername());
        setPlayerLevel(player.getLevel());
        setPlayerAvatarCode(player.getAvatarCode());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getCoins() {
        return coins;
    }

    public void setCoins(Integer coins) {
        this.coins = coins;
    }

    public Integer getExperience() {
        return experience;
    }

    public void setExperience(Integer experience) {
        this.experience = experience;
    }

    public Integer getRewardPoints() {
        return rewardPoints;
    }

    public void setRewardPoints(Integer rewardPoints) {
        this.rewardPoints = rewardPoints;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getPlayerUsername() {
        return playerUsername;
    }

    public void setPlayerUsername(String playerUsername) {
        this.playerUsername = playerUsername;
    }

    public Integer getPlayerLevel() {
        return playerLevel;
    }

    public void setPlayerLevel(Integer playerLevel) {
        this.playerLevel = playerLevel;
    }

    public Integer getPlayerAvatarCode() {
        return playerAvatarCode;
    }

    public void setPlayerAvatarCode(Integer playerAvatarCode) {
        this.playerAvatarCode = playerAvatarCode;
    }

    public Map<String, Boolean> getLikes() {
        if (likes == null) {
            likes = new HashMap<>();
        }
        return likes;
    }

    public void setLikes(Map<String, Boolean> likes) {
        this.likes = likes;
    }

    public Map<String, Boolean> getAddedBy() {
        if (addedBy == null) {
            addedBy = new HashMap<>();
        }
        return addedBy;
    }

    public void setAddedBy(Map<String, Boolean> addedBy) {
        this.addedBy = addedBy;
    }

    public Long getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Long completedAt) {
        this.completedAt = completedAt;
    }

    public String getQuestId() {
        return questId;
    }

    public void setQuestId(String questId) {
        this.questId = questId;
    }

    @Exclude
    public Category getCategoryType() {
        return Category.valueOf(category);
    }

    @Exclude
    public void addLike(String playerId) {
        getLikes().put(playerId, true);
    }

    @Exclude
    public boolean isGivenKudosByPlayer(String playerId) {
        return getLikes().containsKey(playerId);
    }

    @Exclude
    public void removeLike(String playerId) {
        getLikes().remove(playerId);
    }

    @Exclude
    public boolean isAddedByPlayer(String playerId) {
        return getAddedBy().containsKey(playerId);
    }

    @Exclude
    public void addAddedBy(String playerId) {
        getAddedBy().put(playerId, true);
    }

    @Exclude
    public Avatar getPlayerAvatar() {
        return Avatar.get(playerAvatarCode);
    }
}
package io.ipoli.android.feed.data;

import java.util.Map;

import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.player.Player;
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
    private String playerId;
    private String username;
    private Integer level;
    private String avatar;
    private Map<String, Boolean> likes;

    public Post() {

    }

    public Post(String title, String message, Player player, Quest quest) {
        setCreatedAt(DateUtils.nowUTC().getTime());
        setTitle(title);
        setMessage(message);
        setCategory(quest.getCategory());
        setCompletedAt(quest.getCompletedAt());
        setCoins(quest.getCoins().intValue());
        setExperience(quest.getExperience().intValue());
        setRewardPoints(quest.getRewardPoints().intValue());
        setPlayerId(player.getId());
        setUsername(player.getUsername());
        setLevel(player.getLevel());
        setAvatar(String.valueOf(player.getAvatarCode()));
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Map<String, Boolean> getLikes() {
        return likes;
    }

    public void setLikes(Map<String, Boolean> likes) {
        this.likes = likes;
    }

    public Long getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Long completedAt) {
        this.completedAt = completedAt;
    }
}
package io.ipoli.android.feed.data;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

import io.ipoli.android.player.Avatar;
import io.ipoli.android.player.PetAvatar;
import io.ipoli.android.player.Player;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 6/26/17.
 */

public class PlayerProfile {
    private String id;
    private String displayName;
    private String username;
    private String description;
    private Integer level;
    private String experience;
    private String title;
    private Integer avatarCode;
    private String petName;
    private Integer petAvatarCode;
    private String petState;
    private Long createdAt;
    private Map<String, Boolean> postedQuests;
    private Map<String, Boolean> followers;
    private Map<String, Boolean> following;

    public PlayerProfile() {
    }

    public PlayerProfile(Player player, String title) {
        setId(player.getId());
        setDisplayName(player.getDisplayName());
        setUsername(player.getUsername());
        setDescription(player.getDescription());
        setTitle(title);
        setLevel(player.getLevel());
        setExperience(player.getExperience());
        setAvatarCode(player.getAvatarCode());
        setPetName(player.getPet().getName());
        setPetAvatarCode(player.getPet().getAvatarCode());
        setPetState(player.getPet().getState().name());
        setCreatedAt(player.getCreatedAt());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
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

    public Integer getAvatarCode() {
        return avatarCode;
    }

    public void setAvatarCode(Integer avatarCode) {
        this.avatarCode = avatarCode;
    }

    public String getPetName() {
        return petName;
    }

    public void setPetName(String petName) {
        this.petName = petName;
    }

    public Integer getPetAvatarCode() {
        return petAvatarCode;
    }

    public void setPetAvatarCode(Integer petAvatarCode) {
        this.petAvatarCode = petAvatarCode;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Map<String, Boolean> getPostedQuests() {
        if (postedQuests == null) {
            postedQuests = new HashMap<>();
        }
        return postedQuests;
    }

    public void setPostedQuests(Map<String, Boolean> postedQuests) {
        this.postedQuests = postedQuests;
    }

    public Map<String, Boolean> getFollowers() {
        if (followers == null) {
            followers = new HashMap<>();
        }
        return followers;
    }

    public void setFollowers(Map<String, Boolean> followers) {
        this.followers = followers;
    }

    public Map<String, Boolean> getFollowing() {
        if (following == null) {
            following = new HashMap<>();
        }
        return following;
    }

    public void setFollowing(Map<String, Boolean> following) {
        this.following = following;
    }

    @Exclude
    public Avatar getPlayerAvatar() {
        return Avatar.get(avatarCode);
    }

    @Exclude
    public PetAvatar getPetAvatar() {
        return PetAvatar.get(petAvatarCode);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getPetState() {
        return petState;
    }

    public void setPetState(String petState) {
        this.petState = petState;
    }
}

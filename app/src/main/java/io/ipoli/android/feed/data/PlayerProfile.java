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
    private Integer level;
    private Integer avatarCode;
    private String petName;
    private Integer petAvatarCode;
    private Long createdAt;
    private Map<String, Boolean> postedQuests;
    private Map<String, Follower> followers;
    private Map<String, Follower> followings;

    public PlayerProfile() {
    }

    public PlayerProfile(Player player) {
        setId(player.getId());
        setDisplayName(player.getDisplayName());
        setUsername(player.getUsername());
        setLevel(player.getLevel());
        setAvatarCode(player.getAvatarCode());
        setPetName(player.getPet().getName());
        setPetAvatarCode(player.getPet().getAvatarCode());
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

    public Map<String, Follower> getFollowers() {
        if (followers == null) {
            followers = new HashMap<>();
        }
        return followers;
    }

    public void setFollowers(Map<String, Follower> followers) {
        this.followers = followers;
    }

    public Map<String, Follower> getFollowings() {
        if (followings == null) {
            followings = new HashMap<>();
        }
        return followings;
    }

    public void setFollowings(Map<String, Follower> followings) {
        this.followings = followings;
    }

    @Exclude
    public Avatar getPlayerAvatar() {
        return Avatar.get(avatarCode);
    }

    @Exclude
    public PetAvatar getPetAvatar() {
        return PetAvatar.get(petAvatarCode);
    }
}

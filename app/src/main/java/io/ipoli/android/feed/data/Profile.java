package io.ipoli.android.feed.data;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

import io.ipoli.android.player.data.Avatar;
import io.ipoli.android.player.data.PetAvatar;
import io.ipoli.android.player.data.Player;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 6/26/17.
 */
public class Profile {
    private String id;
    private String displayName;
    private String username;
    private String bio;
    private Integer level;
    private String experience;
    private Integer avatarCode;
    private String petName;
    private Integer petAvatarCode;
    private String petState;
    private Long createdAt;
    // postId -> questId
    private Map<String, String> posts;
    private Map<String, Boolean> followers;
    private Map<String, Boolean> following;
    private Map<Integer, Long> achievements;

    public Profile() {
    }

    public Profile(Player player) {
        setId(player.getId());
        setDisplayName(player.getFullDisplayName());
        setUsername(player.getUsername());
        setBio(player.getBio());
        setLevel(player.getLevel());
        setExperience(player.getExperience());
        setAvatarCode(player.getAvatarCode());
        setPetName(player.getPet().getName());
        setPetAvatarCode(player.getPet().getAvatarCode());
        setPetState(player.getPet().getState().name());
        setCreatedAt(player.getCreatedAt());
        setAchievements(player.getAchievements());
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

    public Map<String, String> getPosts() {
        if (posts == null) {
            posts = new HashMap<>();
        }
        return posts;
    }

    public void setPosts(Map<String, String> posts) {
        this.posts = posts;
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

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
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

    public Map<Integer, Long> getAchievements() {
        if (achievements == null) {
            achievements = new HashMap<>();
        }
        return achievements;
    }

    public void setAchievements(Map<Integer, Long> achievements) {
        this.achievements = achievements;
    }

    @Exclude
    public boolean isFollowedBy(String playerId) {
        return getFollowers().containsKey(playerId);
    }
}

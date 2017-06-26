package io.ipoli.android.feed.data;

import java.util.HashMap;
import java.util.Map;

import io.ipoli.android.player.Player;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 6/26/17.
 */

public class PlayerProfile {
    private String id;
    private String username;
    private Integer level;
    private String avatar;
    private String petName;
    private Integer petAvatar;
    private Long createdAt;
    private Map<String, Post> posts;
    private Map<String, Follower> followers;
    private Map<String, Follower> followings;
    private Map<String, Boolean> likes;

    public PlayerProfile() {
    }

    public PlayerProfile(Player player) {
        setId(player.getId());
        setUsername(player.getUsername());
        setLevel(player.getLevel());
        setAvatar(String.valueOf(player.getAvatarCode()));
        setPetName(player.getPet().getName());
        setPetAvatar(player.getPet().getAvatarCode());
        setCreatedAt(player.getCreatedAt());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getPetName() {
        return petName;
    }

    public void setPetName(String petName) {
        this.petName = petName;
    }

    public Integer getPetAvatar() {
        return petAvatar;
    }

    public void setPetAvatar(Integer petAvatar) {
        this.petAvatar = petAvatar;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Map<String, Post> getPosts() {
        if (posts == null) {
            posts = new HashMap<>();
        }
        return posts;
    }

    public void setPosts(Map<String, Post> posts) {
        this.posts = posts;
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

    public Map<String, Boolean> getLikes() {
        if (likes == null) {
            likes = new HashMap<>();
        }
        return likes;
    }

    public void setLikes(Map<String, Boolean> likes) {
        this.likes = likes;
    }
}

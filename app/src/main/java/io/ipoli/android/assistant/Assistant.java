package io.ipoli.android.assistant;

import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/9/16.
 */
public class Assistant extends RealmObject {

    @PrimaryKey
    private String id;

    @Required
    private String name;

    private String avatar;

    private String state;

    public enum State {
        TUTORIAL_START, TUTORIAL_RENAME, TUTORIAL_CHANGE_ASSISTANT_AVATAR, TUTORIAL_CHANGE_PLAYER_AVATAR,
        TUTORIAL_ADD_QUEST, TUTORIAL_PLAN_TODAY, TUTORIAL_SHOW_QUESTS, TUTORIAL_ADD_TODAY_QUEST,
        TUTORIAL_REVIEW_TODAY, TUTORIAL_SHOW_EXAMPLES, TUTORIAL_HELP, NORMAL, FEEDBACK;
    }

    @Ignore
    private State stateType;

    public State getStateType() {
        return State.valueOf(state);
    }

    public Assistant() {

    }

    public Assistant(String name, String avatar, State state) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.avatar = avatar;
        this.state = state.name();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}

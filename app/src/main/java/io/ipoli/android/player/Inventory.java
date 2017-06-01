package io.ipoli.android.player;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.threeten.bp.LocalDate;

import java.util.HashMap;
import java.util.Map;

import io.ipoli.android.store.Upgrade;

import static io.ipoli.android.app.utils.DateUtils.toMillis;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/25/17.
 */

public class Inventory {
    private Map<Integer, Long> upgrades;
    private Map<Integer, Long> pets;
    private Map<Integer, Long> avatars;

    public Inventory() {
        // intentional
    }

    @JsonIgnore
    public void addUpgrade(Upgrade upgrade, LocalDate localDate) {
        getUpgrades().put(upgrade.code, toMillis(localDate));
    }

    @JsonIgnore
    public void addPet(PetAvatar petAvatar, LocalDate localDate) {
        getPets().put(petAvatar.code, toMillis(localDate));
    }

    @JsonIgnore
    public void addAvatar(Avatar avatar, LocalDate localDate) {
        getAvatars().put(avatar.code, toMillis(localDate));
    }

    public Map<Integer, Long> getUpgrades() {
        if(upgrades == null) {
            upgrades = new HashMap<>();
        }
        return upgrades;
    }

    public void setUpgrades(Map<Integer, Long> upgrades) {
        this.upgrades = upgrades;
    }

    public Map<Integer, Long> getPets() {
        if(pets == null) {
            pets = new HashMap<>();
        }
        return pets;
    }

    public void setPets(Map<Integer, Long> pets) {
        this.pets = pets;
    }

    public Map<Integer, Long> getAvatars() {
        if(avatars == null) {
            avatars = new HashMap<>();
        }
        return avatars;
    }

    public void setAvatars(Map<Integer, Long> avatars) {
        this.avatars = avatars;
    }
}

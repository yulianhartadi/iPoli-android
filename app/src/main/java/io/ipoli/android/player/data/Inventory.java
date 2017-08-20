package io.ipoli.android.player.data;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.threeten.bp.LocalDate;

import java.util.HashMap;
import java.util.Map;

import io.ipoli.android.store.PowerUp;

import static io.ipoli.android.app.utils.DateUtils.toMillis;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/25/17.
 */

public class Inventory {
    private Map<Integer, Long> powerUps;
    private Map<Integer, Long> pets;
    private Map<Integer, Long> avatars;

    public Inventory() {
        // intentional
    }

    @JsonIgnore
    public void addPowerUp(PowerUp powerUp, LocalDate expirationDate) {
        getPowerUps().put(powerUp.code, toMillis(expirationDate));
    }

    @JsonIgnore
    public void addPet(PetAvatar petAvatar, LocalDate localDate) {
        getPets().put(petAvatar.code, toMillis(localDate));
    }

    @JsonIgnore
    public void addAvatar(Avatar avatar, LocalDate localDate) {
        getAvatars().put(avatar.code, toMillis(localDate));
    }

    public Map<Integer, Long> getPowerUps() {
        if (powerUps == null) {
            powerUps = new HashMap<>();
        }
        return powerUps;
    }

    public void setPowerUps(Map<Integer, Long> powerUps) {
        this.powerUps = powerUps;
    }

    public Map<Integer, Long> getPets() {
        if (pets == null) {
            pets = new HashMap<>();
        }
        return pets;
    }

    public void setPets(Map<Integer, Long> pets) {
        this.pets = pets;
    }

    public Map<Integer, Long> getAvatars() {
        if (avatars == null) {
            avatars = new HashMap<>();
        }
        return avatars;
    }

    public void setAvatars(Map<Integer, Long> avatars) {
        this.avatars = avatars;
    }

    @JsonIgnore
    public void enableAllPowerUps(LocalDate expirationDate) {
        for (PowerUp powerUp : PowerUp.values()) {
            addPowerUp(powerUp, expirationDate);
        }
    }
}

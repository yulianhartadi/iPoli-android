package io.ipoli.android.player.data

import io.ipoli.android.common.datetime.DateUtils
import io.ipoli.android.store.avatars.data.Avatar
import io.realm.RealmList
import io.realm.RealmObject
import org.threeten.bp.LocalDate


/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 8/22/17.
 */
open class Inventory() : RealmObject() {

    var powerUps: RealmList<InventoryItem> = RealmList()
    var pets: RealmList<InventoryItem> = RealmList()
    var avatars: RealmList<InventoryItem> = RealmList()

    //    fun addPowerUp(powerUp: PowerUp, expirationDate: LocalDate) {
//        getPowerUps().put(powerUp.code, toMillis(expirationDate))
//    }
//
//    fun addPet(petAvatar: PetAvatar, localDate: LocalDate) {
//        getPets().put(petAvatar.code, toMillis(localDate))
//    }

    fun addAvatar(avatar: Avatar, localDate: LocalDate) {
        avatars.add(InventoryItem(avatar.code, DateUtils.toMillis(localDate)))
    }

    fun addAvatar(code: Int, localDate: LocalDate) {
        avatars.add(InventoryItem(code, DateUtils.toMillis(localDate)))
    }

    fun hasAvatar(code: Int): Boolean {
        return avatars.find { inventoryItem -> inventoryItem.code == code } != null
    }
//    fun enableAllPowerUps(expirationDate: LocalDate) {
//        for (powerUp in PowerUp.values()) {
//            addPowerUp(powerUp, expirationDate)
//        }
//    }
}

open class InventoryItem(var code: Int = -1, var date: Long = -1) : RealmObject() {
}
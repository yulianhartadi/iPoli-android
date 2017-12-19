package mypoli.android.player.persistence.model

import mypoli.android.common.persistence.CouchbasePersistedModel

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 12/1/17.
 */

data class CouchbasePlayer(override val map: MutableMap<String, Any?> = mutableMapOf()) : CouchbasePersistedModel {
    override var type: String by map
    override var id: String by map
    var level: Int by map
    var coins: Int by map
    var experience: Long by map
    var authProvider: MutableMap<String, Any?> by map
    var avatarCode: Int by map
    var currentTheme: String by map
    var pet: MutableMap<String, Any?> by map
    var inventory: MutableMap<String, Any?> by map
    override var createdAt: Long by map
    override var updatedAt: Long by map
    override var removedAt: Long? by map

    companion object {
        const val TYPE = "Player"
    }
}

data class CouchbaseAuthProvider(val map: MutableMap<String, Any?> = mutableMapOf()) {
    var id: String by map
    var provider: String by map
    var firstName: String by map
    var lastName: String by map
    var username: String by map
    var email: String by map
    var image: String by map
}

data class CouchbasePet(val map: MutableMap<String, Any?> = mutableMapOf()) {
    var name: String by map
    var avatar: String by map
    var moodPoints: Int by map
    var healthPoints: Int by map
    var experienceBonus: Float by map
    var coinBonus: Float by map
    var itemDropChanceBonus: Float by map
}

data class CouchbaseInventory(val map: MutableMap<String, Any?> = mutableMapOf()) {
    var food: MutableMap<String, Long> by map
    var pets: List<MutableMap<String, Any?>> by map
    var themes: List<String> by map
    var colorPacks: List<String> by map
    var iconPacks: List<String> by map
}

data class CouchbaseInventoryPet(val map: MutableMap<String, Any?> = mutableMapOf()) {
    var name: String by map
    var avatar: String by map
}

enum class ProviderType {
    FACEBOOK, GOOGLE, ANONYMOUS
}
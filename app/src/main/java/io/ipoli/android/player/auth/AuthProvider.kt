package io.ipoli.android.player.auth

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 8/14/17.
 */
open class AuthProvider(val map: MutableMap<String, Any?> = mutableMapOf()) {
    var id: String by map
    var provider: String by map
    var firstName: String by map
    var lastName: String by map
    var username: String by map
    var email: String by map
    var image: String by map
}


enum class ProviderType {
    FACEBOOK, GOOGLE, ANONYMOUS
}
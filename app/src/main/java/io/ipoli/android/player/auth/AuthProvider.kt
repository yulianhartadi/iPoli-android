package io.ipoli.android.player.auth

import io.realm.RealmObject

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/14/17.
 */
open class AuthProvider(
    var id: String = "",
    var provider: String = "",
    var firstName: String = "",
    var lastName: String = "",
    var username: String = "",
    var email: String = "",
    var image: String = ""
) : RealmObject()

enum class ProviderType {
    FACEBOOK, GOOGLE, ANONYMOUS
}
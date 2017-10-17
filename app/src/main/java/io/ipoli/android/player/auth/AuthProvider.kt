package io.ipoli.android.player.auth

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
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
)

enum class ProviderType {
    FACEBOOK, GOOGLE, ANONYMOUS
}
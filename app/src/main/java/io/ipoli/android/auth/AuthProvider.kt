package io.ipoli.android.auth

/**
 * Created by vini on 8/14/17.
 */
data class AuthProvider(
        val id: String,
        val provider: String,
        val firstName: String,
        val lastName: String,
        val username: String,
        val email: String,
        val image: String
)

enum class ProviderType {
    FACEBOOK, GOOGLE
}
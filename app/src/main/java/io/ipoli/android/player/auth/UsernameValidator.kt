package io.ipoli.android.player.auth

import io.ipoli.android.Constants
import io.ipoli.android.player.persistence.PlayerRepository
import java.nio.charset.Charset
import java.util.regex.Pattern

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 5/7/18.
 */
class UsernameValidator(private val playerRepository: PlayerRepository) {

    public enum class ValidationError {
        EMPTY_USERNAME,
        EXISTING_USERNAME,
        INVALID_FORMAT,
        INVALID_LENGTH
    }

    public fun validate(
        username: String
    ): ValidationError? {

        if (username.isBlank()) {
            return ValidationError.EMPTY_USERNAME
        }

        val asciiEncoder = Charset.forName("US-ASCII").newEncoder()
        if (!asciiEncoder.canEncode(username)) {
            return ValidationError.INVALID_FORMAT
        }

        val p = Pattern.compile("^\\w+$")
        if (!p.matcher(username).matches()) {
            return ValidationError.INVALID_FORMAT
        }

        if (username.length < Constants.USERNAME_MIN_LENGTH || username.length > Constants.USERNAME_MAX_LENGTH) {
            return ValidationError.INVALID_LENGTH
        }

        if (!playerRepository.isUsernameAvailable(username)) {
            return ValidationError.EXISTING_USERNAME
        }

        return null
    }
}
package mypoli.android.player.auth.error

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 2/12/18.
 */
class SignInError(message: String, cause: Exception? = null) : Exception(message, cause)
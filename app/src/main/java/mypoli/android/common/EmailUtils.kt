package mypoli.android.common

import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_SENDTO
import android.net.Uri.fromParts
import mypoli.android.BuildConfig
import mypoli.android.Constants


/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 2/13/18.
 */
class EmailUtils {

    companion object {

        fun send(context: Context, subject: String, chooserTitle: String) {
            send(context, subject, "", chooserTitle)
        }

        fun send(context: Context, subject: String, playerId: String, chooserTitle: String) {
            val emailIntent = Intent(
                ACTION_SENDTO, fromParts(
                    "mailto", Constants.MYPOLI_EMAIL, null
                )
            )
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
            if (playerId.isNotEmpty()) {
                val body =
                    "\n\nPlease, do not delete below this line\n=====================\nThis will help us fix the issue faster:\nPlayer id " + playerId +
                        "\nVersion " + BuildConfig.VERSION_NAME
                emailIntent.putExtra(Intent.EXTRA_TEXT, body)
            }
            context.startActivity(Intent.createChooser(emailIntent, chooserTitle))
        }
    }
}
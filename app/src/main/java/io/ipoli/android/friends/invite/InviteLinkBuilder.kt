package io.ipoli.android.friends.invite

import android.net.Uri
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import io.ipoli.android.BuildConfig
import io.ipoli.android.MyPoliApp
import java.net.URI

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 07/11/2018.
 */
interface InviteLinkBuilder {
    fun create(): URI
}

class FirebaseInviteLinkBuilder : InviteLinkBuilder {

    companion object {
        private const val DEBUG_DYNAMIC_LINK_DOMAIN = "https://mypolidev.page.link"
        private const val PROD_DYNAMIC_LINK_DOMAIN = "https://mypoli.page.link"
    }

    override fun create(): URI {
        val playerId = FirebaseAuth.getInstance().currentUser!!.uid

        val dynamicLinkDomain =
            if (BuildConfig.DEBUG)
                DEBUG_DYNAMIC_LINK_DOMAIN
            else
                PROD_DYNAMIC_LINK_DOMAIN

        val packageName = MyPoliApp.instance.applicationContext.packageName
        val linkTask = FirebaseDynamicLinks.getInstance()
            .createDynamicLink()
            .setLongLink(Uri.parse("$dynamicLinkDomain/?link=https://www.mypoli.fun/invite?playerId=$playerId&apn=$packageName"))
            .buildShortDynamicLink()

        val shortLink = Tasks.await(linkTask)
        return URI.create(shortLink.shortLink.toString())
    }

}
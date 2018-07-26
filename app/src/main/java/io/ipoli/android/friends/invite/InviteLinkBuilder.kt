package io.ipoli.android.friends.invite

import android.net.Uri
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import io.ipoli.android.BuildConfig
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
        private const val DEBUG_DYNAMIC_LINK_DOMAIN = "mypolidev.page.link"
        private const val PROD_DYNAMIC_LINK_DOMAIN = "mypoli.page.link"
    }

    override fun create(): URI {
        val playerId = FirebaseAuth.getInstance().currentUser!!.uid

        val dynamicLinkDomain =
            if (BuildConfig.DEBUG)
                DEBUG_DYNAMIC_LINK_DOMAIN
            else
                PROD_DYNAMIC_LINK_DOMAIN

        val linkTask = FirebaseDynamicLinks.getInstance()
            .createDynamicLink()
            .setLink(Uri.parse("https://www.mypoli.fun/invite?playerId=$playerId"))
            .setDynamicLinkDomain(dynamicLinkDomain)
            .buildShortDynamicLink()

        val shortLink = Tasks.await(linkTask)
        return URI.create(shortLink.shortLink.toString())
    }

}
package mypoli.android.common.api

import mypoli.android.ApiConstants
import java.net.URL

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 3/23/18.
 */
interface UrlProvider {
    fun getMembershipStatus(): URL
}

class ProdUrlProvider : UrlProvider {
    override fun getMembershipStatus() = URL("${ApiConstants.PROD_API_URL}subscriptions/")
}

class DevUrlProvider : UrlProvider {
    override fun getMembershipStatus() = URL("${ApiConstants.DEV_API_URL}subscriptions/")

}
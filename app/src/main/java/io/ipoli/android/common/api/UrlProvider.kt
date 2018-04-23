package io.ipoli.android.common.api

import io.ipoli.android.ApiConstants
import java.net.URL

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 3/23/18.
 */
interface UrlProvider {
    val getMembershipStatus: URL
    val migratePlayer: URL
}

class ProdUrlProvider : UrlProvider {
    override val getMembershipStatus = URL("${ApiConstants.PROD_API_URL}subscriptions/")
    override val migratePlayer = URL("${ApiConstants.PROD_API_URL}migrations/")
}

class DevUrlProvider : UrlProvider {
    override val getMembershipStatus = URL("${ApiConstants.DEV_API_URL}subscriptions/")
    override val migratePlayer = URL("${ApiConstants.DEV_API_URL}migrations/")
}
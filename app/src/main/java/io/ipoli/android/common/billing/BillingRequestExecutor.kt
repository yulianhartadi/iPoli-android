package io.ipoli.android.common.billing

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 07/03/2018.
 */
class BillingRequestExecutor(
    private val billingResponseHandler: BillingResponseHandler
) {
    fun execute(
        billingClient: BillingClient,
        request: (BillingClient) -> Unit,
        failureListener: BillingResponseHandler.FailureListener? = null
    ) {
        if (billingClient.isReady) {
            request(billingClient)
        } else {
            billingClient.startConnection(object : BillingClientStateListener {

                override fun onBillingServiceDisconnected() {
                    failureListener?.onDisconnected()
                }

                override fun onBillingSetupFinished(responseCode: Int) {
                    billingResponseHandler.handle(
                        responseCode = responseCode,
                        onSuccess = {
                            request(billingClient)
                        },
                        failureListener = failureListener
                    )
                }

            })
        }
    }
}
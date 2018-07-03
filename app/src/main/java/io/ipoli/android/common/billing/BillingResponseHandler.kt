package io.ipoli.android.common.billing

import com.android.billingclient.api.BillingClient
import com.crashlytics.android.Crashlytics
import io.ipoli.android.BuildConfig
import io.ipoli.android.common.analytics.EventLogger
import timber.log.Timber

class BillingError(message: String, cause: Throwable? = null) :
    Exception(message, cause)

class BillingResponseHandler(
    private val eventLogger: EventLogger
) {

    interface FailureListener {

        fun onCanceledByUser()

        fun onDisconnected()

        fun onUnavailable(@BillingClient.BillingResponse responseCode: Int)

        fun onError(@BillingClient.BillingResponse responseCode: Int)
    }

    fun handle(
        @BillingClient.BillingResponse responseCode: Int, onSuccess: () -> Unit,
        failureListener: FailureListener? = null
    ) {

        when (responseCode) {
            BillingClient.BillingResponse.OK -> onSuccess()
            BillingClient.BillingResponse.USER_CANCELED -> {
                eventLogger.logEvent("billing_request_canceled_by_user")
                failureListener?.onCanceledByUser()
            }
            BillingClient.BillingResponse.SERVICE_UNAVAILABLE, BillingClient.BillingResponse.SERVICE_DISCONNECTED -> {
                failureListener?.onDisconnected()
            }
            BillingClient.BillingResponse.BILLING_UNAVAILABLE, BillingClient.BillingResponse.FEATURE_NOT_SUPPORTED -> {
                eventLogger.logEvent(
                    "billing_unavailable",
                    mapOf("responseCode" to responseCode)
                )
                failureListener?.onUnavailable(responseCode)
            }
            BillingClient.BillingResponse.ITEM_UNAVAILABLE, BillingClient.BillingResponse.ITEM_NOT_OWNED, BillingClient.BillingResponse.ITEM_ALREADY_OWNED, BillingClient.BillingResponse.DEVELOPER_ERROR, BillingClient.BillingResponse.ERROR -> {

                val error = BillingError("Billing request failed with response: $responseCode")

                if (BuildConfig.DEBUG) {
                    Timber.e(error)
                } else {
                    Crashlytics.logException(error)
                }
                failureListener?.onError(responseCode)
            }
        }
    }

    fun handle(@BillingClient.BillingResponse responseCode: Int, listener: FailureListener? = null) {
        handle(responseCode, {}, listener)
    }
}
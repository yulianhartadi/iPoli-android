package mypoli.android.store.purchase

import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import mypoli.android.common.api.Api
import mypoli.android.common.datetime.DateUtils
import mypoli.android.store.membership.MembershipPlan
import org.solovyev.android.checkout.*
import org.solovyev.android.checkout.Purchase
import org.threeten.bp.LocalDate
import java.util.*


/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 3/16/18.
 */
interface SubscriptionManager {

    fun loadInventory(productsListener: (Inventory.Product, Set<String>) -> Unit)

    fun subscribe(
        sku: String,
        successListener: (LocalDate) -> Unit,
        errorListener: (Int, Exception) -> Unit
    )

    fun changeSubscription(
        sku: String, activeSkus: Set<String>,
        successListener: (LocalDate, LocalDate) -> Unit,
        errorListener: (String, Exception?) -> Unit
    )
}

class AndroidSubscriptionManager(private val checkout: UiCheckout) : SubscriptionManager {

    companion object {

//        val SKUS =   MembershipPlan.values().map { it.sku }

        val SKUS = listOf(
            MembershipPlan.MONTHLY.sku,
            MembershipPlan.YEARLY.sku
        )
    }

    override fun loadInventory(productsListener: (Inventory.Product, Set<String>) -> Unit) {
        launch(UI) {
            checkout.loadInventory(
                Inventory.Request.create().loadAllPurchases()
                    .loadSkus(ProductTypes.SUBSCRIPTION, SKUS)
            ) { products ->
                val subscriptions = products.get(ProductTypes.SUBSCRIPTION)
                val activeSkus = subscriptions.purchases.filter {
                    it.state == Purchase.State.PURCHASED && it.autoRenewing
                }.map { it.sku }.toSet()
                productsListener(subscriptions, activeSkus)
            }
        }
    }

    override fun subscribe(
        sku: String,
        successListener: (LocalDate) -> Unit,
        errorListener: (Int, Exception) -> Unit
    ) {
        val payload = UUID.randomUUID().toString()
        launch(UI) {
            checkout.destroyPurchaseFlow()
            checkout.startPurchaseFlow(ProductTypes.SUBSCRIPTION, sku, payload, object :
                EmptyRequestListener<Purchase>() {
                override fun onSuccess(purchase: Purchase) {
                    launch { successListener(DateUtils.fromMillis(purchase.time)) }
                }

                override fun onError(responseCode: Int, e: Exception) {
                    launch { errorListener(responseCode, e) }
                }
            })
        }
    }

    override fun changeSubscription(
        sku: String, activeSkus: Set<String>,
        successListener: (LocalDate, LocalDate) -> Unit,
        errorListener: (String, Exception?) -> Unit
    ) {
        val changeSubscriptionListener = object : RequestListener<Purchase> {
            override fun onSuccess(purchase: Purchase) {
                launch {
                    try {
                        val status = Api().getMembershipStatus(purchase.sku, purchase.token)
                        successListener(status.startDate, status.expirationDate)
                    } catch (e: Api.MembershipStatusException) {
                        errorListener(e.message!!, e)
                    }
                }

            }

            override fun onError(responseCode: Int, e: Exception) {
                launch { errorListener(responseCode.toString(), e) }
            }
        }

        launch(UI) {
            checkout.whenReady(object : Checkout.EmptyListener() {
                override fun onReady(requests: BillingRequests) {
                    val flow =
                        checkout.createOneShotPurchaseFlow(changeSubscriptionListener)
                    requests.changeSubscription(activeSkus.toList(), sku, null, flow)
                }
            })
        }
    }


}
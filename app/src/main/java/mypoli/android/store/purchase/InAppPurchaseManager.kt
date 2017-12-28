package mypoli.android.store.purchase

import android.content.res.Resources
import mypoli.android.R
import org.solovyev.android.checkout.*
import java.lang.Exception
import java.util.*


/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 12/28/17.
 */

enum class GemPackType {
    BASIC, SMART, PLATINUM
}

data class GemPack(
    val title: String,
    val shortTitle: String,
    val price: String,
    val gems: Int,
    val isPurchased: Boolean,
    val type: GemPackType
)

interface InAppPurchaseManager {
    interface PurchaseListener {
        fun onPurchased()
        fun onError()

    }

    fun loadAll(cb: (List<GemPack>) -> Unit)

    fun purchase(gemPack: GemPackType, purchaseListener: PurchaseListener)
}

class AndroidInAppPurchaseManager(private val checkout: UiCheckout, private val resources: Resources) : InAppPurchaseManager {

    companion object {
        val GEM_PACK_TYPE_TO_SKU = mapOf(
            GemPackType.BASIC to "test",
            GemPackType.SMART to "test",
            GemPackType.PLATINUM to "test"
        )

        val GEM_PACK_TYPE_TO_GEMS = mapOf(
            GemPackType.BASIC to 8,
            GemPackType.SMART to 12,
            GemPackType.PLATINUM to 20
        )
    }

    override fun loadAll(cb: (List<GemPack>) -> Unit) {

        val skus = listOf("test")
//        val skus = GEM_PACK_TYPE_TO_SKU.values.toList()

        checkout.loadInventory(
            Inventory.Request.create()
                .loadAllPurchases()
                .loadSkus(ProductTypes.IN_APP, skus)
        ) { products ->
            val appProducts = products.get(ProductTypes.IN_APP)

            val gemPacks = GEM_PACK_TYPE_TO_SKU.map { (k, v) ->
                val sku = appProducts.getSku(v)!!
                val gems = GEM_PACK_TYPE_TO_GEMS[k]!!

                val titleRes = when (k) {
                    GemPackType.BASIC -> R.string.gem_pack_basic_title
                    GemPackType.SMART -> R.string.gem_pack_smart_title
                    GemPackType.PLATINUM -> R.string.gem_pack_platinum_title
                }

                val shortTitleRes = when (k) {
                    GemPackType.BASIC -> R.string.gem_pack_basic_title_short
                    GemPackType.SMART -> R.string.gem_pack_smart_title_short
                    GemPackType.PLATINUM -> R.string.gem_pack_platinum_title_short
                }

                GemPack(
                    resources.getString(titleRes),
                    resources.getString(shortTitleRes),
                    sku.price,
                    gems,
                    appProducts.isPurchased(sku),
                    k
                )
            }

            cb(gemPacks)
        }
    }

    override fun purchase(gemPack: GemPackType, purchaseListener: InAppPurchaseManager.PurchaseListener) {
        val payload = UUID.randomUUID().toString()

        checkout.whenReady(object : Checkout.EmptyListener() {
            override fun onReady(requests: BillingRequests) {
                requests.purchase(
                    ProductTypes.IN_APP,
                    GEM_PACK_TYPE_TO_SKU[gemPack]!!,
                    payload,
                    checkout.createOneShotPurchaseFlow(object : RequestListener<Purchase> {
                        override fun onSuccess(result: Purchase) {
                            purchaseListener.onPurchased()
                        }

                        override fun onError(response: Int, e: Exception) {
                            purchaseListener.onError()
                        }
                    })
                )
            }
        })
    }
}
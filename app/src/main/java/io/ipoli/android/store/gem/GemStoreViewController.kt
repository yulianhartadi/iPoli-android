package io.ipoli.android.store.gem

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.SkuType
import com.android.billingclient.api.BillingClient.newBuilder
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.SkuDetailsParams
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import io.ipoli.android.Constants.Companion.GEM_PACK_TYPE_TO_GEMS
import io.ipoli.android.Constants.Companion.GEM_PACK_TYPE_TO_SKU
import io.ipoli.android.Constants.Companion.SKU_TO_GEM_PACK_TYPE
import io.ipoli.android.R
import io.ipoli.android.common.billing.BillingResponseHandler
import io.ipoli.android.common.redux.android.ReduxViewController
import io.ipoli.android.common.view.*
import io.ipoli.android.player.inventory.InventoryViewController
import io.ipoli.android.store.gem.GemStoreViewState.StateType.*
import kotlinx.android.synthetic.main.controller_gem_store.view.*
import kotlinx.android.synthetic.main.view_inventory_toolbar.view.*
import space.traversal.kapsule.required
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by Venelin Valkov <venelin@io.ipoli.io>
 * on 27.12.17.
 */
class GemStoreViewController(args: Bundle? = null) :
    ReduxViewController<GemStoreAction, GemStoreViewState, GemStoreReducer>(
        args
    ) {

    override val reducer = GemStoreReducer

    private val billingResponseHandler by required { billingResponseHandler }
    private val billingRequestExecutor by required { billingRequestExecutor }

    private lateinit var billingClient: BillingClient

    private val failureListener = object : BillingResponseHandler.FailureListener {

        override fun onCanceledByUser() {
            onBillingCanceledByUser()
        }

        override fun onDisconnected() {
            onBillingDisconnected()
        }

        override fun onUnavailable(responseCode: Int) {
            onBillingUnavailable()
        }

        override fun onError(responseCode: Int) {
            onBillingError()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        val view = inflater.inflate(R.layout.controller_gem_store, container, false)

        setToolbar(view.toolbar)
        view.toolbarTitle.setText(R.string.gem_store)

        setChildController(
            view.playerGems,
            InventoryViewController(showCurrencyConverter = false)
        )

        view.mostPopular.setCompoundDrawablesWithIntrinsicBounds(
            IconicsDrawable(view.context)
                .icon(GoogleMaterial.Icon.gmd_favorite)
                .colorRes(R.color.md_white)
                .sizeDp(24),
            null, null, null
        )

        billingClient =
            newBuilder(activity!!).setListener { responseCode, purchases ->
                billingResponseHandler.handle(responseCode, {
                    purchases!!.forEach {
                        dispatch(GemStoreAction.GemPackBought(SKU_TO_GEM_PACK_TYPE[it.sku]!!))
                    }
                    consumePurchases(purchases)
                }, failureListener)
            }
                .build()

        return view
    }

    private fun consumePurchases(purchases: List<Purchase>?, listener: (() -> Unit)? = null) {
        if (purchases == null || purchases.isEmpty()) {
            listener?.invoke()
            return
        }

        val count = AtomicInteger(0)
        purchases.forEach { p ->
            billingClient.execute { bc ->
                bc.consumeAsync(
                    p.purchaseToken
                ) { _, _ ->
                    if (count.incrementAndGet() == purchases.size) {
                        listener?.invoke()
                    }
                }
            }

        }
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        showBackButton()

        billingClient.execute { bc ->

            val params = SkuDetailsParams.newBuilder()
                .setSkusList(GEM_PACK_TYPE_TO_SKU.values.toList())
                .setType(SkuType.INAPP)
                .build()

            bc.querySkuDetailsAsync(params) { responseCode, skuDetailsList ->

                billingResponseHandler.handle(
                    responseCode = responseCode,
                    onSuccess = {
                        val gemPacks = GEM_PACK_TYPE_TO_SKU.map { (k, v) ->
                            val sku = skuDetailsList.first { it.sku == v }
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
                                stringRes(titleRes),
                                stringRes(shortTitleRes),
                                sku.price,
                                gems,
                                k
                            )
                        }

                        dispatch(GemStoreAction.Load(gemPacks))
                    },
                    failureListener = failureListener
                )
            }
        }
    }

    private fun onBillingUnavailable() {
        enableButtons()
        activity?.let {
            Toast.makeText(it, R.string.billing_unavailable, Toast.LENGTH_LONG).show()
        }
    }

    private fun onBillingError() {
        enableButtons()
        activity?.let {
            Toast.makeText(it, R.string.purchase_failed, Toast.LENGTH_LONG).show()
        }
    }

    private fun onBillingCanceledByUser() {
        enableButtons()
    }

    private fun onBillingDisconnected() {
        enableButtons()
        activity?.let {
            Toast.makeText(it, R.string.billing_disconnected, Toast.LENGTH_LONG).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            return router.handleBack()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDetach(view: View) {
        billingClient.endConnection()
        super.onDetach(view)
    }

    override fun render(state: GemStoreViewState, view: View) {

        when (state.type) {

            PLAYER_CHANGED ->
                if (state.isGiftPurchased)
                    showMostPopular(view)

            GEM_PACKS_LOADED -> {

                if (state.isGiftPurchased)
                    showMostPopular(view)

                state.gemPacks.forEach {
                    val gp = it
                    when (it.type) {
                        GemPackType.BASIC -> {
                            view.basicPackPrice.text = it.price
                            view.basicPackTitle.text = it.title
                            @SuppressLint("SetTextI18n")
                            view.basicPackGems.text = "x ${it.gems}"
                            view.basicPackBuy.onDebounceClick {
                                disableButtons()
                                buyPack(gp)
                            }
                        }
                        GemPackType.SMART -> {
                            view.smartPackPrice.text = it.price
                            view.smartPackTitle.text = it.title
                            @SuppressLint("SetTextI18n")
                            view.smartPackGems.text = "x ${it.gems}"
                            view.smartPackBuy.onDebounceClick {
                                disableButtons()
                                buyPack(gp)
                            }
                        }
                        GemPackType.PLATINUM -> {
                            view.platinumPackPrice.text = it.price
                            view.platinumPackTitle.text = it.title
                            @SuppressLint("SetTextI18n")
                            view.platinumPackGems.text = "x ${it.gems}"
                            view.platinumPackBuy.onDebounceClick {
                                disableButtons()
                                buyPack(gp)
                            }
                        }
                    }
                }
            }

            GEM_PACK_PURCHASED -> {
                enableButtons()
                Toast.makeText(view.context, R.string.gem_pack_purchased, Toast.LENGTH_LONG).show()
            }

            DOG_UNLOCKED -> {
                showMostPopular(view)
                Toast.makeText(view.context, R.string.gift_unlocked, Toast.LENGTH_LONG).show()
            }

            else -> {
            }
        }
    }

    private fun buyPack(gemPack: GemPack) {
        val r = billingClient.queryPurchases(BillingClient.SkuType.INAPP)

        billingResponseHandler.handle(r.responseCode, {
            consumePurchases(r.purchasesList) {

                val flowParams = BillingFlowParams.newBuilder()
                    .setSku(GEM_PACK_TYPE_TO_SKU[gemPack.type])
                    .setType(SkuType.INAPP)
                    .build()
                billingResponseHandler.handle(
                    responseCode = billingClient.launchBillingFlow(activity!!, flowParams),
                    listener = failureListener
                )

            }
        }, failureListener)
    }

    private fun enableButtons() {
        view!!.basicPackBuy.enableClick()
        view!!.smartPackBuy.enableClick()
        view!!.platinumPackBuy.enableClick()
    }

    private fun disableButtons() {
        view!!.basicPackBuy.disableClick()
        view!!.smartPackBuy.disableClick()
        view!!.platinumPackBuy.disableClick()
    }

    private fun showMostPopular(view: View) {
        view.giftContainer.visibility = View.GONE
        view.mostPopular.visibility = View.VISIBLE
    }

    private fun BillingClient.execute(request: (BillingClient) -> Unit) {
        billingRequestExecutor.execute(this, request, failureListener)
    }
}
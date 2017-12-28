package mypoli.android.store

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.controller_gem_store.view.*
import kotlinx.android.synthetic.main.view_inventory_toolbar.view.*
import mypoli.android.BillingConstants
import mypoli.android.R
import mypoli.android.common.mvi.MviViewController
import mypoli.android.common.view.addToolbarView
import mypoli.android.common.view.removeToolbarView
import mypoli.android.common.view.showBackButton
import mypoli.android.store.GemStoreViewState.StateType.*
import mypoli.android.store.purchase.AndroidInAppPurchaseManager
import mypoli.android.store.purchase.GemPackType
import org.solovyev.android.checkout.Billing
import org.solovyev.android.checkout.Checkout
import org.solovyev.android.checkout.UiCheckout
import space.traversal.kapsule.required

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 27.12.17.
 */
class GemStoreViewController(args: Bundle? = null) : MviViewController<GemStoreViewState, GemStoreViewController, GemStorePresenter, GemStoreIntent>(args) {

    private val presenter by required { gemStorePresenter }

    override fun createPresenter() = presenter

    private lateinit var inventoryToolbar: ViewGroup

    private lateinit var checkout: UiCheckout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {
        setHasOptionsMenu(true)
        val view = inflater.inflate(R.layout.controller_gem_store, container, false)

        inventoryToolbar = addToolbarView(R.layout.view_inventory_toolbar) as ViewGroup
        inventoryToolbar.toolbarTitle.setText(R.string.gem_store)

        val billing = Billing(activity!!, object : Billing.DefaultConfiguration() {
            override fun getPublicKey() =
                BillingConstants.appPublicKey
        })

        checkout = Checkout.forActivity(activity!!, billing)
        checkout.start()
        presenter.purchaseManager = AndroidInAppPurchaseManager(checkout, activity!!.resources)

        registerForActivityResult(AndroidInAppPurchaseManager.PURCHASE_REQUEST_CODE)

        return view
    }

    override fun onAttach(view: View) {
        showBackButton()
        super.onAttach(view)
        send(GemStoreIntent.LoadData)
    }

    override fun onDestroyView(view: View) {
        removeToolbarView(inventoryToolbar)
        checkout.stop()
        super.onDestroyView(view)
    }

    override fun render(state: GemStoreViewState, view: View) {
        when (state.type) {
            PLAYER_CHANGED -> {
                inventoryToolbar.playerGems.text = state.playerGems.toString()
                if (state.isGiftPurchased) {
                    view.giftContainer.visibility = View.GONE
                }
            }

            GEM_PACKS_LOADED -> {
                state.gemPacks.forEach {
                    when (it.type) {
                        GemPackType.BASIC -> {
                            view.basicPackPrice.text = it.price
                            view.basicPackTitle.text = it.title
                            view.basicPackBuy.sendOnClick(GemStoreIntent.BuyGemPack(it))
                        }
                        GemPackType.SMART -> {
                            view.smartPackPrice.text = it.price
                            view.smartPackTitle.text = it.title
                            view.smartPackBuy.sendOnClick(GemStoreIntent.BuyGemPack(it))
                        }
                        GemPackType.PLATINUM -> {
                            view.platinumPackPrice.text = it.price
                            view.platinumPackTitle.text = it.title
                            view.platinumPackBuy.sendOnClick(GemStoreIntent.BuyGemPack(it))
                        }
                    }
                }
            }

            GEM_PACK_PURCHASED -> {
                Toast.makeText(view.context, R.string.gem_pack_purchased, Toast.LENGTH_LONG).show()
            }

            DOG_UNLOCKED -> {
                view.giftContainer.visibility = View.GONE
                Toast.makeText(view.context, R.string.gift_unlocked, Toast.LENGTH_LONG).show()
            }

            PURCHASE_FAILED -> {
                Toast.makeText(view.context, R.string.purchase_failed, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        checkout.onActivityResult(requestCode, resultCode, data)
    }

}
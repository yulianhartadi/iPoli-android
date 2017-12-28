package mypoli.android.store

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.controller_gem_store.view.*
import kotlinx.android.synthetic.main.view_inventory_toolbar.view.*
import mypoli.android.R
import mypoli.android.common.mvi.MviViewController
import mypoli.android.common.view.addToolbarView
import mypoli.android.common.view.removeToolbarView
import mypoli.android.common.view.showBackButton
import mypoli.android.store.GemStoreViewState.StateType.PLAYER_CHANGED
import space.traversal.kapsule.required

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 27.12.17.
 */
class GemStoreViewController(args: Bundle? = null) : MviViewController<GemStoreViewState, GemStoreViewController, GemStorePresenter, GemStoreIntent>(args) {

    private val presenter by required { gemStorePresenter }

    override fun createPresenter() = presenter

    private lateinit var inventoryToolbar: ViewGroup

//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {
//        setHasOptionsMenu(true)
//        val view = inflater.inflate(R.layout.controller_pet_store, container, false)
//
//        inventoryToolbar = addToolbarView(R.layout.view_inventory_toolbar) as ViewGroup
//        inventoryToolbar.toolbarTitle.setText(R.string.store)
//        inventoryToolbar.playerGems.setOnClickListener {
//            send(PetStoreIntent.ShowCurrencyConverter)
//        }
//
//        view.petPager.clipToPadding = false
//        view.petPager.pageMargin = ViewUtils.dpToPx(16f, view.context).toInt()
//        return view
//    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {
        setHasOptionsMenu(true)
        val view = inflater.inflate(R.layout.controller_gem_store, container, false)

        inventoryToolbar = addToolbarView(R.layout.view_inventory_toolbar) as ViewGroup
        inventoryToolbar.toolbarTitle.setText(R.string.gem_store)

        view.basicPackBuy.setOnClickListener {

        }

        return view
    }

    override fun onAttach(view: View) {
        showBackButton()
        super.onAttach(view)
        send(GemStoreIntent.LoadData)
    }

    override fun onDestroyView(view: View) {
        removeToolbarView(inventoryToolbar)
        super.onDestroyView(view)
    }

    override fun render(state: GemStoreViewState, view: View) {
        when (state.type) {
            PLAYER_CHANGED -> {
                inventoryToolbar.playerGems.text = state.playerGems.toString()

            }
        }

    }

}
package mypoli.android.store

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import mypoli.android.R
import mypoli.android.common.mvi.MviViewController
import space.traversal.kapsule.required

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 27.12.17.
 */
class GemStoreViewController(args: Bundle? = null) : MviViewController<GemStoreViewState, GemStoreViewController, GemStorePresenter, GemStoreIntent>(args) {

    private val presenter by required { gemStorePresenter }

    override fun createPresenter() = presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {
        return inflater.inflate(R.layout.controller_gem_store, container, false)
    }

    override fun render(state: GemStoreViewState, view: View) {

    }

}
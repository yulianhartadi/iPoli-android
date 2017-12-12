package io.ipoli.android.theme

import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import io.ipoli.android.R
import io.ipoli.android.common.mvi.MviViewController
import io.ipoli.android.common.view.showBackButton
import io.ipoli.android.common.view.stringRes
import space.traversal.kapsule.required

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 12/12/17.
 */
class ThemeStoreViewController(args: Bundle? = null) :
    MviViewController<ThemeStoreViewState, ThemeStoreViewController, ThemeStorePresenter, ThemeStoreIntent>(args) {

    private val presenter by required { themeStorePresenter }

    override fun createPresenter() = presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {
        setHasOptionsMenu(true)
        val toolbar = activity!!.findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = stringRes(R.string.theme_store_title)
        val view = inflater.inflate(R.layout.controller_theme_store, container, false)
        return view
    }

    override fun onAttach(view: View) {
        showBackButton()
        super.onAttach(view)
        send(LoadDataIntent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            router.popCurrentController()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun render(state: ThemeStoreViewState, view: View) {
    }

}
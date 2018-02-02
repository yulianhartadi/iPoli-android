package mypoli.android.player.inventory

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import mypoli.android.R
import mypoli.android.common.AppState
import mypoli.android.common.mvi.ViewState
import mypoli.android.common.redux.Action
import mypoli.android.common.redux.android.AndroidStatePresenter
import mypoli.android.common.redux.android.ReduxViewController
import mypoli.android.common.view.CurrencyConverterDialogController

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 01/28/2018.
 */

class GemInventoryPresenter : AndroidStatePresenter<AppState, GemInventoryViewState> {
    override fun present(state: AppState, context: Context) =
        GemInventoryViewState(state.appDataState.player?.gems.toString())
}

data class GemInventoryViewState(val gems: String) : ViewState

class GemInventoryViewController(args: Bundle? = null) :
    ReduxViewController<Action, GemInventoryViewState, GemInventoryPresenter>(args) {

    override val presenter get() = GemInventoryPresenter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.controller_gem_inventory, container, false)
        view.setOnClickListener {
            CurrencyConverterDialogController().showDialog(parentController!!.router, "currency-converter")
        }
        return view
    }

    override fun render(state: GemInventoryViewState, view: View) {
        (view as TextView).text = state.gems
    }
}
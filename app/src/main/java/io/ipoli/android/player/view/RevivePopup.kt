package io.ipoli.android.player.view

import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import io.ipoli.android.R
import io.ipoli.android.common.redux.Action
import io.ipoli.android.common.view.Debounce
import io.ipoli.android.common.view.Popup
import kotlinx.android.synthetic.main.popup_revive.view.*
import space.traversal.kapsule.required

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 09/12/2018.
 */

object ReviveAction : Action

class RevivePopup : Popup(isAutoHide = true) {

    private val stateStore by required { stateStore }

    @SuppressLint("InflateParams")
    override fun createView(inflater: LayoutInflater): View {
        val view = inflater.inflate(R.layout.popup_revive, null)

        view.reviveLogo.background.setColorFilter(
            ContextCompat.getColor(view.context, R.color.evil_snail_background),
            PorterDuff.Mode.SRC_ATOP
        )
        view.revivePlayer.setOnClickListener(Debounce.clickListener {
            stateStore.dispatch(ReviveAction)
            hide()
        })
        return view
    }

}
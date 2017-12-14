package io.ipoli.android.common.view

import android.view.LayoutInflater
import android.view.View
import io.ipoli.android.R
import kotlinx.android.synthetic.main.overlay_pet_message.view.*

class PetMessageViewController(private val listener: UndoClickedListener) : BasePopup(position = Position.BOTTOM) {

    override fun createView(inflater: LayoutInflater): View =
        inflater.inflate(R.layout.overlay_pet_message, null)


    interface UndoClickedListener {
        fun onClick()
    }

    override fun onViewShown(contentView: View) {
        contentView.undoAction.setOnClickListener {
            listener.onClick()
            hide()
        }
    }
}
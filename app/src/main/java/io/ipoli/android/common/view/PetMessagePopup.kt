package io.ipoli.android.common.view

import android.view.LayoutInflater
import android.view.View
import io.ipoli.android.R
import io.ipoli.android.pet.AndroidPetAvatar
import kotlinx.android.synthetic.main.popup_pet_message.view.*
import java.util.concurrent.TimeUnit

class PetMessagePopup(
    private val message: String,
    private val avatar: AndroidPetAvatar,
    private val undoListener: () -> Unit
) : BasePopup(position = Position.BOTTOM) {

    override fun createView(inflater: LayoutInflater): View =
        inflater.inflate(R.layout.popup_pet_message, null)

    override fun onViewShown(contentView: View) {
        contentView.petMessage.text = message
        contentView.petHead.setImageResource(avatar.headImage)
        contentView.undoAction.setOnClickListener {
            undoListener()
            hide()
        }
        autoHideAfter(TimeUnit.SECONDS.toMillis(2))
    }
}
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
) : BasePopup(position = Position.BOTTOM, isAutoHide = true) {

    override fun createView(inflater: LayoutInflater): View {
        val v = inflater.inflate(R.layout.popup_pet_message, null)
        v.petMessage.text = message
        v.petHead.setImageResource(avatar.headImage)

        v.undoAction.setOnClickListener {
            undoListener()
            hide()
        }

        return v
    }


    override fun onViewShown(contentView: View) {
        autoHideAfter(TimeUnit.SECONDS.toMillis(2))
    }
}
package io.ipoli.android.quest

import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import io.ipoli.android.R
import io.ipoli.android.common.view.BasePopup
import kotlinx.android.synthetic.main.popup_reward.view.*

class RewardPopup : BasePopup() {

    override fun createView(inflater: LayoutInflater): View  {
        val view = inflater.inflate(R.layout.popup_reward, null)

        return view
    }

    override fun onViewShown(contentView: View) {
        val reward = contentView.reward as ImageView
        Glide.with(contentView.context)
            .load("https://media3.giphy.com/media/pFnnMFXgQgkrC/giphy.gif")
            .into(reward)
    }
}
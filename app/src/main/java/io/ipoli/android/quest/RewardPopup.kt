package io.ipoli.android.quest

import android.view.LayoutInflater
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import io.ipoli.android.R
import io.ipoli.android.common.view.BasePopup
import kotlinx.android.synthetic.main.popup_reward.view.*

class RewardPopup : BasePopup() {

    override fun createView(inflater: LayoutInflater): View  {
        val view = inflater.inflate(R.layout.popup_reward, null)
        val reward = view.reward
        Glide.with(view.context)
            .load("https://media3.giphy.com/media/pFnnMFXgQgkrC/giphy.gif")
            .apply(RequestOptions().error(R.drawable.pet_5_head).timeout(500))
            .into(reward)
        return view
    }

    override fun onEnterAnimationEnd(contentView: View) {

    }
}
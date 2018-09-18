package io.ipoli.android.player.view

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import io.ipoli.android.MyPoliApp
import io.ipoli.android.R
import io.ipoli.android.common.EmailUtils
import io.ipoli.android.common.view.Popup
import kotlinx.android.synthetic.main.popup_secret_society.view.*

class SecretSocietyPopup : Popup() {

    @SuppressLint("InflateParams")
    override fun createView(inflater: LayoutInflater): View {
        val view = inflater.inflate(R.layout.popup_secret_society, null)
        view.societyInvite.setOnClickListener {
            hide()
            EmailUtils.send(
                MyPoliApp.instance,
                "Invite me to myPoli secret society",
                FirebaseAuth.getInstance().currentUser?.uid,
                "Secret society invite"
            )
        }
        return view
    }

}
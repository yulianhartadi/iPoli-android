package io.ipoli.android.common.privacy

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.ipoli.android.Constants
import io.ipoli.android.R
import io.ipoli.android.common.redux.android.ReduxViewController
import io.ipoli.android.common.view.enterFullScreen
import io.ipoli.android.common.view.exitFullScreen
import io.ipoli.android.common.view.rootRouter
import io.ipoli.android.common.view.visible
import kotlinx.android.synthetic.main.controller_privacy_policy.view.*
import space.traversal.kapsule.required

class PrivacyPolicyViewController(args: Bundle? = null) :
    ReduxViewController<PrivacyPolicyAction, PrivacyPolicyViewState, PrivacyPolicyReducer>(args) {

    private val sharedPreferences by required { sharedPreferences }

    override val reducer = PrivacyPolicyReducer

    @SuppressLint("ApplySharedPref")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.controller_privacy_policy, container, false)
        view.firebasePrivacyPolicy.paintFlags = view.firebasePrivacyPolicy.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        view.amplitudePrivacyPolicy.paintFlags = view.amplitudePrivacyPolicy.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        view.firebasePrivacyPolicy.onDebounceClick {
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse("https://firebase.google.com/support/privacy/")
            startActivity(i)
        }

        view.amplitudePrivacyPolicy.onDebounceClick {
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse("https://amplitude.com/privacy")
            startActivity(i)
        }

        if(sharedPreferences.getInt(Constants.KEY_PRIVACY_ACCEPTED_VERSION, -1) != Constants.PRIVACY_POLICY_VERSION) {
            view.acceptPrivacy.visible()
            view.acceptPrivacy.onDebounceClick {
                sharedPreferences.edit().putInt(Constants.KEY_PRIVACY_ACCEPTED_VERSION, Constants.PRIVACY_POLICY_VERSION).commit()
                rootRouter.popCurrentController()
                activity?.recreate()
            }
        }

        return view
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        enterFullScreen()
    }

    override fun onDetach(view: View) {
        exitFullScreen()
        super.onDetach(view)
    }

    override fun render(state: PrivacyPolicyViewState, view: View) {

    }
}
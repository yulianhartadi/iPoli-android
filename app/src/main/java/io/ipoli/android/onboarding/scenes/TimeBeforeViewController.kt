package io.ipoli.android.onboarding.scenes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.ipoli.android.R
import io.ipoli.android.common.redux.android.BaseViewController
import io.ipoli.android.common.view.inflate
import io.ipoli.android.onboarding.OnboardAction
import io.ipoli.android.onboarding.OnboardReducer
import io.ipoli.android.onboarding.OnboardViewState

class TimeBeforeViewController(args: Bundle? = null) :
    BaseViewController<OnboardAction, OnboardViewState>(args) {

    override val stateKey = OnboardReducer.stateKey

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ) =
        container.inflate(R.layout.controller_onboard_time_before)

    override fun onAttach(view: View) {
        super.onAttach(view)
        view.postDelayed({
            dispatch(OnboardAction.ShowNext)
        }, 1500)
    }

    override fun render(state: OnboardViewState, view: View) {

    }
}
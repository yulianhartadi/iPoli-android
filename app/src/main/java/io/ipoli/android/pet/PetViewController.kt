package io.ipoli.android.pet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.ipoli.android.R
import io.ipoli.android.common.mvi.MviViewController
import space.traversal.kapsule.required

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 11/24/17.
 */
class PetViewController(args: Bundle? = null) : MviViewController<PetViewState, PetViewController, PetPresenter, PetIntent>(args) {

    private val presenter by required { petPresenter }

    override fun createPresenter() = presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View =
        inflater.inflate(R.layout.controller_pet, container, false)

    override fun render(state: PetViewState, view: View) {

    }

}
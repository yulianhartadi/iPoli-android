package io.ipoli.android.pet.shop

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.ipoli.android.R
import io.ipoli.android.common.mvi.MviViewController
import space.traversal.kapsule.required

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 12/4/17.
 */
class PetShopViewController(args: Bundle? = null) : MviViewController<PetShopViewState, PetShopViewController, PetShopPresenter, PetShopIntent>(args) {
    private val presenter by required { petShopPresenter}

    override fun createPresenter() = presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?) =
        inflater.inflate(R.layout.controller_pet_shop, container, false)


    override fun render(state: PetShopViewState, view: View) {
    }

}
package io.ipoli.android.store.avatars

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.ipoli.android.R
import io.ipoli.android.common.BaseController
import io.ipoli.android.daggerComponent
import io.ipoli.android.store.DaggerStoreComponent
import io.ipoli.android.store.StoreComponent

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 8/20/17.
 */
class AvatarListController : BaseController<AvatarListController, AvatarListPresenter>() {

    val avatarListComponent: AvatarListComponent by lazy {
        val component = DaggerAvatarListComponent.builder()
            .controllerComponent(daggerComponent)
            .build()
        component.inject(this@AvatarListController)
        component
    }

    override fun createPresenter(): AvatarListPresenter = avatarListComponent.createAvatarListPresenter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {
        return inflater.inflate(R.layout.controller_avatar_list , container, false)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        avatarListComponent // will ensure that dagger component will be initialized lazily.
    }

    override fun setRestoringViewState(restoringViewState: Boolean) {
    }

}


package io.ipoli.android.store

import dagger.Component
import io.ipoli.android.common.di.ControllerComponent
import javax.inject.Scope

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 8/18/17.
 */
@StoreScope
@Component(modules = arrayOf(StoreItemsModule::class),
        dependencies = arrayOf(ControllerComponent::class))
interface StoreItemsComponent {

    fun inject(controller: StoreItemsController)

    fun createStoreItemsPresenter(): StoreItemsPresenter
}

@Scope
annotation class StoreScope
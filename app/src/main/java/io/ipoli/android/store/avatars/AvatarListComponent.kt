package io.ipoli.android.store.avatars

import dagger.Component
import io.ipoli.android.common.BaseComponent
import io.ipoli.android.common.di.ControllerComponent
import javax.inject.Scope

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 8/20/17.
 */
@AvatarListScope
@Component(modules = arrayOf(AvatarListModule::class),
    dependencies = arrayOf(ControllerComponent::class))
interface AvatarListComponent : BaseComponent<AvatarListController, AvatarListPresenter>

@Scope
annotation class AvatarListScope
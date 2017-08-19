package io.ipoli.android.common

import android.os.Bundle
import com.bluelinelabs.conductor.ControllerChangeHandler
import com.bluelinelabs.conductor.ControllerChangeType
import com.hannesdorfmann.mosby3.RestoreViewOnCreateMviController
import com.hannesdorfmann.mosby3.mvi.MviPresenter
import com.hannesdorfmann.mosby3.mvp.MvpView
import io.ipoli.android.iPoliApp

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/19/17.
 */
abstract class BaseController<V : MvpView, P : MviPresenter<V, *>> : RestoreViewOnCreateMviController<V, P> {

    private var hasExited = false

    constructor() : super()
    constructor(args: Bundle) : super(args)

    public override fun onDestroy() {
        super.onDestroy()

        if (hasExited) {
            iPoliApp.refWatcher.watch(this)
        }
    }

    override fun onChangeEnded(changeHandler: ControllerChangeHandler, changeType: ControllerChangeType) {
        super.onChangeEnded(changeHandler, changeType)

        hasExited = !changeType.isEnter
        if (isDestroyed) {
            iPoliApp.refWatcher.watch(this)
        }
    }

}
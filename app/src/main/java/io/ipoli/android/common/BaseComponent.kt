package io.ipoli.android.common

import com.hannesdorfmann.mosby3.mvi.MviPresenter
import com.hannesdorfmann.mosby3.mvp.MvpView

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 8/27/17.
 */
interface BaseComponent<V : MvpView, out P : MviPresenter<V, *>> {

    fun inject(view: V)

    fun createPresenter(): P
}
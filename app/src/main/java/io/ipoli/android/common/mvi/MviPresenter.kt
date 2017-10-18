package io.ipoli.android.common.mvi

import android.support.annotation.MainThread
import io.ipoli.android.common.RxUseCase
import io.ipoli.android.quest.calendar.dayview.view.DayViewIntent
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 9/8/17.
 */

interface ViewStateRenderer<in ViewState> {
    @MainThread
    fun render(state: ViewState)
}

interface MviPresenter<in V : ViewStateRenderer<VS>, in VS> {

    fun onAttachView(view: V)

    fun onDetachView() {}

    fun onDestroy() {}
}

abstract class CoroutineMviPresenter<V : ViewStateRenderer<VS>, VS>(private val intentChannel: ReceiveChannel<DayViewIntent>, private val initialState: VS) :
    MviPresenter<V, VS> {

    private fun stateReduceActor(view: V) = actor<DayViewIntent> {
        val state = initialState
        view.render(state)
        channel.consumeEach { intent ->
            val newState = reduceState(intent, state)
            launch(UI) {
                view.render(newState)
            }
        }
    }

    override fun onAttachView(view: V) {
        val actor = stateReduceActor(view)
        launch {
            intentChannel.consumeEach {
                actor.send(it)
            }
        }
    }

    abstract fun reduceState(intent: DayViewIntent, state: VS): VS
}

abstract class BaseMviPresenter<V : ViewStateRenderer<VS>, VS>(initialState: VS) :
    MviPresenter<V, VS> {

    private var viewAttachedFirstTime = true

    private val intentRelaysBinders = mutableListOf<RelayBinderPair<V, *>>()

    private val viewStateBehaviorSubject = BehaviorSubject.createDefault(initialState)

    private var intentDisposables: CompositeDisposable? = null

    private var viewRelayConsumerDisposable: Disposable? = null

    private var viewStateDisposable: Disposable? = null

    private interface ViewIntentBinder<in V, I> {
        fun bind(view: V): Observable<I>
    }

    private inner class DisposableIntentObserver<I>(private val subject: PublishSubject<I>) : DisposableObserver<I>() {

        override fun onNext(value: I) {
            subject.onNext(value)
        }

        override fun onError(e: Throwable) {
            throw IllegalStateException("View intents must not throw errors", e)
        }

        override fun onComplete() {
            subject.onComplete()
        }
    }

    private data class RelayBinderPair<in V, I>(val relay: PublishSubject<I>,
                                                val binder: ViewIntentBinder<V, I>)

    private inner class DisposableViewStateObserver<VS>(private val subject: BehaviorSubject<VS>) : DisposableObserver<VS>() {

        override fun onNext(value: VS) {
            subject.onNext(value)
        }

        override fun onError(e: Throwable) {
            throw IllegalStateException(
                "ViewState observable must not reach error state - onError()", e)
        }

        override fun onComplete() {
            // Never completes
        }
    }

    init {
        reset()
    }

    override fun onAttachView(view: V) {
        if (viewAttachedFirstTime) {
            val allIntents = Observable.merge(bindIntents())
                .observeOn(AndroidSchedulers.mainThread())

            subscribeViewState(allIntents.distinctUntilChanged())
        }

        subscribeViewStateConsumer(view)
        subscribeIntents(view)

        viewAttachedFirstTime = false
    }

    private fun subscribeIntents(view: V) {
        intentRelaysBinders.forEach {
            subscribeIntent(view, it)
        }
    }

    @MainThread
    private fun subscribeViewState(viewStateObservable: Observable<VS>) {
        viewStateDisposable = viewStateObservable.subscribeWith(
            DisposableViewStateObserver(viewStateBehaviorSubject))
    }

    @MainThread
    protected abstract fun bindIntents(): List<Observable<VS>>

    @MainThread
    private fun subscribeViewStateConsumer(view: V) {
        viewRelayConsumerDisposable = viewStateBehaviorSubject.subscribe {
            view.render(it)
        }
    }

    @MainThread
    protected fun <I> on(handler: (V) -> Observable<I>): Observable<Pair<VS, I>> =
        on(object : ViewIntentBinder<V, I> {
            override fun bind(view: V): Observable<I> =
                handler(view)
        }).map { intentParams ->
            Pair(viewStateBehaviorSubject.value, intentParams)
        }

    private fun <I> on(binder: ViewIntentBinder<V, I>): Observable<I> {
        val intentRelay = PublishSubject.create<I>()
        intentRelaysBinders.add(RelayBinderPair(intentRelay, binder))
        return intentRelay
    }

    @MainThread
    private fun <I> subscribeIntent(view: V,
                                    relayBinderPair: RelayBinderPair<V, I>) {
        val intent = relayBinderPair.binder.bind(view)

        if (intentDisposables == null) {
            intentDisposables = CompositeDisposable()
        }

        val disposableRelay = DisposableIntentObserver(relayBinderPair.relay)
        intentDisposables?.add(intent.subscribeWith(disposableRelay))
    }

    protected fun unbindIntents() {}

    override fun onDetachView() {

        viewRelayConsumerDisposable?.dispose()
        viewRelayConsumerDisposable = null

        intentDisposables?.dispose()
        intentDisposables = null
    }

    override fun onDestroy() {
        viewStateDisposable?.dispose()
        unbindIntents()
        reset()
    }

    private fun reset() {
        viewAttachedFirstTime = true
        intentRelaysBinders.clear()
    }

    protected fun <T, R> Observable<Pair<VS, T>>.execute(
        useCase: RxUseCase<T, R>,
        transformer: ObservableTransformer<R, R> = runOnIO()
    ): Observable<Pair<VS, R>> =
        switchMap { (state, params) ->
            useCase.execute(params)
                .compose(transformer)
                .map { result ->
                    Pair(state, result)
                }
        }

    protected fun <T, R> Observable<Pair<VS, T>>.executeAndReduce(
        useCase: RxUseCase<T, R>,
        reducer: (VS, R) -> VS,
        transformer: ObservableTransformer<VS, VS> = runOnIO()
    ): Observable<VS> =
        switchMap { (state, params) ->
            useCase.execute(params)
                .map { reducer(state, it) }
                .compose(transformer)
        }

    protected fun <T> runOnComputation(): ObservableTransformer<T, T> {
        return ObservableTransformer { observable ->
            observable.subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
        }
    }

    protected fun <T> runOnIO(): ObservableTransformer<T, T> {
        return ObservableTransformer { observable ->
            observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        }
    }
}
package mypoli.android.common.redux

import mypoli.android.common.redux.MiddleWare.Result.Continue
import mypoli.android.quest.calendar.CalendarAction
import mypoli.android.quest.calendar.CalendarReducer
import mypoli.android.quest.calendar.CalendarState

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 01/20/2018.
 */

interface Action

interface AsyncAction : Action {
    suspend fun execute(dispatcher: Dispatcher)
}

interface State

data class AppState(
    val calendarState: CalendarState
) : State


object AppReducer : Reducer<AppState, Action> {

    override fun reduce(state: AppState, action: Action) =
        when (action) {
            is CalendarAction -> state.copy(
                calendarState = CalendarReducer.reduce(state.calendarState, action)
            )
            else -> state
        }

    override fun defaultState() =
        AppState(
            calendarState = CalendarReducer.defaultState()
        )
}

interface Reducer<S : State, in A : Action> {

    fun reduce(state: S, action: A): S

    fun defaultState(): S
}

interface Dispatcher {
    fun dispatch(action: Action)
}

class StateStore<out S : State>(
    private val reducer: Reducer<S, Action>,
    middleware: List<MiddleWare<S>> = listOf()
) : Dispatcher {

    interface StateChangeSubscriber<in S, T> {

        interface StateTransformer<in S, out T> {
            fun transform(state: S): T
        }

        val transformer: StateTransformer<S, T>

        fun changeIfNew(oldState: S, newState: S) {
            val old = transformer.transform(oldState)
            val new = transformer.transform(newState)
            if (old != new) onStateChanged(new)
        }

        fun onStateChanged(newState: T)
    }

    interface SimpleStateChangeSubscriber<S> : StateChangeSubscriber<S, S> {
        override val transformer
            get() = object : StateChangeSubscriber.StateTransformer<S, S> {
                override fun transform(state: S) = state
            }
    }

    private var stateChangeSubscribers: List<StateChangeSubscriber<S, *>> = listOf()
    private var state = reducer.defaultState()
    private val middleWare = CompositeMiddleware<S>(middleware)

    override fun dispatch(action: Action) {
        val res = middleWare.execute(state, this, action)
        if (res == Continue) changeState(action)
    }

    private fun changeState(action: Action) {
        val newState = reducer.reduce(state, action)
        val oldState = state
        state = newState
        notifyStateChanged(oldState, newState)
    }

    private fun notifyStateChanged(oldState: S, newState: S) {
        stateChangeSubscribers.forEach {
            it.changeIfNew(oldState, newState)
        }
    }

    fun <T> subscribe(subscriber: StateChangeSubscriber<S, T>) {
        stateChangeSubscribers += subscriber
        subscriber.onStateChanged(subscriber.transformer.transform(state))
    }

    fun <T> unsubscribe(subscriber: StateChangeSubscriber<S, T>) {
        stateChangeSubscribers -= subscriber
    }
}
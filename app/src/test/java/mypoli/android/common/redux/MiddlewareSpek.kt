package mypoli.android.common.redux

import kotlinx.coroutines.experimental.runBlocking
import mypoli.android.common.AppState
import mypoli.android.common.redux.MiddleWare.Result.Continue
import mypoli.android.common.redux.MiddleWare.Result.Stop
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be false`
import org.amshove.kluent.`should be true`
import org.amshove.kluent.`should be`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 01/20/2018.
 */

object MiddlewareSpek : Spek({

    class TestState : State

    class TestDispatcher : Dispatcher {
        override fun <A : Action> dispatch(action: A) {}
    }

    class TestAction : Action

    var executeCount = 0

    beforeEachTest {
        executeCount = 0
    }

    fun executeMiddleware(middleWare: MiddleWare<TestState>, action: Action = TestAction()) =
        middleWare.execute(TestState(), TestDispatcher(), action)

    class CountExecutionsMiddleware : SimpleMiddleware<TestState> {

        override fun onExecute(
            state: TestState,
            dispatcher: Dispatcher,
            action: Action
        ) {
            executeCount++
        }
    }

    class StopMiddleware : MiddleWare<TestState> {
        override fun execute(
            state: TestState,
            dispatcher: Dispatcher,
            action: Action
        ) = Stop
    }

    describe("CompositeMiddleware") {

        it("should call all middleware") {

            val m = CompositeMiddleware(
                listOf(
                    CountExecutionsMiddleware(),
                    CountExecutionsMiddleware()
                )
            )
            val result = executeMiddleware(m)
            executeCount.`should be equal to`(2)
            result.`should be`(Continue)
        }

        it("should stop after first middleware") {

            val m = CompositeMiddleware(
                listOf(
                    CountExecutionsMiddleware(),
                    StopMiddleware()
                )
            )
            val result = executeMiddleware(m)
            executeCount.`should be equal to`(1)
            result.`should be`(Stop)
        }

        it("should stop at first middleware") {

            val m = CompositeMiddleware(
                listOf(
                    StopMiddleware(),
                    CountExecutionsMiddleware()
                )
            )
            val result = executeMiddleware(m)
            executeCount.`should be equal to`(0)
            result.`should be`(Stop)
        }

        it("should be able to handle action if one middleware can") {
            val m = CompositeMiddleware(
                listOf(
                    object : MiddleWare<AppState> {
                        override fun execute(
                            state: AppState,
                            dispatcher: Dispatcher,
                            action: Action
                        ) = Continue

                        override fun canHandle(action: Action) = false
                    },
                    StopMiddleware()
                )
            )

            m.canHandle(TestAction()).`should be true`()
        }

        it("should be not be able to handle action if no middleware can") {
            val m = CompositeMiddleware(
                listOf(
                    object : MiddleWare<AppState> {
                        override fun execute(
                            state: AppState,
                            dispatcher: Dispatcher,
                            action: Action
                        ) = Continue

                        override fun canHandle(action: Action) = false
                    },
                    object : MiddleWare<AppState> {
                        override fun execute(
                            state: AppState,
                            dispatcher: Dispatcher,
                            action: Action
                        ) = Continue

                        override fun canHandle(action: Action) = false
                    }
                )
            )

            m.canHandle(TestAction()).`should be false`()
        }
    }

    describe("SagaMiddleware") {

        var asyncExecutes = 0

        class TestSaga : Saga {
            override suspend fun execute(action: Action, dispatcher: Dispatcher) {
                asyncExecutes++
            }

            override fun canHandle(action: Action) = action is TestAction

        }

        beforeEachTest {
            asyncExecutes = 0
        }

        it("should execute saga") {

            runBlocking {
                executeMiddleware(
                    SagaMiddleware<TestState>(coroutineContext, handlers = listOf(TestSaga())),
                    TestAction()
                )
            }
            asyncExecutes.`should be equal to`(1)
        }
    }

})


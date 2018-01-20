package mypoli.android.common.redux

import mypoli.android.common.redux.MiddleWare.Result.Continue
import mypoli.android.common.redux.MiddleWare.Result.Stop
import org.amshove.kluent.`should be equal to`
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
        override fun dispatch(action: Action) {}
    }

    class TestAction : Action

    describe("CompositeMiddleware") {
        var executeCount = 0

        beforeEachTest {
            executeCount = 0
        }

        fun executeMiddleware(middleWare: MiddleWare<TestState>) =
            middleWare.execute(TestState(), TestDispatcher(), TestAction())

        class TestMiddleware : SimpleMiddleware<TestState>() {

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

        it("should call all middleware") {

            val cMiddleware = CompositeMiddleware(listOf(TestMiddleware(), TestMiddleware()))
            val result = executeMiddleware(cMiddleware)
            executeCount.`should be equal to`(2)
            result.`should be`(Continue)
        }

        it("should stop after first middleware") {

            val m = CompositeMiddleware(
                listOf(
                    TestMiddleware(),
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
                    TestMiddleware()
                )
            )
            val result = executeMiddleware(m)
            executeCount.`should be equal to`(0)
            result.`should be`(Stop)
        }
    }


})


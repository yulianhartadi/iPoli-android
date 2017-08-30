package io.ipoli.android.reward.edit

import io.ipoli.android.util.RxSchedulersSpek
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.amshove.kluent.`should be instance of`
import org.amshove.kluent.`should equal`
import org.jetbrains.spek.api.dsl.context
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.include
import org.jetbrains.spek.subject.SubjectSpek

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/29/17.
 */
class EditRewardUseCaseSpek : SubjectSpek<EditRewardUseCase>({

    subject { EditRewardUseCase() }

    include(RxSchedulersSpek)

    //@TODO fix this
    RxAndroidPlugins.reset()
    RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }

    RxJavaPlugins.reset()
    RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
    RxJavaPlugins.setComputationSchedulerHandler { Schedulers.trampoline() }
    RxJavaPlugins.setNewThreadSchedulerHandler { Schedulers.trampoline() }

    describe("create Reward") {


        val reward = EditRewardViewModel("",
            "reward 1",
            "desc 1",
            200)

        val states = subject.execute(Parameters(reward)).blockingIterable()
        it("should have 1 state") {
            states.count() `should equal` 1
        }

        context("state") {

            val state = states.first()
            it("should be Created") {
                state `should be instance of` EditRewardViewState.Created::class
            }
        }
    }

    describe("create new Reward without name") {

        val reward = EditRewardViewModel("",
            "",
            "desc 1",
            200)

        val states = subject.execute(Parameters(reward)).blockingIterable()

        it("should have 1 state") {
            states.count() `should equal` 1
        }

        context("state") {

            val state = states.first()
            it("should be Invalid") {
                state `should be instance of` EditRewardViewState.Invalid::class
            }

            it("should have value") {
                val expectedState = EditRewardViewState.Invalid(listOf(EditRewardViewState.ValidationError.EMPTY_NAME))
                state `should equal` expectedState
            }
        }
    }
})
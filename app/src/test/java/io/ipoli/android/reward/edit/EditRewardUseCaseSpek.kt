package io.ipoli.android.reward.edit

import io.ipoli.android.common.jobservice.JobQueue
import io.ipoli.android.util.RxSchedulersSpek
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.amshove.kluent.`should be instance of`
import org.amshove.kluent.`should equal`
import org.amshove.kluent.mock
import org.jetbrains.spek.api.dsl.SpecBody
import org.jetbrains.spek.api.dsl.context
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.include
import org.jetbrains.spek.subject.SubjectSpek
import org.jetbrains.spek.subject.dsl.SubjectProviderDsl
import kotlin.reflect.KClass

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/29/17.
 */
class EditRewardUseCaseSpek : SubjectSpek<EditRewardUseCase>({

    val jobQueue = mock<JobQueue>()
    subject { EditRewardUseCase(jobQueue) }

    include(RxSchedulersSpek)

    //@TODO fix this
    setupRxJava()

    describe("create Reward") {
        val states = executeUseCase(createReward())

        checkForSingleState(states)

        checkFirstState(states,
            EditRewardViewState.Added::class,
            EditRewardViewState.Added)
    }

    describe("create Reward without name") {

        val states = executeUseCase(createReward(""))

        checkForSingleState(states)

        val validationErrors = listOf(EditRewardViewState.ValidationError.EMPTY_NAME)

        checkFirstState(states,
            EditRewardViewState.Invalid::class,
            EditRewardViewState.Invalid(validationErrors))
    }

    describe("create Reward with too long name") {
        val reward = createReward("s".repeat(51))

        val states = executeUseCase(reward)

        checkForSingleState(states)

        val validationErrors = listOf(EditRewardViewState.ValidationError.NAME_TOO_LONG)

        checkFirstState(states,
            EditRewardViewState.Invalid::class,
            EditRewardViewState.Invalid(validationErrors))
    }

    describe("create Reward with too long description") {
        val states = executeUseCase(createReward(description = "s".repeat(101)))

        checkForSingleState(states)

        val validationErrors = listOf(
            EditRewardViewState.ValidationError.DESCRIPTION_TOO_LONG
        )

        checkFirstState(states,
            EditRewardViewState.Invalid::class,
            EditRewardViewState.Invalid(validationErrors))
    }

    describe("create Reward with empty name and long description") {
        val states = executeUseCase(createReward("", "s".repeat(101)))

        checkForSingleState(states)

        val validationErrors = listOf(
            EditRewardViewState.ValidationError.EMPTY_NAME,
            EditRewardViewState.ValidationError.DESCRIPTION_TOO_LONG
        )

        checkFirstState(states,
            EditRewardViewState.Invalid::class,
            EditRewardViewState.Invalid(validationErrors))
    }

    describe("create Reward with negative price") {
        val states = executeUseCase(createReward(price = -1))

        checkForSingleState(states)

        val validationErrors = listOf(
            EditRewardViewState.ValidationError.NEGATIVE_PRICE
        )

        checkFirstState(states,
            EditRewardViewState.Invalid::class,
            EditRewardViewState.Invalid(validationErrors))
    }

    describe("edit Reward") {
        val useCase = EditRewardUseCase(jobQueue, "123")
        val states = useCase.execute(Parameters(createReward())).blockingIterable()

        checkForSingleState(states)

        checkFirstState(states,
            EditRewardViewState.Updated::class,
            EditRewardViewState.Updated)
    }
})

private fun <T : EditRewardViewState> SpecBody.checkFirstState(states: Iterable<EditRewardViewState>, stateType: KClass<T>, expected: EditRewardViewState) {
    context("state") {
        val state = states.first()
        checkStateType(state, stateType)
        checkStateValue(state, expected)
    }
}

private fun SpecBody.checkStateValue(state: EditRewardViewState, expected: EditRewardViewState) {
    it("should have value") {
        state `should equal` expected
    }
}

private fun <T : EditRewardViewState> SpecBody.checkStateType(expectedState: EditRewardViewState, actualStateClass: KClass<T>) {
    it("should be " + expectedState.javaClass.simpleName) {
        expectedState `should be instance of` actualStateClass
    }
}

private fun SpecBody.checkForSingleState(states: Iterable<EditRewardViewState>) =
    checkStateCount(states, 1)

private fun SpecBody.checkStateCount(states: Iterable<EditRewardViewState>, count: Int) {
    it("should have $count state") {
        states.count() `should equal` count
    }
}

private fun SubjectProviderDsl<EditRewardUseCase>.executeUseCase(reward: EditRewardViewModel) =
    subject.execute(Parameters(reward)).blockingIterable()

private fun createReward(name: String = "reward", description: String = "desc",
                         price: Int = 200): EditRewardViewModel =
    EditRewardViewModel(
        name,
        description,
        price
    )

private fun setupRxJava() {
    RxAndroidPlugins.reset()
    RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }

    RxJavaPlugins.reset()
    RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
    RxJavaPlugins.setComputationSchedulerHandler { Schedulers.trampoline() }
    RxJavaPlugins.setNewThreadSchedulerHandler { Schedulers.trampoline() }
}
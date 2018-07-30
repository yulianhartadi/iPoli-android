package io.ipoli.android.common

import io.ipoli.android.challenge.predefined.category.list.ChallengeListForCategoryAction
import io.ipoli.android.challenge.usecase.BuyChallengeUseCase
import io.ipoli.android.common.di.BackgroundModule
import io.ipoli.android.common.redux.Action
import io.ipoli.android.common.redux.Dispatcher
import io.ipoli.android.common.redux.SideEffectHandler
import io.ipoli.android.MyPoliApp
import io.ipoli.android.pet.store.PetStoreAction
import io.ipoli.android.pet.usecase.BuyPetUseCase
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.launch
import space.traversal.kapsule.Injects
import space.traversal.kapsule.inject
import space.traversal.kapsule.required

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 01/27/2018.
 */

abstract class AppSideEffectHandler : SideEffectHandler<AppState>,
    Injects<BackgroundModule> {

    private var dispatcher: Dispatcher? = null

    override fun onCreate() {
        inject(MyPoliApp.backgroundModule(MyPoliApp.instance))
    }

    override suspend fun execute(action: Action, state: AppState, dispatcher: Dispatcher) {
        this.dispatcher = dispatcher
        doExecute(action, state)
    }

    abstract suspend fun doExecute(
        action: Action,
        state: AppState
    )

    fun dispatch(action: Action) {
        dispatcher!!.dispatch(action)
    }

    protected fun <E> listenForChanges(
        oldChannel: Channel<E>?,
        channelCreator: () -> Channel<E>,
        onResult: (E) -> Unit
    ) {
        oldChannel?.close()
        val newChannel = channelCreator()

        launch(CommonPool) {
            for (d in newChannel) {
                onResult(d)
            }
        }
    }
}

object BuyPredefinedChallengeSideEffectHandler : AppSideEffectHandler() {

    override suspend fun doExecute(action: Action, state: AppState) {
        val challenge = (action as ChallengeListForCategoryAction.BuyChallenge).challenge
        val result = buyChallengeUseCase.execute(BuyChallengeUseCase.Params(challenge))
        when (result) {
            is BuyChallengeUseCase.Result.ChallengeBought -> {
                dispatch(ChallengeListForCategoryAction.ChallengeBought(challenge))
            }

            BuyChallengeUseCase.Result.TooExpensive -> {
                dispatch(
                    ChallengeListForCategoryAction.ChallengeTooExpensive(
                        challenge
                    )
                )
            }
        }
    }

    private val buyChallengeUseCase by required { buyChallengeUseCase }

    override fun canHandle(action: Action) = action is ChallengeListForCategoryAction.BuyChallenge
}

object ChangePetSideEffectHandler : AppSideEffectHandler() {

    private val changePetUseCase by required { changePetUseCase }

    override suspend fun doExecute(action: Action, state: AppState) {
        changePetUseCase.execute((action as PetStoreAction.ChangePet).pet)
    }

    override fun canHandle(action: Action) = action is PetStoreAction.ChangePet
}

object BuyPetSideEffectHandler : AppSideEffectHandler() {

    private val buyPetUseCase by required { buyPetUseCase }

    override suspend fun doExecute(action: Action, state: AppState) {
        val result = buyPetUseCase.execute((action as PetStoreAction.BuyPet).pet)
        when (result) {
            is BuyPetUseCase.Result.PetBought -> {
                dispatch(PetStoreAction.PetBought)
            }
            BuyPetUseCase.Result.TooExpensive -> {
                dispatch(PetStoreAction.PetTooExpensive)
            }
        }
    }

    override fun canHandle(action: Action) = action is PetStoreAction.BuyPet
}
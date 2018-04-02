package io.ipoli.android.store.powerup.middleware

import io.ipoli.android.challenge.list.ChallengeListAction
import io.ipoli.android.common.AppState
import io.ipoli.android.common.redux.Action
import io.ipoli.android.common.redux.Dispatcher
import io.ipoli.android.common.redux.MiddleWare
import io.ipoli.android.note.NoteAction
import io.ipoli.android.player.Inventory
import io.ipoli.android.quest.schedule.addquest.AddQuestAction
import io.ipoli.android.quest.timer.QuestAction
import io.ipoli.android.repeatingquest.edit.EditRepeatingQuestAction
import io.ipoli.android.store.powerup.PowerUp

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 03/20/2018.
 */
data class ShowBuyPowerUpAction(val powerUp: PowerUp.Type) : Action

class CheckEnabledPowerUpMiddleWare : MiddleWare<AppState> {

    override fun execute(
        state: AppState,
        dispatcher: Dispatcher,
        action: Action
    ): MiddleWare.Result {
        val p = state.dataState.player ?: return MiddleWare.Result.Continue

        val inventory = p.inventory

        return when (action) {
            ChallengeListAction.AddChallenge ->
                checkForAvailablePowerUp(PowerUp.Type.CHALLENGES, inventory, dispatcher)

            QuestAction.Start ->
                checkForAvailablePowerUp(PowerUp.Type.TIMER, inventory, dispatcher)

            AddQuestAction.AddSubQuest,
            is QuestAction.AddSubQuest,
            is EditRepeatingQuestAction.AddSubQuest ->
                checkForAvailablePowerUp(PowerUp.Type.SUB_QUESTS, inventory, dispatcher)

            is NoteAction.Save ->
                checkForAvailablePowerUp(PowerUp.Type.NOTES, inventory, dispatcher)

            else -> MiddleWare.Result.Continue
        }
    }

    private fun checkForAvailablePowerUp(
        powerUp: PowerUp.Type,
        inventory: Inventory,
        dispatcher: Dispatcher
    ) =
        when {
            inventory.isPowerUpEnabled(powerUp) -> MiddleWare.Result.Continue
            else -> {
                dispatcher.dispatch(ShowBuyPowerUpAction(powerUp))
                MiddleWare.Result.Stop
            }
        }

}
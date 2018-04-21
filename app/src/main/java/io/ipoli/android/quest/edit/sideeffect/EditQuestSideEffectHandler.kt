package io.ipoli.android.quest.edit.sideeffect

import io.ipoli.android.Constants
import io.ipoli.android.common.AppSideEffectHandler
import io.ipoli.android.common.AppState
import io.ipoli.android.common.redux.Action
import io.ipoli.android.quest.Color
import io.ipoli.android.quest.Reminder
import io.ipoli.android.quest.edit.EditQuestAction
import io.ipoli.android.quest.edit.EditQuestViewState
import io.ipoli.android.quest.schedule.addquest.AddQuestAction
import io.ipoli.android.quest.schedule.addquest.AddQuestViewState
import io.ipoli.android.quest.subquest.SubQuest
import io.ipoli.android.quest.usecase.Result
import io.ipoli.android.quest.usecase.SaveQuestUseCase
import space.traversal.kapsule.required

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 4/10/18.
 */
class EditQuestSideEffectHandler : AppSideEffectHandler() {

    private val questRepository by required { questRepository }
    private val saveQuestUseCase by required { saveQuestUseCase }

    override suspend fun doExecute(action: Action, state: AppState) {
        when (action) {
            is EditQuestAction.Load -> {
                val quest = questRepository.findById(action.questId)
                dispatch(EditQuestAction.Loaded(quest!!, action.params))
            }

            is AddQuestAction.Save -> {
                val addQuestState = state.stateFor(AddQuestViewState::class.java)
                val scheduledDate = addQuestState.date
                val reminder = addQuestState.time?.let {
                    Reminder("", it, scheduledDate)
                }
                val questParams = SaveQuestUseCase.Parameters(
                    name = action.name,
                    tags = emptyList(),
                    subQuests = emptyList(),
                    color = addQuestState.color ?: Color.GREEN,
                    icon = addQuestState.icon,
                    scheduledDate = scheduledDate,
                    startTime = addQuestState.time,
                    duration = addQuestState.duration ?: Constants.QUEST_MIN_DURATION,
                    reminders = reminder?.let { listOf(it) }
                )

                val result = saveQuestUseCase.execute(questParams)

                when (result) {
                    is Result.Invalid -> {
                        dispatch(AddQuestAction.SaveInvalidQuest(result.error))
                    }
                    else -> dispatch(AddQuestAction.QuestSaved)
                }
            }

            is EditQuestAction.Save -> {
                val s = state.stateFor(EditQuestViewState::class.java)

                val reminder = s.startTime?.let {
                    s.scheduleDate?.let {
                        Reminder.create(s.reminder, s.scheduleDate, s.startTime)
                    }

                }

                val subQuests = action.newSubQuestNames.entries.map {
                    var subQuest = SubQuest(it.value, null, null)
                    if (s.subQuests.containsKey(it.key)) {
                        val sq = s.subQuests[it.key]!!
                        subQuest = subQuest.copy(
                            completedAtDate = sq.completedAtDate,
                            completedAtTime = sq.completedAtTime
                        )
                    }
                    subQuest
                }

                val questParams = SaveQuestUseCase.Parameters(
                    id = s.id,
                    name = s.name,
                    subQuests = subQuests,
                    color = s.color,
                    icon = s.icon,
                    scheduledDate = s.scheduleDate,
                    startTime = s.startTime,
                    duration = s.duration,
                    reminders = reminder?.let { listOf(it) },
                    challengeId = s.challenge?.id,
                    repeatingQuestId = s.repeatingQuestId,
                    note = s.note,
                    tags = s.questTags
                )

                saveQuestUseCase.execute(questParams)

            }
        }
    }

    override fun canHandle(action: Action) =
        action is EditQuestAction
                || action is AddQuestAction

}
package io.ipoli.android.tag.show

import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.DataLoadedAction
import io.ipoli.android.common.mvi.BaseViewState
import io.ipoli.android.common.redux.Action
import io.ipoli.android.quest.Color
import io.ipoli.android.quest.Icon
import io.ipoli.android.tag.Tag
import io.ipoli.android.tag.usecase.CreateTagItemsUseCase

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 04/04/2018.
 */
sealed class TagAction : Action {

    object TagCountLimitReached : TagAction()

    data class Load(val tagId: String) : TagAction()
    data class Unfavorite(val tagId: String) : TagAction()
    data class Favorite(val tagId: String) : TagAction()
    data class Remove(val tagId: String) : TagAction()
    data class CompleteQuest(val questId: String) : TagAction()
    data class UndoCompleteQuest(val questId: String) : TagAction()
}

object TagReducer : BaseViewStateReducer<TagViewState>() {

    override fun reduce(state: AppState, subState: TagViewState, action: Action) =
        when (action) {

            is TagAction.Load -> {
                val tag = state.dataState.tags.firstOrNull { it.id == action.tagId }
                tag?.let {
                    createChangedState(it, subState)
                } ?: subState.copy(type = TagViewState.StateType.LOADING)
            }

            is DataLoadedAction.TagsChanged -> {
                val tag = action.tags.firstOrNull { it.id == subState.id }
                when {
                    tag != null -> createChangedState(tag, subState)
                    subState.type == TagViewState.StateType.LOADING -> subState
                    else -> subState.copy(type = TagViewState.StateType.REMOVED)
                }
            }

            is DataLoadedAction.TagItemsChanged -> {

                val quests = action.items
                    .filterIsInstance<CreateTagItemsUseCase.TagItem.QuestItem>()
                    .map { it.quest }

                val completedCount = quests.count { it.completedAtDate != null }

                subState.copy(
                    type = TagViewState.StateType.TAG_ITEMS_CHANGED,
                    items = action.items,
                    questCount = quests.size,
                    progressPercent = ((completedCount.toFloat() / quests.size) * 100).toInt()
                )
            }

            else -> subState
        }

    private fun createChangedState(tag: Tag, state: TagViewState) =
        state.copy(
            type = TagViewState.StateType.TAG_CHANGED,
            id = tag.id,
            name = tag.name,
            color = tag.color,
            icon = tag.icon,
            isFavorite = tag.isFavorite
        )

    override fun defaultState() = TagViewState(
        type = TagViewState.StateType.LOADING,
        id = "",
        name = "",
        color = Color.PINK,
        icon = null,
        isFavorite = false,
        items = emptyList(),
        questCount = -1,
        progressPercent = -1
    )

    override val stateKey = key<TagViewState>()
}

data class TagViewState(
    val type: StateType,
    val id: String,
    val name: String,
    val color: Color,
    val icon: Icon?,
    val isFavorite: Boolean,
    val items: List<CreateTagItemsUseCase.TagItem>,
    val questCount: Int,
    val progressPercent: Int
) : BaseViewState() {

    enum class StateType {
        LOADING, TAG_CHANGED, TAG_ITEMS_CHANGED, REMOVED
    }
}
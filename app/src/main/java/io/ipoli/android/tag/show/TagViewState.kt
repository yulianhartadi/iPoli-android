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
                    createChangedState(it)
                } ?: TagViewState.Loading(action.tagId)
            }

            is DataLoadedAction.TagsChanged -> {
                val tag = action.tags.firstOrNull { it.id == subState.id }
                when {
                    tag != null -> createChangedState(tag)
                    subState is TagViewState.Loading -> subState
                    else -> TagViewState.Removed(subState.id)
                }
            }

            is DataLoadedAction.TagItemsChanged -> {

                val quests = action.items
                    .filterIsInstance<CreateTagItemsUseCase.TagItem.QuestItem>()
                    .map { it.quest }

                val completedCount = quests.count { it.completedAtDate != null }

                TagViewState.TagItemsChanged(
                    id = action.tagId,
                    items = action.items,
                    questCount = quests.size,
                    progressPercent = ((completedCount.toFloat() / quests.size) * 100).toInt()
                )
            }

            else -> subState
        }

    private fun createChangedState(tag: Tag) =
        TagViewState.TagChanged(
            id = tag.id,
            name = tag.name,
            color = tag.color,
            icon = tag.icon,
            isFavorite = tag.isFavorite
        )

    override fun defaultState() = TagViewState.Loading("")

    override val stateKey = key<TagViewState>()
}

sealed class TagViewState(open val id: String) : BaseViewState() {

    data class Loading(override val id: String) : TagViewState(id)

    data class TagChanged(
        override val id: String,
        val name: String,
        val color: Color,
        val icon: Icon?,
        val isFavorite: Boolean
    ) : TagViewState(id)

    data class TagItemsChanged(
        override val id: String,
        val items: List<CreateTagItemsUseCase.TagItem>,
        val questCount: Int,
        val progressPercent: Int
    ) : TagViewState(id)

    data class Removed(override val id: String) : TagViewState(id)
}
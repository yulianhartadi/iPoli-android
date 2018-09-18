package io.ipoli.android.player.attribute

import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.DataLoadedAction
import io.ipoli.android.common.redux.Action
import io.ipoli.android.common.redux.BaseViewState
import io.ipoli.android.player.attribute.AttributeListViewState.StateType.*
import io.ipoli.android.player.data.Player
import io.ipoli.android.tag.Tag

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 9/13/18.
 */
sealed class AttributeListAction : Action {
    data class Load(val attribute: Player.AttributeType?) : AttributeListAction()
    data class AddTag(val attributeType: Player.AttributeType, val tag: Tag) : AttributeListAction()
    data class RemoveTag(val attributeType: Player.AttributeType, val tag: Tag) : AttributeListAction()
}


object AttributeListReducer : BaseViewStateReducer<AttributeListViewState>() {
    override val stateKey = key<AttributeListViewState>()

    override fun reduce(
        state: AppState,
        subState: AttributeListViewState,
        action: Action
    ): AttributeListViewState {
        return when(action) {
            is AttributeListAction.Load -> {
                val player = state.dataState.player
                if (player == null)
                    subState.copy(
                        type = LOADING
                    )
                else {
                    val attributes = Player.AttributeType.values().map {
                        player.attributes[it]!!
                    }
                    subState.copy(
                        type = DATA_LOADED,
                        attributes = attributes,
                        firstSelectedIndex = if (action.attribute == null) 0
                        else attributes.map { it.type }.indexOfFirst { it == action.attribute },
                        tags = state.dataState.tags,
                        rank = player.rank
                    )
                }
            }

            is DataLoadedAction.PlayerChanged -> {
                val attributes = Player.AttributeType.values().map {
                    action.player.attributes[it]!!
                }
                subState.copy(
                    type = DATA_CHANGED,
                    attributes = attributes,
                    firstSelectedIndex = null,
                    tags = state.dataState.tags,
                    rank = action.player.rank
                )
            }

            is DataLoadedAction.TagsChanged ->
                subState.copy(
                    type = DATA_CHANGED,
                    tags = action.tags
                )

            else ->
                subState
        }
    }

    override fun defaultState() = AttributeListViewState(
        type = LOADING,
        attributes = null,
        rank = null,
        firstSelectedIndex = null,
        tags = null
    )
}

data class AttributeListViewState(
    val type : StateType,
    val attributes: List<Player.Attribute>?,
    val rank : Player.Rank?,
    val firstSelectedIndex: Int?,
    val tags: List<Tag>?
) : BaseViewState() {
    enum class StateType {
        LOADING,
        DATA_LOADED,
        DATA_CHANGED
    }
}
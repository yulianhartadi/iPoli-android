package io.ipoli.android.tag.edit

import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.Validator
import io.ipoli.android.common.mvi.ViewState
import io.ipoli.android.common.redux.Action
import io.ipoli.android.quest.Color
import io.ipoli.android.quest.Icon
import io.ipoli.android.tag.edit.EditTagViewState.StateType.*
import io.ipoli.android.tag.list.TagListReducer

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 4/4/18.
 */
sealed class EditTagAction : Action {
    object Save : EditTagAction()
    data class Load(val tagId: String?) : EditTagAction()
    data class ChangeIcon(val icon: Icon?) : EditTagAction()
    data class ChangeColor(val color: Color) : EditTagAction()
    data class Validate(val name: String) : EditTagAction()
}

object EditTagReducer : BaseViewStateReducer<EditTagViewState>() {

    override fun reduce(
        state: AppState,
        subState: EditTagViewState,
        action: Action
    ) = when (action) {

        is EditTagAction.Load -> {
            val tag = state.dataState.tags.find { it.id == action.tagId }
            subState.copy(
                type = DATA_LOADED,
                id = tag?.id,
                name = tag?.name ?: "",
                color = tag?.color ?: subState.color,
                icon = tag?.icon,
                isFavorite = tag?.isFavorite ?: false
            )
        }

        is EditTagAction.ChangeIcon -> {
            subState.copy(
                type = ICON_CHANGED,
                icon = action.icon
            )
        }

        is EditTagAction.ChangeColor -> {
            subState.copy(
                type = COLOR_CHANGED,
                color = action.color
            )
        }

        is EditTagAction.Validate -> {
            val errors = Validator.validate(action).check<EditTagReducer.ValidationError> {
                "name" {
                    given { name.isEmpty() } addError ValidationError.EMPTY_NAME
                    given {
                        if (subState.id.isNullOrEmpty()) {
                            state.dataState.tags.any { it.name == name }
                        } else false
                    } addError ValidationError.EXISTING_NAME
                }
            }
            subState.copy(
                type = if (errors.isEmpty()) {
                    VALIDATION_SUCCESSFUL
                } else if (errors.contains(ValidationError.EMPTY_NAME)) {
                    VALIDATION_ERROR_EMPTY_NAME
                } else {
                    VALIDATION_ERROR_EXISTING_NAME
                },
                name = action.name
            )
        }

        else -> subState
    }

    override fun defaultState() = EditTagViewState(
        type = LOADING,
        id = null,
        name = "",
        color = Color.GREEN,
        icon = null,
        isFavorite = false
    )

    override val stateKey = TagListReducer.key<EditTagViewState>()

    enum class ValidationError {
        EMPTY_NAME, EXISTING_NAME
    }

}

data class EditTagViewState(
    val type: EditTagViewState.StateType,
    val id: String?,
    val name: String,
    val color: Color,
    val icon: Icon?,
    val isFavorite: Boolean
) : ViewState {

    enum class StateType {
        LOADING,
        DATA_LOADED,
        COLOR_CHANGED,
        ICON_CHANGED,
        VALIDATION_SUCCESSFUL,
        VALIDATION_ERROR_EMPTY_NAME,
        VALIDATION_ERROR_EXISTING_NAME
    }
}
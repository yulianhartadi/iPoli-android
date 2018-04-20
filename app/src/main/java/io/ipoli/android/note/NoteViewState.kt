package io.ipoli.android.note

import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.mvi.ViewState
import io.ipoli.android.common.redux.Action
import io.ipoli.android.note.NoteViewState.Type.*
import io.ipoli.android.quest.show.QuestAction

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 03/26/2018.
 */
sealed class NoteAction : Action {
    data class Load(val note: String, val startEdit: Boolean, val isClosable: Boolean) :
        NoteAction()
    data class Save(val text: String) : NoteAction()
    data class Update(val note: String) : NoteAction()
    data class Preview(val text: String) : NoteAction()

    object Edit : NoteAction()
}

object NoteReducer : BaseViewStateReducer<NoteViewState>() {

    override fun reduce(state: AppState, subState: NoteViewState, action: Action) =
        when (action) {
            is NoteAction.Load -> {
                subState.copy(
                    type = if (action.startEdit) EDIT else VIEW,
                    text = action.note,
                    isClosable = action.isClosable,
                    showEmpty = !action.startEdit && action.note.isEmpty(),
                    showText = !action.startEdit && action.note.isNotEmpty(),
                    isPreview = false
                )
            }

            is NoteAction.Preview -> {
                subState.copy(
                    type = VIEW,
                    text = action.text,
                    isPreview = true,
                    showEmpty = action.text.isEmpty(),
                    showText = action.text.isNotEmpty()
                )
            }

            is NoteAction.Save ->
                subState.copy(
                    type = SAVED,
                    text = action.text,
                    isPreview = false,
                    showEmpty = action.text.isEmpty(),
                    showText = action.text.isNotEmpty()
                )

            NoteAction.Edit ->
                subState.copy(
                    type = EDIT,
                    isPreview = false,
                    showEmpty = false,
                    showText = false
                )

            is QuestAction.UpdateNote -> {
                subState.copy(
                    type = CHANGED,
                    text = action.note,
                    showEmpty = action.note.isEmpty(),
                    showText = action.note.isNotEmpty()
                )
            }

            else -> subState
        }

    override fun defaultState() = NoteViewState(
        type = LOADING,
        text = "",
        isClosable = false,
        isPreview = false,
        showEmpty = false,
        showText = false
    )

    override val stateKey = key<NoteViewState>()

}

data class NoteViewState(
    val type: Type,
    val text: String,
    val isPreview: Boolean,
    val showEmpty: Boolean,
    val showText: Boolean,
    val isClosable: Boolean
) : ViewState {
    enum class Type {
        LOADING,
        VIEW,
        EDIT,
        SAVED,
        CHANGED
    }
}
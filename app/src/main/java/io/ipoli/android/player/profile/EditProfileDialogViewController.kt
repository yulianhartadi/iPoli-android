package io.ipoli.android.player.profile

import android.annotation.SuppressLint
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import io.ipoli.android.Constants
import io.ipoli.android.R
import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.DataLoadedAction
import io.ipoli.android.common.redux.Action
import io.ipoli.android.common.redux.BaseViewState
import io.ipoli.android.common.view.ReduxDialogController
import io.ipoli.android.common.view.colorRes
import io.ipoli.android.player.data.AndroidAvatar
import io.ipoli.android.player.data.Avatar
import io.ipoli.android.player.data.Player
import io.ipoli.android.player.profile.EditProfileViewState.StateType.DATA_CHANGED
import io.ipoli.android.player.profile.EditProfileViewState.StateType.LOADING
import kotlinx.android.synthetic.main.dialog_edit_profile.*
import kotlinx.android.synthetic.main.dialog_edit_profile.view.*

sealed class EditProfileAction : Action {
    class Save(val displayName: String, val bio: String) : EditProfileAction()

    object Load : EditProfileAction()
}

object EditProfileReducer : BaseViewStateReducer<EditProfileViewState>() {
    override val stateKey = key<EditProfileViewState>()

    override fun reduce(
        state: AppState,
        subState: EditProfileViewState,
        action: Action
    ) = when (action) {
        is EditProfileAction.Load -> {
            val player = state.dataState.player
            if (player == null)
                subState.copy(type = LOADING)
            else
                createChangedState(subState, player)
        }

        is DataLoadedAction.PlayerChanged ->
            createChangedState(subState, action.player)

        else -> subState
    }

    private fun createChangedState(
        subState: EditProfileViewState,
        player: Player
    ) =
        subState.copy(
            type = DATA_CHANGED,
            displayName = player.displayName,
            bio = player.bio,
            avatar = player.avatar
        )

    override fun defaultState() = EditProfileViewState(
        type = LOADING,
        displayName = null,
        bio = null,
        avatar = null
    )

}

data class EditProfileViewState(
    val type: StateType,
    val displayName: String?,
    val bio: String?,
    val avatar: Avatar?
) : BaseViewState() {
    enum class StateType {
        LOADING,
        DATA_CHANGED
    }
}


class EditProfileDialogController(args: Bundle? = null) :
    ReduxDialogController<EditProfileAction, EditProfileViewState, EditProfileReducer>(args) {

    override val reducer = EditProfileReducer


    private val displayNameWatcher: TextWatcher = object : TextWatcher {

        override fun afterTextChanged(s: Editable) {
            renderDisplayNameLengthHint(s.length)
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

        }
    }

    private val bioWatcher: TextWatcher = object : TextWatcher {

        override fun afterTextChanged(s: Editable) {
            renderBioLengthHint(s.length)
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

        }

    }

    override fun onCreateContentView(inflater: LayoutInflater, savedViewState: Bundle?): View {
        return inflater.inflate(R.layout.dialog_edit_profile, null)
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        dialog.window.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.profileClose.setOnClickListener {
            dismiss()
        }
        dialog.profileDisplayName.addTextChangedListener(displayNameWatcher)
        dialog.profileBio.addTextChangedListener(bioWatcher)
    }

    override fun onDetach(view: View) {
        dialog.profileDisplayName.removeTextChangedListener(displayNameWatcher)
        dialog.profileBio.removeTextChangedListener(bioWatcher)
        super.onDetach(view)
    }

    override fun onCreateLoadAction(): EditProfileAction? {
        return EditProfileAction.Load
    }

    override fun onCreateDialog(
        dialogBuilder: AlertDialog.Builder,
        contentView: View,
        savedViewState: Bundle?
    ): AlertDialog =
        dialogBuilder
            .setCustomTitle(null)
            .create()

    private fun renderDisplayNameLengthHint(length: Int) {
        @SuppressLint("SetTextI18n")
        dialog.displayNameLengthHint.text = "$length/${Constants.DISPLAY_NAME_MAX_LENGTH}"
    }

    private fun renderBioLengthHint(length: Int) {
        @SuppressLint("SetTextI18n")
        dialog.bioLengthHint.text = "$length/${Constants.BIO_MAX_LENGTH}"
    }

    override fun render(state: EditProfileViewState, view: View) {
        when (state.type) {
            DATA_CHANGED -> {
                Glide.with(view.context).load(state.avatarImage)
                    .apply(RequestOptions.circleCropTransform())
                    .into(view.profileAvatar)
                val background = view.profileAvatar.background as GradientDrawable
                background.setColor(colorRes(AndroidAvatar.valueOf(state.avatar!!.name).backgroundColor))

                view.profileDisplayName.setText(state.displayNameText)
                view.profileDisplayName.setSelection(state.displayNameText.length)

                view.profileBio.setText(state.bioText)
                view.profileBio.setSelection(state.bioText.length)

                view.profileSave.onDebounceClick {
                    dispatch(
                        EditProfileAction.Save(
                            view.profileDisplayName.text.toString(),
                            view.profileBio.text.toString()
                        )
                    )
                    dismiss()
                }
            }

            else -> {
            }
        }
    }

    private val EditProfileViewState.avatarImage: Int
        get() = AndroidAvatar.valueOf(avatar!!.name).image

    private val EditProfileViewState.displayNameText: String
        get() = displayName ?: ""

    private val EditProfileViewState.bioText: String
        get() = bio ?: ""

}

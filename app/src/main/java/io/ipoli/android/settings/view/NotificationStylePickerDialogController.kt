package io.ipoli.android.settings.view

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import io.ipoli.android.R
import io.ipoli.android.common.view.ReduxDialogController
import io.ipoli.android.common.view.stringRes
import io.ipoli.android.pet.AndroidPetAvatar
import io.ipoli.android.pet.LoadPetDialogAction
import io.ipoli.android.pet.PetDialogReducer
import io.ipoli.android.pet.PetDialogViewState
import io.ipoli.android.player.data.Player.Preferences
import kotlinx.android.synthetic.main.view_dialog_header.view.*

class NotificationStylePickerDialogController(args: Bundle? = null) :
    ReduxDialogController<LoadPetDialogAction, PetDialogViewState, PetDialogReducer>(args) {

    override val reducer = PetDialogReducer

    private var selectedNotificationStyle: Preferences.NotificationStyle =
        Preferences.NotificationStyle.ALL

    private var listener: (Preferences.NotificationStyle) -> Unit = {}

    constructor(
        notificationStyle: Preferences.NotificationStyle,
        listener: (Preferences.NotificationStyle) -> Unit
    ) : this() {
        this.selectedNotificationStyle = notificationStyle
        this.listener = listener
    }

    @SuppressLint("InflateParams")
    override fun onCreateContentView(inflater: LayoutInflater, savedViewState: Bundle?): View =
        inflater.inflate(R.layout.dialog_empty_container, null)

    override fun onHeaderViewCreated(headerView: View) {
        headerView.dialogHeaderTitle.setText(R.string.select_notification_style)
    }

    override fun onCreateDialog(
        dialogBuilder: AlertDialog.Builder,
        contentView: View,
        savedViewState: Bundle?
    ): AlertDialog =
        dialogBuilder
            .setSingleChoiceItems(
                listOf(
                    stringRes(R.string.notification_style_notification),
                    stringRes(R.string.notification_style_popup),
                    stringRes(R.string.notification_style_all)
                ).toTypedArray(),
                selectedNotificationStyle.ordinal
            ) { _, which ->
                selectedNotificationStyle =
                    when (which) {
                        0 -> Preferences.NotificationStyle.NOTIFICATION
                        1 -> Preferences.NotificationStyle.POPUP
                        else -> Preferences.NotificationStyle.ALL
                    }
            }
            .setPositiveButton(R.string.dialog_ok, null)
            .setNegativeButton(R.string.cancel, null)
            .create()

    override fun onCreateLoadAction() = LoadPetDialogAction

    override fun onDialogCreated(dialog: AlertDialog, contentView: View) {
        dialog.setOnShowListener {
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener { _ ->
                listener(selectedNotificationStyle)
                dismiss()
            }
        }
    }

    override fun render(state: PetDialogViewState, view: View) {
        if (state.type == PetDialogViewState.Type.PET_LOADED) {
            changeIcon(AndroidPetAvatar.valueOf(state.petAvatar!!.name).headImage)
        }
    }
}
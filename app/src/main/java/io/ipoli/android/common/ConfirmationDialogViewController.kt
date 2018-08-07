package io.ipoli.android.common

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import io.ipoli.android.R
import io.ipoli.android.common.view.ReduxDialogController
import io.ipoli.android.pet.AndroidPetAvatar
import io.ipoli.android.pet.LoadPetDialogAction
import io.ipoli.android.pet.PetDialogReducer
import io.ipoli.android.pet.PetDialogViewState
import kotlinx.android.synthetic.main.view_dialog_header.view.*

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 8/6/18.
 */
class ConfirmationDialogViewController :
    ReduxDialogController<LoadPetDialogAction, PetDialogViewState, PetDialogReducer> {

    override val reducer = PetDialogReducer

    private var listener: () -> Unit = {}

    private var title: String = ""
    private var message: String = ""

    constructor(
        title: String,
        message: String,
        listener: () -> Unit
    ) : this() {
        this.listener = listener
        this.title = title
        this.message = message
    }

    constructor(args: Bundle? = null) : super(args)

    override fun onHeaderViewCreated(headerView: View) {
        headerView.dialogHeaderTitle.text = title
    }

    override fun onCreateContentView(inflater: LayoutInflater, savedViewState: Bundle?): View {
        val view = inflater.inflate(R.layout.dialog_confirmation, null)
        (view as TextView).text = message
        return view
    }

    override fun onCreateDialog(
        dialogBuilder: AlertDialog.Builder,
        contentView: View,
        savedViewState: Bundle?
    ): AlertDialog =
        dialogBuilder
            .setPositiveButton(R.string.dialog_yes, null)
            .setNegativeButton(R.string.cancel, null)
            .create()

    override fun onDialogCreated(dialog: AlertDialog, contentView: View) {
        dialog.setOnShowListener { _ ->
            setPositiveButtonListener {
                dismiss()
                listener()
            }
        }
    }

    override fun onCreateLoadAction() = LoadPetDialogAction

    override fun render(state: PetDialogViewState, view: View) {
        if (state.type == PetDialogViewState.Type.PET_LOADED) {
            changeIcon(AndroidPetAvatar.valueOf(state.petAvatar!!.name).headImage)
        }
    }
}
package mypoli.android.common.view

import android.content.DialogInterface
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import mypoli.android.R
import mypoli.android.pet.AndroidPetAvatar
import mypoli.android.pet.LoadPetIntent
import mypoli.android.pet.PetDialogPresenter
import mypoli.android.pet.PetDialogViewState
import kotlinx.android.synthetic.main.dialog_text_picker.view.*
import kotlinx.android.synthetic.main.view_dialog_header.view.*
import space.traversal.kapsule.required

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 12/3/17.
 */
class TextPickerDialogController :
    MviDialogController<PetDialogViewState, TextPickerDialogController, PetDialogPresenter, LoadPetIntent> {

    private val presenter by required { petDialogPresenter }

    override fun createPresenter() = presenter

    private var listener: (String) -> Unit = {}

    private var title: String = ""
    private var text: String = ""
    private var hint: String = ""

    constructor(listener: (String) -> Unit, title: String, text: String = "", hint: String = "") : this() {
        this.listener = listener
        this.title = title
        this.text = text
        this.hint = hint
    }

    constructor(args: Bundle? = null) : super(args)

    override fun onHeaderViewCreated(headerView: View) {
        headerView.dialogHeaderTitle.text = title
    }

    override fun onCreateContentView(inflater: LayoutInflater, savedViewState: Bundle?): View {
        val contentView = inflater.inflate(R.layout.dialog_text_picker, null)

        val textView = contentView.text
        textView.setText(text)
        textView.setSelection(textView.text.length)

        contentView.textLayout.hint = hint
        return contentView
    }

    override fun onCreateDialog(dialogBuilder: AlertDialog.Builder, contentView: View, savedViewState: Bundle?): AlertDialog =
        dialogBuilder
            .setPositiveButton(R.string.dialog_ok, null)
            .setNegativeButton(R.string.cancel, null)
            .create()

    override fun onDialogCreated(dialog: AlertDialog, contentView: View) {
        dialog.setOnShowListener {
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                val textView = contentView.text
                val text = textView.text.toString()
                if (text.isNotEmpty()) {
                    listener(text)
                    dialog.dismiss()
                } else {
                    textView.error = stringRes(R.string.text_picker_empty_error)
                }
            }
        }
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        send(LoadPetIntent)
    }

    override fun render(state: PetDialogViewState, view: View) {
        if (state.type == PetDialogViewState.Type.PET_LOADED) {
            changeIcon(AndroidPetAvatar.valueOf(state.petAvatar!!.name).headImage)
        }
    }
}

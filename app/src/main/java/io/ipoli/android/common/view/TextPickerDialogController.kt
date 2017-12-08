package io.ipoli.android.common.view

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import io.ipoli.android.R
import io.ipoli.android.common.di.ControllerModule
import io.ipoli.android.common.mvi.ViewStateRenderer
import io.ipoli.android.pet.AndroidPetAvatar
import io.ipoli.android.pet.LoadPetIntent
import io.ipoli.android.pet.PetDialogPresenter
import io.ipoli.android.pet.PetDialogViewState
import kotlinx.android.synthetic.main.dialog_text_picker.view.*
import space.traversal.kapsule.Injects
import space.traversal.kapsule.required

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 12/3/17.
 */
class TextPickerDialogController : MviDialogController<PetDialogViewState, ColorPickerDialogController, PetDialogPresenter, LoadPetIntent>
    , ViewStateRenderer<PetDialogViewState>, Injects<ControllerModule> {

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

    override fun onCreateDialog(savedViewState: Bundle?): DialogView {
        val inflater = LayoutInflater.from(activity!!)

        val contentView = inflater.inflate(R.layout.dialog_text_picker, null)

        val textView = contentView.text
        textView.setText(text)
        textView.setSelection(textView.text.length)

        contentView.textLayout.hint = hint

        val dialog = AlertDialog.Builder(activity!!)
            .setView(contentView)
            .setTitle(title)
            .setIcon(R.drawable.pet_5_head)
            .setPositiveButton(R.string.dialog_ok, { _, _ ->
                listener(textView.text.toString())
            })
            .setNegativeButton(R.string.cancel, null)
            .create()

        return DialogView(dialog, contentView)
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

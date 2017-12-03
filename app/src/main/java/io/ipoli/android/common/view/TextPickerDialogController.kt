package io.ipoli.android.common.view

import android.app.Dialog
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import io.ipoli.android.R
import kotlinx.android.synthetic.main.dialog_text_picker.view.*

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 12/3/17.
 */
class TextPickerDialogController : BaseDialogController {

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


    override fun onCreateDialog(savedViewState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(activity!!)

        val contentView = inflater.inflate(R.layout.dialog_text_picker, null)

        val textView = contentView.text
        textView.setText(text)
        textView.setSelection(textView.text.length)

        contentView.textLayout.hint = hint

        return AlertDialog.Builder(activity!!)
            .setView(contentView)
            .setTitle(title)
            .setIcon(R.drawable.pet_5_head)
            .setPositiveButton(R.string.dialog_ok, { _, _ ->
                listener(textView.text.toString())
            })
            .setNegativeButton(R.string.cancel, null)
            .create()
    }

}
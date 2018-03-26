package io.ipoli.android.common

import android.os.Bundle
import android.support.annotation.StringRes
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import io.ipoli.android.R
import io.ipoli.android.common.view.BaseDialogController
import kotlinx.android.synthetic.main.dialog_loader.view.*

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 9/2/17.
 */

class LoaderDialogController : BaseDialogController {

    @StringRes
    private var title: Int? = null
    private var message: Int? = null

    constructor(args: Bundle? = null) : super(args)

    constructor(@StringRes title: Int, @StringRes message: Int) : this() {
        this.title = title
        this.message = message
    }

    override fun onCreateDialog(
        dialogBuilder: AlertDialog.Builder,
        contentView: View,
        savedViewState: Bundle?
    ): AlertDialog {
        val dialog = dialogBuilder
            .setTitle(title ?: R.string.please_wait)
            .setIcon(R.drawable.logo)
            .create()
        dialog.setCancelable(false)
        return dialog
    }

    override fun createHeaderView(inflater: LayoutInflater): View? = null

    override fun onCreateContentView(inflater: LayoutInflater, savedViewState: Bundle?): View {
        val view = inflater.inflate(R.layout.dialog_loader, null)
        view.loaderMessage.setText(message ?: R.string.loading)
        return view
    }

}
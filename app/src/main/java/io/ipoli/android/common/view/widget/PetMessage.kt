package io.ipoli.android.common.view.widget

import android.support.annotation.DrawableRes
import android.support.design.widget.Snackbar
import android.view.View
import android.widget.TextView
import io.ipoli.android.R

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 10/13/17.
 */
object PetMessage {

    fun show(view: View, @DrawableRes icon: Int, message: String, action: String, listener: () -> Unit) {
        val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
        val snackbarContainer = snackbar.view as Snackbar.SnackbarLayout
        val text = snackbarContainer.findViewById<TextView>(android.support.design.R.id.snackbar_text)
        text.setCompoundDrawablesWithIntrinsicBounds(view.resources.getDrawable(R.drawable.ic_done_white_24dp), null, null, null);
        text.setCompoundDrawablePadding(16);
        snackbar.setAction(action, {
            listener()
        })

        snackbar.show()
    }
}
package io.ipoli.android.app.ui.dialogs;

import android.app.ProgressDialog;
import android.content.Context;

import io.ipoli.android.app.utils.StringUtils;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/29/17.
 */
public class LoadingDialog {

    private final ProgressDialog dialog;

    private LoadingDialog(Context context, String title, String message) {
        dialog = create(context, title, message);
    }

    private ProgressDialog create(Context context, String title, String message) {
        ProgressDialog dialog = new ProgressDialog(context);
        if (StringUtils.isNotEmpty(title)) {
            dialog.setTitle(title);
        }
        if (StringUtils.isNotEmpty(message)) {
            dialog.setMessage(message);
        }
        dialog.setIndeterminate(true);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        return dialog;
    }

    public void show() {
        dialog.show();
    }

    public void dismiss() {
        dialog.dismiss();
    }

    public static LoadingDialog show(Context context, String title, String message) {
        LoadingDialog loadingDialog = new LoadingDialog(context, title, message);
        loadingDialog.show();
        return loadingDialog;
    }
}

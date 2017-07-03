package io.ipoli.android.app.ui;

import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.View;

import io.ipoli.android.R;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 7/3/17.
 */

public class ThemedSnackbar {
    private static int actionColor = R.color.md_red_A200;

    public static Snackbar make(View view, @StringRes int message, int length) {
        Snackbar snackbar = Snackbar.make(view, message, length);
        snackbar.setActionTextColor(ContextCompat.getColor(view.getContext(), actionColor));
        return snackbar;
    }

    public static Snackbar make(View view, String message, int length) {
        Snackbar snackbar = Snackbar.make(view, message, length);
        snackbar.setActionTextColor(ContextCompat.getColor(view.getContext(), actionColor));
        return snackbar;
    }
}

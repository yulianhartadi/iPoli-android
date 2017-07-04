package io.ipoli.android.app.ui;

import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.RelativeLayout;

public class FabMenuViewBehavior extends CoordinatorLayout.Behavior<RelativeLayout> {

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, RelativeLayout child, View dependency) {
        return dependency instanceof Snackbar.SnackbarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, RelativeLayout child, View dependency) {
        float translationY = Math.min(0, dependency.getTranslationY() - dependency.getHeight());
        child.setTranslationY(translationY);
        return true;
    }
}

package io.ipoli.android.app.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/4/17.
 */
public class LayoutManagerFactory {

    @NonNull
    public static LinearLayoutManager createReverseLayoutManager(Context context) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        return layoutManager;
    }
}

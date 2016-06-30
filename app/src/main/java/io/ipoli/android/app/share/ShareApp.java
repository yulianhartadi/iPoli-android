package io.ipoli.android.app.share;

import android.graphics.drawable.Drawable;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/30/16.
 */
public class ShareApp {
    public String packageName;
    public String name;
    public Drawable icon;

    public ShareApp(String packageName, String name, Drawable icon) {
        this.packageName = packageName;
        this.name = name;
        this.icon = icon;
    }
}
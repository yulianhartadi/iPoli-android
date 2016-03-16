package io.ipoli.android.app.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/**
 * Created by Polina Zhelyazkova <poly_vjk@abv.bg>
 * on 3/15/16.
 */
public class EmailUtils {

    private static final String IPOLI_EMAIL = "hi@ipoli.io";

    public static void send(Context context, String subject, String chooserTitle) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", IPOLI_EMAIL, null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        context.startActivity(Intent.createChooser(emailIntent, chooserTitle));
    }

}

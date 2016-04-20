package io.ipoli.android.app.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import io.ipoli.android.Constants;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/15/16.
 */
public class EmailUtils {

    public static void send(Context context, String subject, String chooserTitle) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", Constants.IPOLI_EMAIL, null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        context.startActivity(Intent.createChooser(emailIntent, chooserTitle));
    }

}

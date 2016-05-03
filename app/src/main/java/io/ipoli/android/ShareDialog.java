package io.ipoli.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.facebook.share.model.ShareLinkContent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/30/16.
 */
public class ShareDialog {

    public static void show(Context context, String text) {

        final Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT, text);

        List<ShareApp> shareApps = new ArrayList<>();
        final List<ResolveInfo> apps = context.getPackageManager().queryIntentActivities(i, 0);
        ResolveInfo facebook = null;
        ResolveInfo twitter = null;
        for (ResolveInfo info : apps) {
            String packageName = info.activityInfo.packageName;
            String name = info.loadLabel(context.getPackageManager()).toString();
            if (packageName.startsWith("com.facebook.katana") || packageName.startsWith("com.twitter.android")) {
                if (name.equals("Facebook")) {
                    facebook = info;
                } else if (name.equals("Tweet")) {
                    twitter = info;
                }
                continue;
            }

            shareApps.add(new ShareApp(packageName, name, info.loadIcon(context.getPackageManager())));
        }

        if (twitter != null) {
            shareApps.add(0, new ShareApp(twitter.activityInfo.packageName, "Twitter", twitter.loadIcon(context.getPackageManager())));
        }
        if (facebook != null) {
            shareApps.add(0, new ShareApp(facebook.activityInfo.packageName, "Facebook", facebook.loadIcon(context.getPackageManager())));

        }


        ShareDialogAdapter adapter = new ShareDialogAdapter(context, shareApps);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Share your achievement with...");
        builder.setAdapter(adapter, (dialog, item) -> {
            ShareApp shareApp = adapter.getItem(item);
            if (shareApp.packageName.startsWith("com.facebook.katana")) {
                Log.d("Share", "facebook");

                ShareLinkContent content = new ShareLinkContent.Builder()
//                        .setContentUrl(Uri.parse("https://play.google.com/store/apps/details?id=io.ipoli.android"))
                        .setContentTitle("Success")
                        .setContentDescription("I run 10km")
                        .build();

                com.facebook.share.widget.ShareDialog sd =new com.facebook.share.widget.ShareDialog((Activity) context);
                sd.show(content, com.facebook.share.widget.ShareDialog.Mode.AUTOMATIC);

//                ShareDialog shareDialog = new ShareDialog(content);
//                shareDialog.show(context, com.facebook.share.widget.ShareDialog.Mode.AUTOMATIC);


            } else {
                Log.d("Share", "other");
                i.setPackage(shareApp.packageName);
                context.startActivity(i);
            }

        });
        AlertDialog alert = builder.create();
        alert.show();
    }


}

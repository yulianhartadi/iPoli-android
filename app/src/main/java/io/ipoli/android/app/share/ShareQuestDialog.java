package io.ipoli.android.app.share;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.List;

import io.ipoli.android.Constants;
import io.ipoli.android.app.events.QuestShareProviderPickedEvent;
import io.ipoli.android.quest.data.Quest;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/30/16.
 */
public class ShareQuestDialog {
    private final static String FACEBOOK_PACKAGE = "com.facebook.katana";
    private final static String TWITTER_PACKAGE = "com.twitter.android";

    public static void show(Context context, Quest quest, Bus eventBus) {
        String title = "I completed my quest";
        String desc = quest.getName();
        String link = "with iPoli " + Constants.SHARE_URL;

        final Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT, "");

        List<ShareApp> shareApps = filterAppsToShareWith(context, i);

        ShareDialogAdapter adapter = new ShareDialogAdapter(context, shareApps);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Share your achievement with...");
        builder.setAdapter(adapter, (dialog, item) -> {
            ShareApp shareApp = adapter.getItem(item);
            eventBus.post(new QuestShareProviderPickedEvent(shareApp.name, quest));

            String packageName = shareApp.packageName;
            if (isFacebook(packageName)) {
                showFBShareDialog((Activity) context, title, desc);
            } else {
                String text = title + ": \"" + desc + "\" " + link;
                if (isTwitter(packageName)) {
                    text += " via " + Constants.TWITTER_USERNAME;
                }

                i.putExtra(Intent.EXTRA_TEXT, text);
                i.setPackage(shareApp.packageName);
                context.startActivity(i);
            }

        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private static void showFBShareDialog(Activity context, String title, String desc) {
        ShareLinkContent content = new ShareLinkContent.Builder()
                .setContentUrl(Uri.parse(Constants.FACEBOOK_APP_LINK))
                .setContentTitle(title)
                .setContentDescription(desc)
                .setImageUrl(Uri.parse(Constants.IPOLI_LOGO_URL))
                .build();

        ShareDialog sd = new ShareDialog(context);
        sd.show(content, ShareDialog.Mode.AUTOMATIC);
    }

    @NonNull
    private static List<ShareApp> filterAppsToShareWith(Context context, Intent i) {
        List<ShareApp> shareApps = new ArrayList<>();
        final List<ResolveInfo> apps = context.getPackageManager().queryIntentActivities(i, 0);
        ResolveInfo facebook = null;
        ResolveInfo twitter = null;
        for (ResolveInfo info : apps) {
            String packageName = info.activityInfo.packageName;
            String name = info.loadLabel(context.getPackageManager()).toString();
            if (isFacebook(packageName) || isTwitter(packageName)) {
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
        return shareApps;
    }

    private static boolean isFacebook(String packageName) {
        return packageName.startsWith(FACEBOOK_PACKAGE);
    }

    private static boolean isTwitter(String packageName) {
        return packageName.startsWith(TWITTER_PACKAGE);
    }


}

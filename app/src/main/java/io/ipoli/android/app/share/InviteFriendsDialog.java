package io.ipoli.android.app.share;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.share.model.AppInviteContent;
import com.facebook.share.widget.AppInviteDialog;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.share.events.InviteFriendsProviderPickedEvent;

import static io.ipoli.android.Constants.FACEBOOK_PACKAGE;
import static io.ipoli.android.Constants.TWITTER_PACKAGE;
import static io.ipoli.android.MainActivity.INVITE_FRIEND_REQUEST_CODE;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/15/17.
 */
public class InviteFriendsDialog extends DialogFragment {

    private static final String TAG = "invite_friends-dialog";

    @Inject
    Bus eventBus;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        App.getAppComponent(getContext()).inject(this);
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final Intent inviteIntent = new Intent(Intent.ACTION_SEND);
        inviteIntent.setType("text/plain");
        inviteIntent.putExtra(Intent.EXTRA_TEXT, "");

        List<ShareApp> inviteApps = filterInviteProviders(getContext(), inviteIntent);

        ShareDialogAdapter adapter = new ShareDialogAdapter(getContext(), inviteApps);

        LayoutInflater inflater = LayoutInflater.from(getContext());

        View headerView = inflater.inflate(R.layout.fancy_dialog_header, null);

        TextView title = (TextView) headerView.findViewById(R.id.fancy_dialog_title);
        title.setText(R.string.invite_title);

        ImageView image = (ImageView) headerView.findViewById(R.id.fancy_dialog_image);
        image.setImageResource(R.drawable.ic_invite_white_24dp);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCustomTitle(headerView);
        builder.setAdapter(adapter, (dialog, item) -> {
            onInviteProviderClicked(inviteIntent, adapter, item);
        });
        return builder.create();
    }

    protected void onInviteProviderClicked(Intent inviteIntent, ShareDialogAdapter adapter, int item) {
        String message = getString(R.string.invite_message);
        ShareApp shareApp = adapter.getItem(item);

        String packageName = shareApp.packageName;
        eventBus.post(new InviteFriendsProviderPickedEvent(packageName != null ? shareApp.name : "Firebase"));
        if (packageName == null) {
            onInviteWithFirebase(message);
        } else if (isFacebook(packageName)) {
            onInviteWithFacebook();
        } else {
            String text = message + " " + Constants.SHARE_URL;
            if (isTwitter(packageName)) {
                text += " via " + Constants.TWITTER_USERNAME;
            }

            inviteIntent.putExtra(Intent.EXTRA_TEXT, text);
            inviteIntent.setPackage(shareApp.packageName);
            getActivity().startActivity(inviteIntent);
        }
    }

    @NonNull
    private List<ShareApp> filterInviteProviders(Context context, Intent inviteIntent) {
        List<ShareApp> shareApps = new ArrayList<>();
        final List<ResolveInfo> apps = context.getPackageManager().queryIntentActivities(inviteIntent, 0);
        ResolveInfo twitter = null;
        for (ResolveInfo info : apps) {
            String packageName = info.activityInfo.packageName;
            String name = info.loadLabel(context.getPackageManager()).toString();
            if (isTwitter(packageName)) {
                if (name.equals("Tweet")) {
                    twitter = info;
                }
                continue;
            }
            if (isFacebook(packageName)) {
                continue;
            }

            shareApps.add(new ShareApp(packageName, name, info.loadIcon(context.getPackageManager())));
        }

        if (twitter != null) {
            shareApps.add(0, new ShareApp(twitter.activityInfo.packageName, "Twitter", twitter.loadIcon(context.getPackageManager())));
        }

        shareApps.add(0, new ShareApp(null, "Facebook", ContextCompat.getDrawable(getContext(), R.drawable.ic_facebook_blue_40dp)));
        shareApps.add(0, new ShareApp(null, "Email or SMS", ContextCompat.getDrawable(getContext(), R.drawable.ic_email_red_40dp)));
        return shareApps;
    }

    private static boolean isFacebook(String packageName) {
        return packageName.startsWith(FACEBOOK_PACKAGE);
    }

    private static boolean isTwitter(String packageName) {
        return packageName.startsWith(TWITTER_PACKAGE);
    }

    public void show(FragmentManager fragmentManager) {
        show(fragmentManager, TAG);
    }

    @Override
    public void onActivityCreated(Bundle arg0) {
        super.onActivityCreated(arg0);
        getDialog().getWindow()
                .getAttributes().windowAnimations = R.style.SlideInDialogAnimation;
    }

    public void onInviteWithFirebase(String message) {
        Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invite_title))
                .setMessage(message)
                .setCustomImage(Uri.parse(Constants.INVITE_IMAGE_URL))
                .setCallToActionText(getString(R.string.invite_call_to_action))
                .build();
        getActivity().startActivityForResult(intent, INVITE_FRIEND_REQUEST_CODE);
    }

    public void onInviteWithFacebook() {
        if (AppInviteDialog.canShow()) {
            AppInviteContent content = new AppInviteContent.Builder()
                    .setApplinkUrl(Constants.FACEBOOK_APP_LINK)
                    .setPreviewImageUrl(Constants.INVITE_IMAGE_URL)
                    .build();
            AppInviteDialog.show(this, content);
        } else {
            Toast.makeText(getContext(), R.string.invite_request_update_facebook, Toast.LENGTH_LONG).show();
        }
    }
}

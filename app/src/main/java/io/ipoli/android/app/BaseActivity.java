package io.ipoli.android.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.facebook.share.model.AppInviteContent;
import com.facebook.share.widget.AppInviteDialog;
import com.squareup.otto.Bus;

import javax.inject.Inject;

import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.events.ContactUsTapEvent;
import io.ipoli.android.app.events.FeedbackTapEvent;
import io.ipoli.android.app.events.InviteFriendEvent;
import io.ipoli.android.app.utils.EmailUtils;
import io.ipoli.android.tutorial.TutorialActivity;
import io.ipoli.android.tutorial.events.ShowTutorialEvent;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
public class BaseActivity extends AppCompatActivity {

    @Inject
    protected Bus eventBus;

    protected AppComponent appComponent() {
        return App.getAppComponent(this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appComponent().inject(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_feedback:
                eventBus.post(new FeedbackTapEvent());
                EmailUtils.send(this, getString(R.string.feedback_email_subject), getString(R.string.feedback_email_chooser_title));
                break;
            case R.id.action_contact_us:
                eventBus.post(new ContactUsTapEvent());
                EmailUtils.send(this, getString(R.string.contact_us_email_subject), getString(R.string.contact_us_email_chooser_title));
                break;
            case R.id.action_show_tutorial:
                eventBus.post(new ShowTutorialEvent());
                startTutorial();
                break;
            case R.id.action_invite_friend:
                eventBus.post(new InviteFriendEvent());
                inviteFriend();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void inviteFriend() {
        if (AppInviteDialog.canShow()) {
            AppInviteContent content = new AppInviteContent.Builder()
                    .setApplinkUrl(Constants.FACEBOOK_APP_LINK)
                    .setPreviewImageUrl(Constants.FACEBOOK_INVITE_IMAGE_URL)
                    .build();
            AppInviteDialog.show(this, content);
        } else {
            Toast.makeText(this, R.string.show_invite_failed, Toast.LENGTH_LONG).show();
        }
    }

    protected void startTutorial() {
        Intent intent = new Intent(this, TutorialActivity.class);
        startActivity(intent);
    }

}

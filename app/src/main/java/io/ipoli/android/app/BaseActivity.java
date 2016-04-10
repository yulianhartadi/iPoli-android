package io.ipoli.android.app;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.squareup.otto.Bus;

import javax.inject.Inject;

import io.ipoli.android.R;
import io.ipoli.android.app.events.ContactUsClickEvent;
import io.ipoli.android.app.events.FeedbackClickEvent;
import io.ipoli.android.app.utils.EmailUtils;

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
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                View feedbackView = findViewById(R.id.action_feedback);
//                Tutorial.getInstance(MainActivity.this).addItem(new TutorialItem.Builder(MainActivity.this)
//                        .setTarget(inboxButton)
//                        .setFocusType(Focus.MINIMUM)
//                        .enableDotAnimation(false)
//                        .setState(Tutorial.State.TUTORIAL_START_INBOX)
//                        .build());
//                Tutorial.getInstance(MainActivity.this).addItem(new TutorialItem.Builder(MainActivity.this)
//                        .setTarget(feedbackView)
//                        .setFocusType(Focus.MINIMUM)
//                        .enableDotAnimation(false)
//                        .performClick(false)
//                        .dismissOnTouch(true)
//                        .setState(Tutorial.State.TUTORIAL_VIEW_FEEDBACK)
//                        .build());
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_feedback:
                eventBus.post(new FeedbackClickEvent());
                EmailUtils.send(this, getString(R.string.feedback_email_subject), getString(R.string.feedback_email_chooser_title));
                break;
            case R.id.action_contact_us:
                eventBus.post(new ContactUsClickEvent());
                EmailUtils.send(this, getString(R.string.contact_us_email_subject), getString(R.string.contact_us_email_chooser_title));
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}

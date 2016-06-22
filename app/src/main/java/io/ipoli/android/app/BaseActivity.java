package io.ipoli.android.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.squareup.otto.Bus;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import org.joda.time.LocalDate;

import java.util.Set;

import javax.inject.Inject;

import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.utils.LocalStorage;
import io.ipoli.android.challenge.activities.PickDailyChallengeQuestsActivity;
import io.ipoli.android.player.ui.dialogs.LevelUpDialogFragment;
import io.realm.Realm;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
public class BaseActivity extends RxAppCompatActivity {

    @Inject
    protected Bus eventBus;
    private Realm realm;

    protected AppComponent appComponent() {
        return App.getAppComponent(this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = Realm.getDefaultInstance();
        appComponent().inject(this);
    }

    protected Realm getRealm() {
        return realm;
    }

    @Override
    protected void onDestroy() {
        realm.close();
        super.onDestroy();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Set<Integer> challengeDays = LocalStorage.of(this).readIntSet(Constants.KEY_DAILY_CHALLENGE_DAYS, Constants.DEFAULT_DAILY_CHALLENGE_DAYS);
        int currentDayOfWeek = LocalDate.now().getDayOfWeek();
        if (!challengeDays.contains(currentDayOfWeek)) {
            menu.findItem(R.id.action_pick_daily_challenge_quests).setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_pick_daily_challenge_quests) {
            startActivity(new Intent(this, PickDailyChallengeQuestsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void showLevelUpMessage(int newLevel) {
        LevelUpDialogFragment.newInstance(newLevel).show(getSupportFragmentManager());
    }

    protected void showLevelDownMessage(int newLevel) {
        Toast.makeText(this, "Level lost! Your level is " + newLevel + "!", Toast.LENGTH_LONG).show();
    }
}

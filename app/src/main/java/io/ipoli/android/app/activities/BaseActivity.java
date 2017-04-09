package io.ipoli.android.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.squareup.otto.Bus;

import org.threeten.bp.LocalDate;

import java.util.Date;
import java.util.Set;

import javax.inject.Inject;

import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.AppComponent;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.KeyboardUtils;
import io.ipoli.android.app.utils.LocalStorage;
import io.ipoli.android.challenge.activities.PickDailyChallengeQuestsActivity;
import io.ipoli.android.player.Player;
import io.ipoli.android.player.persistence.PlayerPersistenceService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
public class BaseActivity extends AppCompatActivity {

    @Inject
    protected Bus eventBus;

    @Inject
    protected LocalStorage localStorage;

    @Inject
    PlayerPersistenceService playerPersistenceService;

    protected AppComponent appComponent() {
        return App.getAppComponent(this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appComponent().inject(this);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (useParentOptionsMenu()) {
            Date todayUtc = DateUtils.toStartOfDayUTC(LocalDate.now());
            Date lastCompleted = new Date(localStorage.readLong(Constants.KEY_DAILY_CHALLENGE_LAST_COMPLETED));
            boolean isCompletedForToday = todayUtc.equals(lastCompleted);

            Set<Integer> challengeDays = localStorage.readIntSet(Constants.KEY_DAILY_CHALLENGE_DAYS, Constants.DEFAULT_DAILY_CHALLENGE_DAYS);
            int currentDayOfWeek = LocalDate.now().getDayOfWeek().getValue();
            if (isCompletedForToday || !challengeDays.contains(currentDayOfWeek)) {
                menu.findItem(R.id.action_pick_daily_challenge_quests).setVisible(false);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (useParentOptionsMenu()) {
            getMenuInflater().inflate(R.menu.main_menu, menu);
        }
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

    protected void showLevelDownMessage(int newLevel) {
        Toast.makeText(this, "Level lost! Your level is " + newLevel + "!", Toast.LENGTH_LONG).show();
    }

    protected void hideKeyboard() {
        KeyboardUtils.hideKeyboard(this);
    }

    protected void showKeyboard() {
        KeyboardUtils.showKeyboard(this);
    }

    protected boolean useParentOptionsMenu() {
        return true;
    }

    protected Player getPlayer() {
        return playerPersistenceService.get();
    }

    protected boolean shouldUse24HourFormat() {
        return getPlayer().getUse24HourFormat();
    }
}

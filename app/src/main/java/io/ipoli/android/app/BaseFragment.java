package io.ipoli.android.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.MenuItem;

import com.squareup.otto.Bus;

import javax.inject.Inject;

import io.ipoli.android.R;
import io.ipoli.android.app.utils.LocalStorage;
import io.ipoli.android.player.Player;
import io.ipoli.android.player.persistence.PlayerPersistenceService;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 6/1/16.
 */
public abstract class BaseFragment extends Fragment {

    @Inject
    Bus eventBus;

    @Inject
    LocalStorage localStorage;

    @Inject
    PlayerPersistenceService playerPersistenceService;

    protected abstract boolean useOptionsMenu();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getAppComponent(getContext()).inject(this);
        setHasOptionsMenu(useOptionsMenu());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_help) {
            showHelpDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void showHelpDialog() {
    }

    protected void postEvent(Object event) {
        eventBus.post(event);
    }

    protected Player getPlayer() {
        return playerPersistenceService.get();
    }

    protected boolean shouldUse24HourFormat() {
        return getPlayer().getUse24HourFormat();
    }

}
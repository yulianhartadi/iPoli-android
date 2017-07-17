package io.ipoli.android.store.fragments;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.BaseFragment;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.events.ScreenShownEvent;
import io.ipoli.android.app.events.SyncCalendarRequestEvent;
import io.ipoli.android.app.persistence.CalendarPersistenceService;
import io.ipoli.android.app.sync.AndroidCalendarEventParser;
import io.ipoli.android.app.sync.AndroidCalendarLoader;
import io.ipoli.android.app.sync.SyncAndroidCalendarProvider;
import io.ipoli.android.app.ui.dialogs.AndroidCalendarsPickerFragment;
import io.ipoli.android.app.ui.dialogs.LoadingDialog;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.LocalStorage;
import io.ipoli.android.player.data.Player;
import io.ipoli.android.player.PowerUpManager;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.store.PowerUp;
import io.ipoli.android.store.activities.StoreActivity;
import io.ipoli.android.store.adapters.PowerUpStoreAdapter;
import io.ipoli.android.store.events.BuyPowerUpEvent;
import io.ipoli.android.store.events.PowerUpUnlockedEvent;
import io.ipoli.android.store.viewmodels.PowerUpViewModel;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static io.ipoli.android.Constants.RC_CALENDAR_PERM;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/23/17.
 */

public class PowerUpStoreFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Void> {

    @Inject
    Bus eventBus;

    @Inject
    PowerUpManager powerUpManager;

    @Inject
    LocalStorage localStorage;

    @Inject
    CalendarPersistenceService calendarPersistenceService;

    @Inject
    AndroidCalendarEventParser androidCalendarEventParser;

    @Inject
    SyncAndroidCalendarProvider syncAndroidCalendarProvider;

    private Unbinder unbinder;

    @BindView(R.id.power_up_list)
    RecyclerView powerUpList;
    private PowerUpStoreAdapter adapter;
    private LoadingDialog loadingDialog;
    private Map<Long, Category> selectedCalendars;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragement_power_up_store, container, false);
        unbinder = ButterKnife.bind(this, view);
        App.getAppComponent(getContext()).inject(this);
        ((StoreActivity) getActivity()).populateTitle(R.string.fragment_power_up_store_title);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        powerUpList.setLayoutManager(layoutManager);

        List<PowerUpViewModel> upgrades = createUpgradeViewModels();

        adapter = new PowerUpStoreAdapter(getContext(), eventBus, upgrades, powerUpManager.getUnlockedCodes());
        powerUpList.setAdapter(adapter);

        eventBus.post(new ScreenShownEvent(getActivity(), EventSource.STORE_UPGRADES));

        return view;
    }

    @NonNull
    private List<PowerUpViewModel> createUpgradeViewModels() {
        List<PowerUpViewModel> upgrades = new ArrayList<>();
        List<PowerUp> lockedPowerUps = new ArrayList<>();
        List<PowerUp> unlockedPowerUps = new ArrayList<>();

        for (PowerUp powerUp : PowerUp.values()) {
            if (powerUpManager.isUnlocked(powerUp)) {
                unlockedPowerUps.add(powerUp);
            } else {
                lockedPowerUps.add(powerUp);
            }
        }

        Collections.sort(unlockedPowerUps, ((u1, u2) ->
                -Long.compare(powerUpManager.getExpirationDate(u1), powerUpManager.getExpirationDate(u2))));

        for (PowerUp powerUp : lockedPowerUps) {
            upgrades.add(new PowerUpViewModel(getContext(), powerUp));
        }

        for (PowerUp powerUp : unlockedPowerUps) {
            upgrades.add(new PowerUpViewModel(getContext(), powerUp, DateUtils.fromMillis(powerUpManager.getExpirationDate(powerUp))));
        }
        return upgrades;
    }

    @Subscribe
    public void buyUpgradeEvent(BuyPowerUpEvent e) {
        PowerUp powerUp = e.powerUp;
        String title = getString(powerUp.title);
        if (powerUpManager.hasEnoughCoinsForPowerUp(powerUp)) {
            powerUpManager.unlock(powerUp);

            eventBus.post(new PowerUpUnlockedEvent(powerUp));
            if (powerUp == PowerUp.CALENDAR_SYNC) {
                pickCalendarsToSync();
            } else {
                String message = getString(R.string.power_up_successfully_bought, title);
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
            adapter.setViewModels(createUpgradeViewModels());
        } else {
            Toast.makeText(getContext(), getString(R.string.power_up_too_expensive, title), Toast.LENGTH_SHORT).show();
        }
    }

    private void pickCalendarsToSync() {
        if (EasyPermissions.hasPermissions(getContext(), Manifest.permission.READ_CALENDAR)) {
            showCalendarsPicker();
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.allow_read_calendars_perm_reason), RC_CALENDAR_PERM, Manifest.permission.READ_CALENDAR);
        }
    }

    @AfterPermissionGranted(RC_CALENDAR_PERM)
    private void showCalendarsPicker() {
        Player player = getPlayer();
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        Fragment fragment = AndroidCalendarsPickerFragment.newInstance(R.string.choose_calendars_title, player.getAndroidCalendars(),
                this::onSelectCalendarsToSync);
        transaction.add(fragment, "calendar_picker");
        transaction.commitAllowingStateLoss();
    }

    private void onSelectCalendarsToSync(Map<Long, Category> selectedCalendars) {
        loadingDialog = LoadingDialog.show(getContext(), getString(R.string.sync_calendars_loading_dialog_title), getString(R.string.sync_calendars_loading_dialog_message));
        this.selectedCalendars = selectedCalendars;
        if (!selectedCalendars.isEmpty()) {
            eventBus.post(new SyncCalendarRequestEvent(selectedCalendars, EventSource.STORE_UPGRADES));
        }
        getLoaderManager().initLoader(1, null, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        eventBus.register(this);
    }

    @Override
    public void onPause() {
        eventBus.unregister(this);
        super.onPause();
    }

    public void onDestroyView() {
        unbinder.unbind();
        super.onDestroyView();
    }

    @Override
    protected boolean useOptionsMenu() {
        return false;
    }

    @Override
    public Loader<Void> onCreateLoader(int id, Bundle args) {
        return new AndroidCalendarLoader(getContext(), localStorage, selectedCalendars, getPlayer(),
                syncAndroidCalendarProvider, androidCalendarEventParser, calendarPersistenceService);
    }

    @Override
    public void onLoadFinished(Loader<Void> loader, Void data) {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
            loadingDialog = null;
        }
    }

    @Override
    public void onLoaderReset(Loader<Void> loader) {
        // intentional
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }
}

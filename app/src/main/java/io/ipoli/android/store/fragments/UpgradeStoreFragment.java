package io.ipoli.android.store.fragments;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.AndroidCalendarEventParser;
import io.ipoli.android.app.App;
import io.ipoli.android.app.BaseFragment;
import io.ipoli.android.app.SyncAndroidCalendarProvider;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.events.ScreenShownEvent;
import io.ipoli.android.app.events.SyncCalendarRequestEvent;
import io.ipoli.android.app.persistence.CalendarPersistenceService;
import io.ipoli.android.app.sync.AndroidCalendarLoader;
import io.ipoli.android.app.ui.dialogs.AndroidCalendarsPickerFragment;
import io.ipoli.android.app.ui.dialogs.LoadingDialog;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.LocalStorage;
import io.ipoli.android.player.UpgradeManager;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.store.Upgrade;
import io.ipoli.android.store.activities.StoreActivity;
import io.ipoli.android.store.adapters.UpgradeStoreAdapter;
import io.ipoli.android.store.events.BuyUpgradeEvent;
import io.ipoli.android.store.events.UpgradeUnlockedEvent;
import io.ipoli.android.store.viewmodels.UpgradeViewModel;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static io.ipoli.android.Constants.RC_CALENDAR_PERM;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/23/17.
 */

public class UpgradeStoreFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Void> {

    @Inject
    Bus eventBus;

    @Inject
    UpgradeManager upgradeManager;

    @Inject
    LocalStorage localStorage;

    @Inject
    CalendarPersistenceService calendarPersistenceService;

    @Inject
    AndroidCalendarEventParser androidCalendarEventParser;

    @Inject
    SyncAndroidCalendarProvider syncAndroidCalendarProvider;

    private Unbinder unbinder;

    @BindView(R.id.upgrade_list)
    RecyclerView upgradeList;
    private UpgradeStoreAdapter adapter;
    private LoadingDialog loadingDialog;
    private Map<Long, Category> selectedCalendars;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragement_upgrade_store, container, false);
        unbinder = ButterKnife.bind(this, view);
        App.getAppComponent(getContext()).inject(this);
        ((StoreActivity) getActivity()).populateTitle(R.string.fragment_upgrade_store_title);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        upgradeList.setLayoutManager(layoutManager);

        List<UpgradeViewModel> upgrades = createUpgradeViewModels();

        adapter = new UpgradeStoreAdapter(getContext(), eventBus, upgrades, upgradeManager.getUnlockedCodes());
        upgradeList.setAdapter(adapter);

        eventBus.post(new ScreenShownEvent(EventSource.STORE_UPGRADES));

        return view;
    }

    @NonNull
    private List<UpgradeViewModel> createUpgradeViewModels() {
        List<UpgradeViewModel> upgrades = new ArrayList<>();
        List<Upgrade> lockedUpgrades = new ArrayList<>();
        List<Upgrade> unlockedUpgrades = new ArrayList<>();

        for (Upgrade upgrade : Upgrade.values()) {
            if (upgradeManager.isUnlocked(upgrade)) {
                unlockedUpgrades.add(upgrade);
            } else {
                lockedUpgrades.add(upgrade);
            }
        }

        Collections.sort(unlockedUpgrades, ((u1, u2) ->
                -Long.compare(upgradeManager.getUnlockDate(u1), upgradeManager.getUnlockDate(u2))));

        for (Upgrade upgrade : lockedUpgrades) {
            upgrades.add(new UpgradeViewModel(getContext(), upgrade));
        }

        for (Upgrade upgrade : unlockedUpgrades) {
            upgrades.add(new UpgradeViewModel(getContext(), upgrade, DateUtils.fromMillis(upgradeManager.getUnlockDate(upgrade))));
        }
        return upgrades;
    }

    @Subscribe
    public void buyUpgradeEvent(BuyUpgradeEvent e) {
        Upgrade upgrade = e.upgrade;
        String title = getString(upgrade.title);
        String message;
        if (upgradeManager.hasEnoughCoinsForUpgrade(upgrade)) {
            upgradeManager.unlock(upgrade);
            message = getString(R.string.upgrade_successfully_bought, title);
            adapter.setViewModels(createUpgradeViewModels());
            eventBus.post(new UpgradeUnlockedEvent(upgrade));
            if (upgrade == Upgrade.CALENDAR_SYNC) {
                pickCalendarsToSync();
            }

        } else {
            message = getString(R.string.upgrade_too_expensive, title);
        }
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void pickCalendarsToSync() {
        if (EasyPermissions.hasPermissions(getContext(), Manifest.permission.READ_CALENDAR)) {
            showCalendarsPicker();
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.allow_read_calendars_perm_reason), RC_CALENDAR_PERM, Manifest.permission.READ_CALENDAR);
        }
    }

    @AfterPermissionGranted(Constants.RC_CALENDAR_PERM)
    private void showCalendarsPicker() {
        AndroidCalendarsPickerFragment.newInstance(R.string.choose_calendars_title, getPlayer().getAndroidCalendars(), this::onSelectCalendarsToSync)
                .show(getFragmentManager());
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

package io.ipoli.android.player;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Bus;

import java.util.NoSuchElementException;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.store.StoreItemType;
import io.ipoli.android.store.Upgrade;
import io.ipoli.android.store.activities.StoreActivity;
import io.ipoli.android.store.events.UpgradeUnlockedEvent;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/22/17.
 */

public class UpgradeDialog extends DialogFragment {
    private static final String TAG = "store_upgrade-dialog";
    private static final String UPGRADE_CODE = "upgrade_code";

    @Inject
    Bus eventBus;

    @Inject
    UpgradeManager upgradeManager;

    @BindView(R.id.upgrade_dialog_title)
    TextView title;

    @BindView(R.id.upgrade_dialog_desc)
    TextView description;

    @BindView(R.id.upgrade_dialog_price)
    TextView price;

    @BindView(R.id.upgrade_price_not_enough_coins)
    TextView notEnoughCoins;

    private OnDismissListener dismissListener;

    private OnUnlockListener unlockListener;

    private Upgrade upgrade;

    private Unbinder unbinder;

    public static UpgradeDialog newInstance(Upgrade upgrade) {
        return newInstance(upgrade, null, null);
    }

    public static UpgradeDialog newInstance(Upgrade upgrade, OnUnlockListener unlockListener) {
        return newInstance(upgrade, unlockListener, null);
    }

    public static UpgradeDialog newInstance(Upgrade upgrade, OnDismissListener dismissListener) {
        return newInstance(upgrade, null, dismissListener);
    }

    public static UpgradeDialog newInstance(Upgrade upgrade, OnUnlockListener unlockListener, OnDismissListener dismissListener) {
        UpgradeDialog fragment = new UpgradeDialog();
        Bundle args = new Bundle();
        args.putInt(UPGRADE_CODE, upgrade.code);
        fragment.setArguments(args);
        if (unlockListener != null) {
            fragment.unlockListener = unlockListener;
        }
        if (dismissListener != null) {
            fragment.dismissListener = dismissListener;
        }
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getAppComponent(getContext()).inject(this);

        if (getArguments() == null) {
            dismiss();
        }

        int code = getArguments().getInt(UPGRADE_CODE);
        upgrade = Upgrade.get(code);
        if (upgrade == null) {
            throw new NoSuchElementException("There is no upgrade with code: " + code);
        }

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getContext());

        View v = inflater.inflate(R.layout.fragment_upgrade_dialog, null);
        View headerView = inflater.inflate(R.layout.fancy_dialog_header, null);

        TextView dialogTitle = (TextView) headerView.findViewById(R.id.fancy_dialog_title);
        dialogTitle.setText(R.string.ready_for_upgrade);

        ImageView image = (ImageView) headerView.findViewById(R.id.fancy_dialog_image);
        image.setImageResource(upgrade.picture);

        unbinder = ButterKnife.bind(this, v);

        title.setText(getString(R.string.upgrade_dialog_title, getString(upgrade.title)));
        description.setText(upgrade.shortDesc);
        notEnoughCoins.setText(getString(R.string.upgrade_dialog_not_enough_coins, upgrade.price));
        price.setText(getString(R.string.upgrade_dialog_price_message, upgrade.price));

        boolean hasEnoughCoins = upgradeManager.hasEnoughCoinsForUpgrade(upgrade);

        price.setVisibility(hasEnoughCoins ? View.VISIBLE : View.GONE);
        notEnoughCoins.setVisibility(hasEnoughCoins ? View.GONE : View.VISIBLE);

        AlertDialog.Builder builder = hasEnoughCoins ? buildDefault(v, headerView) : buildTooExpensive(v, headerView);
        return builder.create();
    }

    private AlertDialog.Builder buildDefault(View view, View titleView) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        return builder.setView(view)
                .setCustomTitle(titleView)
                .setPositiveButton(R.string.unlock, (dialog, which) -> {
                    upgradeManager.unlock(upgrade);
                    String message = getString(R.string.upgrade_successfully_bought, getString(upgrade.title));
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    eventBus.post(new UpgradeUnlockedEvent(upgrade));
                    if (unlockListener != null) {
                        unlockListener.onUnlock();
                    }
                })
                .setNegativeButton(R.string.not_now, (dialog, which) -> {
                })
                .setNeutralButton(R.string.go_to_store, (dialog, which) -> {
                    Intent intent = new Intent(getContext(), StoreActivity.class);
                    intent.putExtra(StoreActivity.START_ITEM_TYPE, StoreItemType.UPGRADES.name());
                    startActivity(intent);
                });
    }

    private AlertDialog.Builder buildTooExpensive(View view, View titleView) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        return builder.setView(view)
                .setCustomTitle(titleView)
                .setPositiveButton(getString(R.string.buy_life_coins), (dialog, which) -> {
                    Intent intent = new Intent(getContext(), StoreActivity.class);
                    intent.putExtra(StoreActivity.START_ITEM_TYPE, StoreItemType.COINS.name());
                    getContext().startActivity(intent);
                })
                .setNegativeButton(R.string.not_now, (dialog, which) -> {
                });
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

    @Override
    public void onDestroyView() {
        unbinder.unbind();
        super.onDestroyView();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (dismissListener != null) {
            dismissListener.onDismiss();
        }
    }

    public interface OnUnlockListener {
        void onUnlock();
    }

    public interface OnDismissListener {
        void onDismiss();
    }
}

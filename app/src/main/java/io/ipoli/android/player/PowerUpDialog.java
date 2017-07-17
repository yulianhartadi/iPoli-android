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
import io.ipoli.android.store.PowerUp;
import io.ipoli.android.store.activities.StoreActivity;
import io.ipoli.android.store.events.PowerUpUnlockedEvent;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/22/17.
 */

public class PowerUpDialog extends DialogFragment {
    private static final String TAG = "store-power-up-dialog";
    private static final String POWER_UP_CODE = "power_up_code";

    @Inject
    Bus eventBus;

    @Inject
    PowerUpManager powerUpManager;

    @BindView(R.id.power_up_dialog_title)
    TextView title;

    @BindView(R.id.power_up_dialog_desc)
    TextView description;

    @BindView(R.id.power_up_dialog_price)
    TextView price;

    @BindView(R.id.power_up_price_not_enough_coins)
    TextView notEnoughCoins;

    private OnDismissListener dismissListener;

    private OnUnlockListener unlockListener;

    private PowerUp powerUp;

    private Unbinder unbinder;

    public static PowerUpDialog newInstance(PowerUp powerUp) {
        return newInstance(powerUp, null, null);
    }

    public static PowerUpDialog newInstance(PowerUp powerUp, OnUnlockListener unlockListener) {
        return newInstance(powerUp, unlockListener, null);
    }

    public static PowerUpDialog newInstance(PowerUp powerUp, OnDismissListener dismissListener) {
        return newInstance(powerUp, null, dismissListener);
    }

    public static PowerUpDialog newInstance(PowerUp powerUp, OnUnlockListener unlockListener, OnDismissListener dismissListener) {
        PowerUpDialog fragment = new PowerUpDialog();
        Bundle args = new Bundle();
        args.putInt(POWER_UP_CODE, powerUp.code);
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

        int code = getArguments().getInt(POWER_UP_CODE);
        powerUp = PowerUp.get(code);
        if (powerUp == null) {
            throw new NoSuchElementException("There is no power-up with code: " + code);
        }

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getContext());

        View v = inflater.inflate(R.layout.fragment_power_up_dialog, null);
        View headerView = inflater.inflate(R.layout.fancy_dialog_header, null);

        TextView dialogTitle = (TextView) headerView.findViewById(R.id.fancy_dialog_title);
        dialogTitle.setText(R.string.ready_for_power_up);

        ImageView image = (ImageView) headerView.findViewById(R.id.fancy_dialog_image);
        image.setImageResource(powerUp.picture);

        unbinder = ButterKnife.bind(this, v);

        title.setText(getString(R.string.power_up_dialog_title, getString(powerUp.title)));
        description.setText(powerUp.shortDesc);
        notEnoughCoins.setText(getString(R.string.power_up_dialog_not_enough_coins, powerUp.price));
        price.setText(getString(R.string.power_up_dialog_price_message, powerUp.price));

        boolean hasEnoughCoins = powerUpManager.hasEnoughCoinsForPowerUp(powerUp);

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
                    powerUpManager.unlock(powerUp);
                    String message = getString(R.string.power_up_successfully_bought, getString(powerUp.title));
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    eventBus.post(new PowerUpUnlockedEvent(powerUp));
                    if (unlockListener != null) {
                        unlockListener.onUnlock();
                    }
                })
                .setNegativeButton(R.string.not_now, (dialog, which) -> {
                })
                .setNeutralButton(R.string.go_to_store, (dialog, which) -> {
                    Intent intent = new Intent(getContext(), StoreActivity.class);
                    intent.putExtra(StoreActivity.START_ITEM_TYPE, StoreItemType.POWER_UPS.name());
                    startActivity(intent);
                });
    }

    private AlertDialog.Builder buildTooExpensive(View view, View titleView) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        return builder.setView(view)
                .setCustomTitle(titleView)
                .setPositiveButton(getString(R.string.buy_life_coins), (dialog, which) -> {
                    Intent intent = new Intent(getContext(), StoreActivity.class);
                    intent.putExtra(StoreActivity.START_ITEM_TYPE, StoreItemType.MEMBERSHIP.name());
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

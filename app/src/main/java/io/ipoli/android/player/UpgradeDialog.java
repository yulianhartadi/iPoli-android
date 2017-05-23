package io.ipoli.android.player;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.NoSuchElementException;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.store.activities.StoreActivity;
import io.ipoli.android.store.StoreItemType;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/22/17.
 */

public class UpgradeDialog extends DialogFragment {
    private static final String TAG = "upgrade-dialog";
    private static final String UPGRADE_CODE = "upgrade_code";

    @Inject
    UpgradesManager upgradesManager;

    @BindView(R.id.upgrade_price)
    TextView price;

    @BindView(R.id.upgrade_price_not_enough_coins)
    TextView notEnoughCoins;

    private Upgrade upgrade;

    private Unbinder unbinder;

    public static UpgradeDialog newInstance(Upgrade upgrade) {
        UpgradeDialog fragment = new UpgradeDialog();
        Bundle args = new Bundle();
        args.putInt(UPGRADE_CODE, upgrade.getCode());
        fragment.setArguments(args);
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
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View v = inflater.inflate(R.layout.fragment_upgrade_dialog, null);
        View titleView = inflater.inflate(R.layout.upgrade_title, null);
        unbinder = ButterKnife.bind(this, v);

        boolean hasEnoughCoins = upgradesManager.hasEnoughCoinsForUpgrade(upgrade);
        String positiveBtnText = hasEnoughCoins ? "Buy" : "Buy coins";

        price.setVisibility(hasEnoughCoins ? View.VISIBLE : View.GONE);
        notEnoughCoins.setVisibility(hasEnoughCoins ? View.GONE : View.VISIBLE);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(v)
                .setCustomTitle(titleView)
                .setPositiveButton(positiveBtnText, (dialog, which) -> {
                    if(hasEnoughCoins) {
                        upgradesManager.buy(upgrade);
                        Toast.makeText(getContext(), "You can now enjoy Repeating quests", Toast.LENGTH_SHORT).show();
                    } else {
                        Intent intent = new Intent(getContext(), StoreActivity.class);
                        intent.putExtra(StoreActivity.START_ITEM_TYPE, StoreItemType.COINS.name());
                        getContext().startActivity(intent);
                    }
                })
                .setNegativeButton("Not now", (dialog, which) -> {
                });

        if (hasEnoughCoins) {
            builder.setNeutralButton("Go to Store", (dialog, which) -> {
            });
        }

        return builder.create();
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
}

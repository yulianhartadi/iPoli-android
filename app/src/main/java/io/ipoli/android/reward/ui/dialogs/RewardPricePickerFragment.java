package io.ipoli.android.reward.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;

import java.util.Arrays;
import java.util.List;

import io.ipoli.android.Constants;
import io.ipoli.android.R;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 6/17/16.
 */
public class RewardPricePickerFragment extends DialogFragment {
    private static final String TAG = "reward-price-picker-dialog";
    private static final String POINTS = "points";

    private Integer points;
    private OnPricePickedListener pricePickedListener;
    private int selectedPriceIndex;

    public static RewardPricePickerFragment newInstance(OnPricePickedListener pricePickedListener) {
        return newInstance(null, pricePickedListener);
    }

    public static RewardPricePickerFragment newInstance(Integer points, OnPricePickedListener pricePickedListener) {
        RewardPricePickerFragment fragment = new RewardPricePickerFragment();
        if (points != null) {
            Bundle args = new Bundle();
            args.putInt(POINTS, points);
            fragment.setArguments(args);
        }
        fragment.pricePickedListener = pricePickedListener;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            points = getArguments().getInt(POINTS);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        List<Integer> availablePrices = Arrays.asList(Constants.REWARD_POINTS);
        String[] prices = new String[availablePrices.size()];
        selectedPriceIndex = 0;
        for (int i = 0; i < availablePrices.size(); i++) {
            prices[i] = getString(R.string.reward_price_points, availablePrices.get(i));
            if (availablePrices.get(i).equals(points)) {
                selectedPriceIndex = i;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(R.drawable.logo)
                .setTitle(R.string.reward_price_question)
                .setSingleChoiceItems(prices, selectedPriceIndex, (dialog, which) ->
                        selectedPriceIndex = which)
                .setPositiveButton(getString(R.string.help_dialog_ok), (dialog, which) ->
                        pricePickedListener.onPricePicked(availablePrices.get(selectedPriceIndex)))
                .setNegativeButton(R.string.cancel, (dialog, which) -> {

                })
                .setNeutralButton(R.string.custom, (dialog, which) ->
                        CustomRewardPricePickerFragment.newInstance(points, pricePickedListener).show(getFragmentManager()));
        return builder.create();

    }

    public void show(FragmentManager fragmentManager) {
        show(fragmentManager, TAG);
    }
}

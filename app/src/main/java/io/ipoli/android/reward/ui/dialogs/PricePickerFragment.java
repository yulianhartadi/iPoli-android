package io.ipoli.android.reward.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;

import java.util.ArrayList;
import java.util.List;

import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.reward.formatters.PriceFormatter;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 6/17/16.
 */
public class PricePickerFragment extends DialogFragment {
    private static final String TAG = "price-picker-dialog";
    private static final String PRICE = "price";
    private static final int MIN_PRICE = Constants.DEFAULT_MIN_REWARD_PRICE;
    private static final int MAX_PRICE = 1000;
    private static final int PRICE_STEP = 50;

    private int price;
    private int selectedPriceIndex;

    private OnPricePickedListener pricePickedListener;

    public interface OnPricePickedListener {
        void onPricePicked(int price);
    }

    public static PricePickerFragment newInstance(OnPricePickedListener pricePickedListener) {
        return newInstance(-1 , pricePickedListener);
    }

    public static PricePickerFragment newInstance(int price, OnPricePickedListener pricePickedListener) {
        PricePickerFragment fragment = new PricePickerFragment();
        Bundle args = new Bundle();
        args.putInt(PRICE, Math.max(price, MIN_PRICE));
        fragment.setArguments(args);
        fragment.pricePickedListener = pricePickedListener;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            price = getArguments().getInt(PRICE);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        List<Integer> availablePrices = generateAvailablePrices();

        List<String> prices = new ArrayList<>();
        selectedPriceIndex = -1;
        for (int i = 0; i < availablePrices.size(); i++) {
            int price = availablePrices.get(i);
            prices.add(PriceFormatter.formatReadable(price));
            if (price == this.price) {
                selectedPriceIndex = i;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(R.drawable.logo)
                .setTitle(R.string.reward_price_question)
                .setSingleChoiceItems(prices.toArray(new String[prices.size()]), selectedPriceIndex, (dialog, which) -> {
                    selectedPriceIndex = which;
                })
                .setPositiveButton(getString(R.string.help_dialog_ok), (dialog, which) -> {
                    pricePickedListener.onPricePicked(availablePrices.get(selectedPriceIndex));
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {

                });
        return builder.create();

    }

    @NonNull
    private List<Integer> generateAvailablePrices() {
        List<Integer> availablePrices = new ArrayList<>();
        int p = MIN_PRICE;
        do {
            availablePrices.add(p);
            p += PRICE_STEP;
        } while (p <= MAX_PRICE);
        return availablePrices;
    }

    public void show(FragmentManager fragmentManager) {
        show(fragmentManager, TAG);
    }
}

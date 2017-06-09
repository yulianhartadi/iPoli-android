package io.ipoli.android.reward.ui.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.utils.StringUtils;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 6/09/17.
 */
public class CustomPointsPickerFragment extends DialogFragment {
    private static final String TAG = "custom-points-picker-dialog";
    private static final String POINTS = "points";

    @BindView(R.id.reward_points)
    TextInputEditText pointsView;

    private Integer points;
    private OnPricePickedListener pricePickedListener;
    private Unbinder unbinder;

    public static CustomPointsPickerFragment newInstance(OnPricePickedListener pricePickedListener) {
        return newInstance(null, pricePickedListener);
    }

    public static CustomPointsPickerFragment newInstance(Integer points, OnPricePickedListener pricePickedListener) {
        CustomPointsPickerFragment fragment = new CustomPointsPickerFragment();
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
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_price_picker, null);
        unbinder = ButterKnife.bind(this, view);

        if (points != null) {
            pointsView.setText(String.valueOf(points));
            pointsView.setSelection(pointsView.getText().length());
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(R.drawable.logo)
                .setTitle(R.string.reward_price_question)
                .setView(view)
                .setPositiveButton(getString(R.string.help_dialog_ok), (dialog, which) -> {
                }).setNegativeButton(R.string.cancel, (dialog, which) -> {

        });

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialog1 -> {
            onShowDialog(dialog);
        });
        return dialog;

    }

    private void onShowDialog(AlertDialog dialog) {
        Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(v -> {
            String text = pointsView.getText().toString();
            if (StringUtils.isEmpty(text)) {
                Toast.makeText(getContext(), "Can't have free rewards", Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedPoints = Constants.REWARD_MAX_PRICE + 1;
            try {
                selectedPoints = Integer.parseInt(pointsView.getText().toString());
            } catch (Exception e) {
            }

            if (selectedPoints > Constants.REWARD_MAX_PRICE) {
                Toast.makeText(getContext(), "Too expensive, be more realistic", Toast.LENGTH_SHORT).show();
                return;
            }

            pricePickedListener.onPricePicked(selectedPoints);
            dismiss();
        });
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
        super.onDestroyView();
    }

    public void show(FragmentManager fragmentManager) {
        show(fragmentManager, TAG);
    }
}

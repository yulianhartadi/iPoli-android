package io.ipoli.android.app.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import io.ipoli.android.R;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/17/16.
 */
public class NoInternetDialogFragment extends DialogFragment {

    private static final String TAG = "no-internet-dialog";
    private OnConfirmListener confirmListener;

    public interface OnConfirmListener {
        public void onConfirm();
    }

    public static NoInternetDialogFragment newInstance(OnConfirmListener confirmListener) {
        NoInternetDialogFragment fragment = new NoInternetDialogFragment();
        fragment.confirmListener = confirmListener;
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_text_picker, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(R.drawable.logo)
                .setTitle(R.string.title_no_internet)
                .setMessage(R.string.turn_on_internet)
                .setCancelable(false)
                .setPositiveButton(getString(R.string.help_dialog_ok), (dialog, which) -> {
                    confirmListener.onConfirm();
                });
        return builder.create();
    }

    public void show(FragmentManager fragmentManager) {
        show(fragmentManager, TAG);
    }
}
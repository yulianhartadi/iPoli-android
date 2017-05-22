package io.ipoli.android.player;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import java.util.NoSuchElementException;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.ipoli.android.R;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/22/17.
 */

public class UpgradeDialog extends DialogFragment {
    private static final String TAG = "upgrade-dialog";
    public static final String UPGRADE_CODE = "upgrade_code";

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

        if(getArguments() == null) {
            dismiss();
        }

        int code = getArguments().getInt(UPGRADE_CODE);
        upgrade = Upgrade.get(code);
        if(upgrade == null) {
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

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(v)
                .setCustomTitle(titleView)
                .setPositiveButton("Buy", (dialog, which) -> {

                })
                .setNegativeButton("Not now", (dialog, which) -> {

                })
                .setNeutralButton("Go to Store", (dialog, which) -> {
                });
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

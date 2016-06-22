package io.ipoli.android.challenge.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.ipoli.android.R;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/22/16.
 */
public class ChallengeCompleteDialogFragment extends DialogFragment {

    private static final String TAG = "challenge-complete-dialog";

    private static final String EXPERIENCE = "experience";
    private static final String COINS = "coins";

    @BindView(R.id.experience_text)
    TextView experienceText;

    @BindView(R.id.coins_text)
    TextView coinsText;

    private Unbinder unbinder;

    private int experience;
    private int coins;

    public static ChallengeCompleteDialogFragment newInstance(int experience, int coins) {
        ChallengeCompleteDialogFragment fragment = new ChallengeCompleteDialogFragment();
        Bundle args = new Bundle();
        args.putInt(EXPERIENCE, experience);
        args.putInt(COINS, coins);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            experience = getArguments().getInt(EXPERIENCE);
            coins = getArguments().getInt(COINS);
        }
    }

    public void show(FragmentManager fragmentManager) {
        show(fragmentManager, TAG);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_challenge_complete, null);
        unbinder = ButterKnife.bind(this, view);
        experienceText.setText(String.valueOf(experience));
        coinsText.setText(String.valueOf(coins));
        return builder.setIcon(R.drawable.logo)
                .setTitle("Daily challenge complete")
                .setView(view)
                .setPositiveButton(getString(R.string.sweet), null)
                .create();
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
        super.onDestroyView();
    }

    @Override
    public void onActivityCreated(Bundle arg0) {
        super.onActivityCreated(arg0);
        getDialog().getWindow()
                .getAttributes().windowAnimations = R.style.ScaleInDialogAnimation;
    }
}

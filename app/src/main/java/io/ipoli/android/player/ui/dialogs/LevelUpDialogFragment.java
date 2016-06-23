package io.ipoli.android.player.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.ipoli.android.R;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/1/16.
 */
public class LevelUpDialogFragment extends DialogFragment {

    private static final String TAG = "level-up-dialog";

    private static final String LEVEL = "level";

    @BindView(R.id.message)
    TextView message;

    private int level;

    private Unbinder unbinder;

    public static LevelUpDialogFragment newInstance(int level) {
        LevelUpDialogFragment fragment = new LevelUpDialogFragment();
        Bundle args = new Bundle();
        args.putInt(LEVEL, level);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null && getArguments().containsKey(LEVEL)) {
            level = getArguments().getInt(LEVEL);
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
        View view = inflater.inflate(R.layout.fragment_level_up, null);
        unbinder = ButterKnife.bind(this, view);
        message.setText(Html.fromHtml(getString(R.string.level_up_message, level)));
        return builder.setIcon(R.drawable.logo)
                .setTitle(getString(R.string.level_up_title))
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

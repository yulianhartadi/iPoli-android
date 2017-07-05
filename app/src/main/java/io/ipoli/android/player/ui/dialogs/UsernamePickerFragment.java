package io.ipoli.android.player.ui.dialogs;

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

import com.squareup.otto.Bus;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.events.ScreenShownEvent;
import io.ipoli.android.app.ui.UsernameValidator;
import io.ipoli.android.feed.persistence.FeedPersistenceService;

import static io.ipoli.android.app.utils.KeyboardUtils.hideKeyboard;
import static io.ipoli.android.app.utils.KeyboardUtils.showKeyboard;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/17/16.
 */
public class UsernamePickerFragment extends DialogFragment {

    private static final String TAG = "username-picker-dialog";

    @Inject
    Bus eventBus;

    @Inject
    FeedPersistenceService feedPersistenceService;

    @BindView(R.id.username)
    TextInputEditText usernameView;

    private OnUsernamePickedListener usernamePickedListener;

    private Unbinder unbinder;


    public static UsernamePickerFragment newInstance(OnUsernamePickedListener listener) {
        UsernamePickerFragment fragment = new UsernamePickerFragment();
        fragment.usernamePickedListener = listener;
        return fragment;
    }

    public interface OnUsernamePickedListener {
        void onUsernamePicked(String username);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getAppComponent(getContext()).inject(this);
        eventBus.post(new ScreenShownEvent(getActivity(), EventSource.USERNAME_PICKER));
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_username_picker, null);
        unbinder = ButterKnife.bind(this, view);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(R.drawable.logo)
                .setView(view)
                .setTitle(R.string.username_picker_title)
                .setPositiveButton(getString(R.string.help_dialog_ok), (dialog, which) -> {
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    hideKeyboard(getDialog());
                });
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> onShowDialog(dialog));
        return dialog;
    }

    private void onShowDialog(AlertDialog dialog) {
        Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(v -> {
            String username = usernameView.getText().toString();
            UsernameValidator.validate(username, feedPersistenceService, new UsernameValidator.ResultListener() {
                @Override
                public void onValid() {
                    hideKeyboard(dialog);
                    usernamePickedListener.onUsernamePicked(username);
                    dismiss();
                }

                @Override
                public void onInvalid(UsernameValidator.UsernameValidationError error) {
                    switch (error) {
                        case EMPTY:
                            usernameView.setError(getString(R.string.username_is_empty));
                            break;
                        case NOT_UNIQUE:
                            usernameView.setError(getString(R.string.username_is_taken));
                            break;
                        default:
                            usernameView.setError(getString(R.string.username_wrong_format));
                            break;
                    }
                }
            });
        });
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        showKeyboard(getDialog());
        super.onActivityCreated(savedInstanceState);
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
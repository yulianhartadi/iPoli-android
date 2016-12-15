package io.ipoli.android.app.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.ipoli.android.R;
import io.ipoli.android.app.utils.StringUtils;

import static io.ipoli.android.app.utils.KeyboardUtils.hideKeyboard;
import static io.ipoli.android.app.utils.KeyboardUtils.showKeyboard;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/17/16.
 */
public class TextPickerFragment extends DialogFragment {

    private static final String TAG = "text-picker-dialog";
    private static final String TEXT = "text";
    private static final String TITLE = "title";

    @BindView(R.id.note)
    EditText noteText;

    private OnTextPickedListener textPickedListener;

    private Unbinder unbinder;

    private String text;
    private int title;

    public static TextPickerFragment newInstance(@StringRes int title, OnTextPickedListener listener) {
        return newInstance("", title, listener);
    }

    public static TextPickerFragment newInstance(String text, @StringRes int title, OnTextPickedListener listener) {
        TextPickerFragment fragment = new TextPickerFragment();
        Bundle args = new Bundle();
        args.putString(TEXT, text);
        args.putInt(TITLE, title);
        fragment.setArguments(args);
        fragment.textPickedListener = listener;
        return fragment;
    }

    public interface OnTextPickedListener {
        void onTextPicked(String text);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        text = getArguments().getString(TEXT);
        title = getArguments().getInt(TITLE);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_text_picker, null);
        unbinder = ButterKnife.bind(this, view);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if (!StringUtils.isEmpty(text)) {
            noteText.setText(text);
            noteText.setSelection(text.length());
        }
        builder.setIcon(R.drawable.logo)
                .setView(view)
                .setTitle(title)
                .setPositiveButton(getString(R.string.help_dialog_ok), (dialog, which) -> {
                    hideKeyboard(getDialog());
                    textPickedListener.onTextPicked(noteText.getText().toString());
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    hideKeyboard(getDialog());
                });
        return builder.create();
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
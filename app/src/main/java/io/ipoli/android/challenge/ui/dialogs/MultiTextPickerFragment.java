package io.ipoli.android.challenge.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import io.ipoli.android.R;

import static io.ipoli.android.app.utils.KeyboardUtils.hideKeyboard;
import static io.ipoli.android.app.utils.KeyboardUtils.showKeyboard;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/17/16.
 */
public class MultiTextPickerFragment extends DialogFragment {
    private static final String TAG = "multi-text-picker-dialog";
    private static final String TEXTS = "texts";
    private static final String HINTS = "hints";
    private static final String TITLE = "title";

    @BindView(R.id.note)
    EditText noteText;

    private OnMultiTextPickedListener textPickedListener;

    private List<String> texts;
    private List<String> hints;
    private int title;
    private List<TextInputEditText> textViews;

    public interface OnMultiTextPickedListener {
        void onTextPicked(List<String> texts);
    }

    public static MultiTextPickerFragment newInstance(ArrayList<String> texts, ArrayList<String> hints, @StringRes int title, OnMultiTextPickedListener listener) {
        if (texts.size() != hints.size()) {
            throw new IllegalArgumentException("Hints and texts must have the same size");
        }
        MultiTextPickerFragment fragment = new MultiTextPickerFragment();
        Bundle args = new Bundle();
        args.putStringArrayList(TEXTS, texts);
        args.putStringArrayList(HINTS, hints);
        args.putInt(TITLE, title);
        fragment.setArguments(args);
        fragment.textPickedListener = listener;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        texts = getArguments().getStringArrayList(TEXTS);
        hints = getArguments().getStringArrayList(HINTS);
        title = getArguments().getInt(TITLE);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_multi_text_picker, null);

        textViews = new ArrayList<>();

        for (int i = 0; i < texts.size(); i++) {
            View textItem = inflater.inflate(R.layout.text_picker_item, view, false);
            ((TextInputLayout) textItem.findViewById(R.id.text_picker_container)).setHint(hints.get(i));
            TextInputEditText textView = ((TextInputEditText) textItem.findViewById(R.id.text_picker_text));
            textView.setText(texts.get(i));
            textViews.add(textView);
            view.addView(textItem);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(R.drawable.logo)
                .setView(view)
                .setTitle(title)
                .setPositiveButton(getString(R.string.help_dialog_ok), (dialog, which) -> {
                    hideKeyboard(getDialog());
                    List<String> texts = new ArrayList<>();
                    for (TextInputEditText textView : textViews) {
                        texts.add(textView.getText().toString());
                    }
                    textPickedListener.onTextPicked(texts);
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

    public void show(FragmentManager fragmentManager) {
        show(fragmentManager, TAG);
    }
}
package io.ipoli.android.challenge.ui.dialogs;

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
import android.view.ViewGroup;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import io.ipoli.android.R;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/17/16.
 */
public class MultiTextPickerFragment extends DialogFragment {
    private static final String TAG = "multi-text-picker-dialog";
    private static final String TEXTS = "texts";
    private static final String TITLE = "title";

    @BindView(R.id.note)
    EditText noteText;

    private OnTextPickedListener textPickedListener;

    private List<String> texts;
    private int title;

    public static MultiTextPickerFragment newInstance(ArrayList<String> texts, @StringRes int title, OnTextPickedListener listener) {
        MultiTextPickerFragment fragment = new MultiTextPickerFragment();
        Bundle args = new Bundle();
        args.putStringArrayList(TEXTS, texts);
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
        texts = getArguments().getStringArrayList(TEXTS);
        title = getArguments().getInt(TITLE);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_text_picker, null);

        for(String text : texts) {
            View textItem = inflater.inflate(R.layout.fragment_text_picker, view, false);
            view.addView(textItem);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//        if (!TextUtils.isEmpty(texts)) {
//            noteText.setText(texts);
//            noteText.setSelection(texts.length());
//        }
        builder.setIcon(R.drawable.logo)
                .setView(view)
                .setTitle(title)
                .setPositiveButton(getString(R.string.help_dialog_ok), (dialog, which) -> {
                    textPickedListener.onTextPicked(noteText.getText().toString());
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {

                });
        return builder.create();
    }

    public void show(FragmentManager fragmentManager) {
        show(fragmentManager, TAG);
    }
}
package com.curiousily.ipoli.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.curiousily.ipoli.EventBus;
import com.curiousily.ipoli.R;
import com.curiousily.ipoli.ui.events.ChangeInputEvent;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/12/15.
 */
public class InputFragment extends DialogFragment {

    public static final String FRAGMENT_TAG = "input_dialog";
    @InjectView(R.id.input_query)
    TextView inputQuery;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_input, container, false);
        ButterKnife.inject(this, view);
        inputQuery.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
        inputQuery.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    inputQuery.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
                } else {
                    inputQuery.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
                }
            }
        });

        return view;
    }

    @Subscribe
    public void onChangeInput(ChangeInputEvent e) {
        inputQuery.setText(e.getInput());
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.get().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.get().register(this);
    }
}

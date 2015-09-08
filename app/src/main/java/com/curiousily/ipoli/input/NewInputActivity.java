package com.curiousily.ipoli.input;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.widget.TextView;

import com.curiousily.ipoli.EventBus;
import com.curiousily.ipoli.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/8/15.
 */
public class NewInputActivity extends AppCompatActivity {

    @Bind(R.id.input_query)
    TextView inputQuery;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_input);
        setTitleColor(getResources().getColor(R.color.md_dark_text_87));
        ButterKnife.bind(this);
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
    }
//
//    @Subscribe
//    public void onChangeInput(ChangeInputEvent e) {
//        inputQuery.setText(e.getInput());
//    }

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

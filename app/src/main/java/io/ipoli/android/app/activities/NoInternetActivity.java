package io.ipoli.android.app.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import io.ipoli.android.R;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 8/14/16.
 */
public class NoInternetActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

        new AlertDialog.Builder(this)
                .setIcon(R.drawable.logo)
                .setTitle("Can I get a connection?")
                .setMessage("I require Internet connection only on the first run to properly setup your experience. Please, enable it.")
                .setPositiveButton("Ok", (dialog, which) -> finish())
                .show();
    }
}
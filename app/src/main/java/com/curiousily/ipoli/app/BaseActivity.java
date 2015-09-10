package com.curiousily.ipoli.app;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.curiousily.ipoli.EventBus;
import com.curiousily.ipoli.R;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/10/15.
 */
public class BaseActivity extends AppCompatActivity implements DialogInterface.OnClickListener {

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.get().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.get().unregister(this);
    }

    public void showAlertDialog(int title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.ok, this)
                .setCancelable(false).show();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        finish();
    }
}

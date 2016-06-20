package io.ipoli.android.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.widget.Toast;

import com.squareup.otto.Bus;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import javax.inject.Inject;

import io.ipoli.android.R;
import io.ipoli.android.player.ui.dialogs.LevelUpDialog;
import io.realm.Realm;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
public class BaseActivity extends RxAppCompatActivity {

    @Inject
    protected Bus eventBus;
    private Realm realm;

    protected AppComponent appComponent() {
        return App.getAppComponent(this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = Realm.getDefaultInstance();
        appComponent().inject(this);
    }

    protected Realm getRealm() {
        return realm;
    }

    @Override
    protected void onDestroy() {
        realm.close();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    protected void showLevelUpMessage(int newLevel) {
        LevelUpDialog.newInstance(newLevel).show(getSupportFragmentManager());
    }

    protected void showLevelDownMessage(int newLevel) {
        Toast.makeText(this, "Level lost! Your level is " + newLevel + "!", Toast.LENGTH_LONG).show();
    }
}

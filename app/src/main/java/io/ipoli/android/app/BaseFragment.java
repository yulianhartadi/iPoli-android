package io.ipoli.android.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.MenuItem;

import io.ipoli.android.R;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 6/1/16.
 */
public abstract class BaseFragment extends Fragment {

    protected abstract boolean useOptionsMenu();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(useOptionsMenu());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_help) {
            showHelpDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void showHelpDialog() {
    }
}
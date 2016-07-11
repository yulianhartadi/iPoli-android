package io.ipoli.android.app;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.trello.rxlifecycle.components.support.RxFragment;

import io.ipoli.android.R;
import io.realm.Realm;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 6/1/16.
 */
public abstract class BaseFragment extends RxFragment {

    private Realm realm;

    protected abstract boolean useOptionsMenu();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(useOptionsMenu());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        realm = Realm.getDefaultInstance();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    protected Realm getRealm() {
        return realm;
    }

    @Override
    public void onDestroyView() {
        realm.close();
        super.onDestroyView();
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

    protected void showKeyboard() {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    protected void hideKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
package io.ipoli.android.app;

import android.support.v4.app.Fragment;

import butterknife.ButterKnife;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
public class BaseFragment extends Fragment {
    protected AppComponent appComponent() {
        return ((App) getActivity().getApplication()).getAppComponent();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}

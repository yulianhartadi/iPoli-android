package io.ipoli.android.app.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.ipoli.android.R;
import io.ipoli.android.challenge.fragments.AddChallengeSummaryFragment;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 7/1/17.
 */

public class DateTimePickerFragment extends DialogFragment {

    private static final String TAG = "date-time-picker-dialog";

    @BindView(R.id.pickers_tabs)
    TabLayout tabLayout;

    @BindView(R.id.pickers_pager)
    ViewPager viewPager;

    private Unbinder unbinder;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_date_time_picker, null);
        unbinder = ButterKnife.bind(this, view);

        PickerPagerAdapter adapter = new PickerPagerAdapter(getFragmentManager());

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(R.drawable.logo)
                .setView(view)
                .setPositiveButton(getString(R.string.help_dialog_ok), (dialog, which) -> {
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                });
        return builder.create();
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
        super.onDestroyView();
    }

    public void show(FragmentManager fragmentManager) {
        show(fragmentManager, TAG);
    }

    private class PickerPagerAdapter extends FragmentPagerAdapter {

        PickerPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
//                case 0:
//                    return new DateFragment();
//                default:
//                    return new AddChallengeSummaryFragment();
            }
        }
    }

}

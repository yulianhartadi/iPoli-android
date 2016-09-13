package io.ipoli.android.challenge.adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.otto.Bus;

import java.util.List;

import io.ipoli.android.R;
import io.ipoli.android.challenge.viewmodels.PickChallengeViewModel;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/13/16.
 */
public class PickChallengeAdapter extends PagerAdapter {

    private final List<PickChallengeViewModel> viewModels;
    private final Bus eventBus;
    private final LayoutInflater layoutInflater;
    private final Context context;

    public PickChallengeAdapter(final Context context, List<PickChallengeViewModel> viewModels, Bus eventBus) {
        this.viewModels = viewModels;
        this.eventBus = eventBus;
        this.layoutInflater = LayoutInflater.from(context);
        this.context = context;
    }

    @Override
    public int getCount() {
        return viewModels.size();
    }

    @Override
    public Object instantiateItem(final ViewGroup container, final int position) {
        final View view = layoutInflater.inflate(R.layout.pick_challenge_item, container, false);
        PickChallengeViewModel vm = viewModels.get(position);

        TextView challengeName = (TextView) view.findViewById(R.id.challenge_name);
        TextView challengeDescription = (TextView) view.findViewById(R.id.challenge_description);
        ImageView challengePicture = (ImageView) view.findViewById(R.id.challenge_picture);

        challengeName.setText(vm.getName());
        challengeDescription.setText(vm.getDescription());
        challengePicture.setImageDrawable(context.getDrawable(vm.getPicture()));

        container.addView(view);
        return view;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    @Override
    public void destroyItem(final ViewGroup container, final int position, final Object object) {
        container.removeView((View) object);
    }
}

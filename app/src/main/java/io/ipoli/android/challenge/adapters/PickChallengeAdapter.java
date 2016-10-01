package io.ipoli.android.challenge.adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.otto.Bus;

import java.util.List;

import io.ipoli.android.R;
import io.ipoli.android.challenge.data.PredefinedChallenge;
import io.ipoli.android.challenge.events.PersonalizeChallengeEvent;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/13/16.
 */
public class PickChallengeAdapter extends PagerAdapter {

    private final List<PredefinedChallenge> predefinedChallenges;
    private final Bus eventBus;
    private final LayoutInflater layoutInflater;
    private final Context context;

    public PickChallengeAdapter(final Context context, List<PredefinedChallenge> predefinedChallenges, Bus eventBus) {
        this.predefinedChallenges = predefinedChallenges;
        this.eventBus = eventBus;
        this.layoutInflater = LayoutInflater.from(context);
        this.context = context;
    }

    @Override
    public int getCount() {
        return predefinedChallenges.size();
    }

    @Override
    public Object instantiateItem(final ViewGroup container, final int position) {
        final View view = layoutInflater.inflate(R.layout.pick_challenge_item, container, false);
        PredefinedChallenge pc = predefinedChallenges.get(position);

        TextView challengeName = (TextView) view.findViewById(R.id.challenge_name);
        TextView challengeDescription = (TextView) view.findViewById(R.id.challenge_description);
        ImageView challengePicture = (ImageView) view.findViewById(R.id.challenge_picture);
        Button personalize = (Button) view.findViewById(R.id.challenge_personalize);

        challengeName.setText(pc.challenge.getName());
        challengeDescription.setText(pc.description);
        challengePicture.setImageDrawable(context.getDrawable(pc.picture));
        personalize.setOnClickListener(view1 -> eventBus.post(new PersonalizeChallengeEvent(position)));

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

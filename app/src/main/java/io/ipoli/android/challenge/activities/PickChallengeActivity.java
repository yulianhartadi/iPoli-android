package io.ipoli.android.challenge.activities;

import android.animation.ArgbEvaluator;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.ViewGroup;

import com.gigamole.infinitecycleviewpager.HorizontalInfiniteCycleViewPager;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.app.activities.BaseActivity;
import io.ipoli.android.challenge.adapters.PickChallengeAdapter;
import io.ipoli.android.challenge.viewmodels.PickChallengeViewModel;
import io.ipoli.android.quest.data.Category;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/13/16.
 */
public class PickChallengeActivity extends BaseActivity {

    @BindView(R.id.root_container)
    ViewGroup rootContainer;

    @BindView(R.id.appbar)
    AppBarLayout appBar;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.challenge_view_pager)
    HorizontalInfiniteCycleViewPager viewPager;

    private final ArgbEvaluator argbEvaluator = new ArgbEvaluator();
    private List<PickChallengeViewModel> challengeViewModels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_pick_challenge);
        ButterKnife.bind(this);
        appComponent().inject(this);

        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        challengeViewModels = new ArrayList<>();
        challengeViewModels.add(new PickChallengeViewModel("Weight Cutter", "Start shedding some weight and feel great", R.drawable.challenge_01, Category.WELLNESS));
        challengeViewModels.add(new PickChallengeViewModel("Stress-Free Mind", "Be mindful and stay in the flow longer", R.drawable.challenge_02, Category.WELLNESS));
        challengeViewModels.add(new PickChallengeViewModel("Healthy & Fit", "Keep working out and live healthier life", R.drawable.challenge_03, Category.WELLNESS));
        challengeViewModels.add(new PickChallengeViewModel("English Jedi", "Advance your English skills", R.drawable.challenge_04, Category.LEARNING));
        challengeViewModels.add(new PickChallengeViewModel("Programming Ninja", "Learn the fundamentals of computer programming", R.drawable.challenge_05, Category.LEARNING));
        challengeViewModels.add(new PickChallengeViewModel("Master presenter", "Learn how to create and present effectively", R.drawable.challenge_06, Category.WORK));
        challengeViewModels.add(new PickChallengeViewModel("Famous writer", "Learn how to become great writer & blogger", R.drawable.challenge_07, Category.WORK));

        setBackgroundColors(challengeViewModels.get(0).getCategory());

        PickChallengeAdapter pickChallengeAdapter = new PickChallengeAdapter(this, challengeViewModels, eventBus);
        viewPager.setAdapter(pickChallengeAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                setBackgroundColors(challengeViewModels.get(viewPager.getRealItem()).getCategory());
            }
        });
    }

    private void setBackgroundColors(Category category) {
        rootContainer.setBackgroundColor(ContextCompat.getColor(this, category.color500));
        appBar.setBackgroundColor(ContextCompat.getColor(this, category.color500));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, category.color500));
        getWindow().setStatusBarColor(ContextCompat.getColor(this, category.color700));
    }
}

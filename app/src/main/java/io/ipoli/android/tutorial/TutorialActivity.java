package io.ipoli.android.tutorial;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.WindowManager;

import com.github.paolorotolo.appintro.AppIntro2;

import java.util.ArrayList;

import io.ipoli.android.R;

public class TutorialActivity extends AppIntro2 {
    private int[] colors = new int[]{R.color.md_blue_500, R.color.md_green_500, R.color.md_orange_500,
    R.color.md_deep_purple_500, R.color.md_teal_500};


    @Override
    public void init(@Nullable Bundle savedInstanceState) {
        getWindow().setNavigationBarColor(Color.BLACK);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        addSlide(TutorialFragment.newInstance("Title", "Description", R.drawable.avatar_01, Color.TRANSPARENT));
        addSlide(TutorialFragment.newInstance("Title", "Description", R.drawable.avatar_02, Color.TRANSPARENT));
        addSlide(TutorialFragment.newInstance("Title", "Description", R.drawable.avatar_03, Color.TRANSPARENT));
        addSlide(TutorialFragment.newInstance("Title", "Description", R.drawable.avatar_04, Color.TRANSPARENT));
        addSlide(TutorialFragment.newInstance("Title", "Description", R.drawable.avatar_05, Color.TRANSPARENT));

        ArrayList<Integer> c = new ArrayList<>();
        for(int color : colors) {
            c.add(ContextCompat.getColor(this, color));
        }

        setAnimationColors(c);
    }

    @Override
    public void onDonePressed() {
        finish();
    }

    @Override
    public void onNextPressed() {

    }

    @Override
    public void onSlideChanged() {
    }

}

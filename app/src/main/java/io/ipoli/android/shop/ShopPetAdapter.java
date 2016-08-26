package io.ipoli.android.shop;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;

import io.ipoli.android.R;
import io.ipoli.android.shop.viewmodels.PetViewModel;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/26/16.
 */
public class ShopPetAdapter extends PagerAdapter {

    private final LayoutInflater layoutInflater;

    public ShopPetAdapter(final Context context, List<PetViewModel> viewModels) {
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public Object instantiateItem(final ViewGroup container, final int position) {
        final View view = layoutInflater.inflate(R.layout.shop_pet_item, container, false);
        ImageView petPicture = (ImageView) view.findViewById(R.id.pet_picture);
        ImageView petStatePicture = (ImageView) view.findViewById(R.id.pet_picture_state);
        if (position == 0) {
            petPicture.setImageResource(R.drawable.pet_1);
            petStatePicture.setImageResource(R.drawable.pet_1_happy);
        } else if (position == 1) {
            petPicture.setImageResource(R.drawable.pet_2);
            petStatePicture.setImageResource(R.drawable.pet_2_happy);
        } else {
            petPicture.setImageResource(R.drawable.pet_3);
            petStatePicture.setImageResource(R.drawable.pet_3_happy);
        }
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

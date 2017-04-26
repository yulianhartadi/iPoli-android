package io.ipoli.android.shop.adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.otto.Bus;

import java.util.List;

import io.ipoli.android.R;
import io.ipoli.android.shop.events.BuyPetRequestEvent;
import io.ipoli.android.shop.viewmodels.PetViewModel;
import mehdi.sakout.fancybuttons.FancyButton;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/26/16.
 */
public class ShopPetAdapter extends PagerAdapter {

    private final LayoutInflater layoutInflater;
    private final List<PetViewModel> viewModels;
    private final Bus eventBus;
    private Context context;

    public ShopPetAdapter(final Context context, List<PetViewModel> viewModels, Bus eventBus) {
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
        final View view = layoutInflater.inflate(R.layout.shop_pet_item, container, false);
        PetViewModel vm = viewModels.get(position);

        TextView petDescription = (TextView) view.findViewById(R.id.pet_description);
        ImageView petPicture = (ImageView) view.findViewById(R.id.pet_picture);
        ImageView petStatePicture = (ImageView) view.findViewById(R.id.pet_picture_state);
        FancyButton petPrice = (FancyButton) view.findViewById(R.id.pet_price);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) petPrice.getIconImageObject().getLayoutParams();
        params.gravity = Gravity.CENTER_VERTICAL;

        petDescription.setText(vm.getDescription());

        petPicture.setImageDrawable(context.getDrawable(vm.getPicture()));

        petStatePicture.setImageDrawable(context.getDrawable(vm.getPictureState()));
        petPrice.setText(vm.getPrice() + "");

        petPrice.setOnClickListener(v -> eventBus.post(new BuyPetRequestEvent(vm)));

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

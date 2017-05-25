package io.ipoli.android.store.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.BaseFragment;
import io.ipoli.android.app.utils.ResourceUtils;
import io.ipoli.android.store.adapters.PetStoreAdapter;
import io.ipoli.android.store.viewmodels.PetViewModel;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/25/17.
 */

public class PetStoreFragment extends BaseFragment {

    @Inject
    Bus eventBus;

    @BindView(R.id.pet_list)
    RecyclerView petList;

    private Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragement_pet_store, container, false);
        unbinder = ButterKnife.bind(this, view);
        App.getAppComponent(getContext()).inject(this);
        getActivity().setTitle("Buy pet");

        String[] descriptions = getResources().getStringArray(R.array.pet_names);
        petList.setLayoutManager(new GridLayoutManager(getContext(), 2));
        List<PetViewModel> petViewModels = new ArrayList<>();
        for (int i = 0; i < descriptions.length; i++) {
            int petIndex = i + 1;
            String pictureName = "pet_" + petIndex;
            if (getPlayer().getPet().getPicture().equals(pictureName)) {
                continue;
            }
            petViewModels.add(new PetViewModel(descriptions[i], 500,
                    ResourceUtils.extractDrawableResource(getContext(), pictureName),
                    ResourceUtils.extractDrawableResource(getContext(), pictureName + "_happy"), pictureName));
        }
        PetStoreAdapter adapter = new PetStoreAdapter(getContext(), eventBus, petViewModels);
        petList.setAdapter(adapter);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        eventBus.register(this);
    }

    @Override
    public void onPause() {
        eventBus.unregister(this);
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
        super.onDestroyView();
    }

    @Override
    protected boolean useOptionsMenu() {
        return false;
    }
}

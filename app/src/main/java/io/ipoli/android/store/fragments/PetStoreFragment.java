package io.ipoli.android.store.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.BaseFragment;
import io.ipoli.android.app.utils.ResourceUtils;
import io.ipoli.android.pet.data.Pet;
import io.ipoli.android.player.Player;
import io.ipoli.android.player.persistence.PlayerPersistenceService;
import io.ipoli.android.store.adapters.PetStoreAdapter;
import io.ipoli.android.store.events.BuyPetRequestEvent;
import io.ipoli.android.store.events.PetBoughtEvent;
import io.ipoli.android.store.viewmodels.PetViewModel;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/25/17.
 */

public class PetStoreFragment extends BaseFragment {

    public static final int PET_PRICE = 500;
    @Inject
    Bus eventBus;

    @Inject
    PlayerPersistenceService playerPersistenceService;

    @BindView(R.id.pet_list)
    RecyclerView petList;

    private Unbinder unbinder;
    private PetStoreAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragement_pet_store, container, false);
        unbinder = ButterKnife.bind(this, view);
        App.getAppComponent(getContext()).inject(this);
        getActivity().setTitle("Buy pet");

        petList.setLayoutManager(new GridLayoutManager(getContext(), 2));
        List<PetViewModel> petViewModels = createPetViewModels();
        adapter = new PetStoreAdapter(getContext(), eventBus, petViewModels);
        petList.setAdapter(adapter);
        return view;
    }

    @NonNull
    private List<PetViewModel> createPetViewModels() {
        String[] descriptions = getResources().getStringArray(R.array.pet_names);
        List<PetViewModel> petViewModels = new ArrayList<>();
        for (int i = 0; i < descriptions.length; i++) {
            int petIndex = i + 1;
            String pictureName = "pet_" + petIndex;
            if (getPlayer().getPet().getPicture().equals(pictureName)) {
                continue;
            }
            petViewModels.add(new PetViewModel(descriptions[i], PET_PRICE,
                    ResourceUtils.extractDrawableResource(getContext(), pictureName),
                    ResourceUtils.extractDrawableResource(getContext(), pictureName + "_happy"), pictureName));
        }
        return petViewModels;
    }

    @Subscribe
    public void onBuyPetRequest(BuyPetRequestEvent e) {
        Player player = getPlayer();
        PetViewModel vm = e.petViewModel;
        if (player.getCoins() < vm.getPrice()) {
            Toast.makeText(getContext(), R.string.not_enough_coins_to_buy_pet, Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setIcon(R.drawable.logo)
                .setTitle(R.string.buy_pet_confirm_title)
                .setMessage(R.string.buy_pet_confirm_message)
                .setPositiveButton(getString(R.string.help_dialog_ok), (dialog, which) -> {
                    eventBus.post(new PetBoughtEvent(e.petViewModel));
                    player.removeCoins(vm.getPrice());
                    Pet pet = player.getPet();
                    pet.setPicture(vm.getPictureName());
                    pet.setHealthPointsPercentage(Constants.DEFAULT_PET_HP);
                    playerPersistenceService.save(player);
                    adapter.setViewModels(createPetViewModels());
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {

                })
                .show();
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

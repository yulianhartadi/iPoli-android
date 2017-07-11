package io.ipoli.android.store.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.BaseFragment;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.events.ScreenShownEvent;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.ResourceUtils;
import io.ipoli.android.player.data.PetAvatar;
import io.ipoli.android.player.data.Player;
import io.ipoli.android.player.persistence.PlayerPersistenceService;
import io.ipoli.android.store.activities.StoreActivity;
import io.ipoli.android.store.adapters.PetStoreAdapter;
import io.ipoli.android.store.events.BuyPetRequestEvent;
import io.ipoli.android.store.events.PetBoughtEvent;
import io.ipoli.android.store.events.UsePetEvent;
import io.ipoli.android.store.viewmodels.PetViewModel;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/25/17.
 */

public class PetStoreFragment extends BaseFragment {

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
        ((StoreActivity) getActivity()).populateTitle(R.string.fragment_pet_store_title);

        petList.setLayoutManager(new GridLayoutManager(getContext(), 2));
        List<PetViewModel> petViewModels = createPetViewModels();
        adapter = new PetStoreAdapter(getContext(), eventBus, petViewModels);
        petList.setAdapter(adapter);

        eventBus.post(new ScreenShownEvent(getActivity(), EventSource.STORE_PETS));

        return view;
    }

    @NonNull
    private List<PetViewModel> createPetViewModels() {
        Player player = getPlayer();
        PetAvatar currentPet = player.getPet().getCurrentAvatar();
        Map<Integer, Long> playerPets = player.getInventory().getPets();
        List<PetViewModel> petViewModels = new ArrayList<>();
        List<PetAvatar> boughtPetAvatars = new ArrayList<>();
        List<PetAvatar> lockedPetAvatars = new ArrayList<>();
        for (PetAvatar petAvatar : PetAvatar.values()) {
            if (playerPets.containsKey(petAvatar.code)) {
                boughtPetAvatars.add(petAvatar);
            } else {
                lockedPetAvatars.add(petAvatar);
            }
        }

        Collections.sort(boughtPetAvatars, ((p1, p2) -> -Long.compare(playerPets.get(p1.code), playerPets.get(p2.code))));

        for (PetAvatar petAvatar : boughtPetAvatars) {
            boolean isCurrent = currentPet.equals(petAvatar);
            petViewModels.add(createViewModel(petAvatar, DateUtils.fromMillis(playerPets.get(petAvatar.code)), isCurrent));
        }

        for (PetAvatar petAvatar : lockedPetAvatars) {
            petViewModels.add(createViewModel(petAvatar));
        }

        return petViewModels;
    }

    private PetViewModel createViewModel(PetAvatar petAvatar) {
        return createViewModel(petAvatar, null, false);
    }

    private PetViewModel createViewModel(PetAvatar petAvatar, LocalDate boughtDate, boolean isCurrent) {
        String pictureName = getContext().getResources().getResourceEntryName(petAvatar.picture);
        int pictureState = ResourceUtils.extractDrawableResource(getContext(), pictureName + "_happy");
        return new PetViewModel(getContext(), petAvatar, pictureState, boughtDate, isCurrent);
    }

    @Subscribe
    public void onBuyPetRequest(BuyPetRequestEvent e) {
        Player player = getPlayer();
        PetAvatar petAvatar = e.petAvatar;
        if (player.getCoins() < petAvatar.price) {
            Toast.makeText(getContext(), R.string.not_enough_coins_to_buy_pet, Toast.LENGTH_SHORT).show();
            return;
        }
        eventBus.post(new PetBoughtEvent(e.petAvatar));
        player.removeCoins(petAvatar.price);
        player.getInventory().addPet(petAvatar, LocalDate.now());
        playerPersistenceService.save(player);
        adapter.setViewModels(createPetViewModels());
    }

    @Subscribe
    public void onUsePet(UsePetEvent e) {
        Player player = getPlayer();
        io.ipoli.android.pet.data.Pet pet = player.getPet();
        pet.setAvatarCode(e.petAvatar.code);
        playerPersistenceService.save(player);
        Toast.makeText(getContext(), R.string.pet_selected_message, Toast.LENGTH_SHORT).show();
        adapter.setViewModels(createPetViewModels());
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

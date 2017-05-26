package io.ipoli.android.store.fragments;

import android.os.Bundle;
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
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.BaseFragment;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.player.Player;
import io.ipoli.android.player.events.AvatarPickedEvent;
import io.ipoli.android.player.persistence.PlayerPersistenceService;
import io.ipoli.android.store.Avatar;
import io.ipoli.android.store.adapters.AvatarStoreAdapter;
import io.ipoli.android.store.events.BuyAvatarRequestEvent;
import io.ipoli.android.store.events.UseAvatarEvent;
import io.ipoli.android.store.viewmodels.AvatarViewModel;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/25/17.
 */

public class AvatarStoreFragment extends BaseFragment {

    @Inject
    Bus eventBus;

    @Inject
    PlayerPersistenceService playerPersistenceService;

    @BindView(R.id.avatar_list)
    RecyclerView avatarList;

    private Unbinder unbinder;
    private AvatarStoreAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragement_avatar_store, container, false);
        unbinder = ButterKnife.bind(this, view);
        App.getAppComponent(getContext()).inject(this);
        getActivity().setTitle("Buy avatar");

        avatarList.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new AvatarStoreAdapter(getContext(), eventBus, createAvatarViewModels());
        avatarList.setAdapter(adapter);
        return view;
    }

    private List<AvatarViewModel> createAvatarViewModels() {
        Map<Integer, Long> playerAvatars = getPlayer().getInventory().getAvatars();
        List<AvatarViewModel> avatarViewModels = new ArrayList<>();
        List<Avatar> boughtAvatars = new ArrayList<>();
        List<Avatar> lockedAvatars = new ArrayList<>();

        for (Avatar avatar : Avatar.values()) {
            if (playerAvatars.containsKey(avatar.code)) {
                boughtAvatars.add(avatar);
            } else {
                lockedAvatars.add(avatar);
            }
        }

        boughtAvatars.sort((a1, a2) -> -Long.compare(playerAvatars.get(a1.code), playerAvatars.get(a2.code)));
        for (Avatar avatar : boughtAvatars) {
            avatarViewModels.add(new AvatarViewModel(getContext(), avatar, DateUtils.fromMillis(playerAvatars.get(avatar.code))));
        }
        for (Avatar avatar : lockedAvatars) {
            avatarViewModels.add(new AvatarViewModel(getContext(), avatar));
        }
        return avatarViewModels;
    }

    @Subscribe
    public void onBuyAvatarRequest(BuyAvatarRequestEvent e) {
        Player player = getPlayer();
        Avatar avatar = e.avatar;
        if (player.getCoins() < avatar.price) {
            Toast.makeText(getContext(), R.string.not_enough_coins_to_buy_avatar, Toast.LENGTH_SHORT).show();
            return;
        }
        eventBus.post(new AvatarPickedEvent(avatar.name()));
        player.removeCoins(avatar.price);
        player.getInventory().addAvatar(avatar, LocalDate.now());
        playerPersistenceService.save(player);
        adapter.setViewModels(createAvatarViewModels());
    }

    @Subscribe
    public void onUseAvatar(UseAvatarEvent e) {
        Player player = getPlayer();
        player.setPicture(getContext().getResources().getResourceEntryName(e.avatar.picture));
        playerPersistenceService.save(player);
        Toast.makeText(getContext(), R.string.avatar_selected_message, Toast.LENGTH_SHORT).show();
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

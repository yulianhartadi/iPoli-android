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
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.BaseFragment;
import io.ipoli.android.app.utils.ResourceUtils;
import io.ipoli.android.player.persistence.PlayerPersistenceService;
import io.ipoli.android.store.adapters.AvatarStoreAdapter;
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
        getActivity().setTitle("Buy pet");

        avatarList.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new AvatarStoreAdapter(getContext(), eventBus, createAvatarViewModels());
        avatarList.setAdapter(adapter);
        return view;
    }

    private List<AvatarViewModel> createAvatarViewModels() {
        List<AvatarViewModel> avatarViewModels = new ArrayList<>();
        for (int i = Constants.AVATAR_COUNT; i >= 1; i--) {
            String pictureName = String.format(Locale.getDefault(), "avatar_%02d", i);
            avatarViewModels.add(new AvatarViewModel("Funky", 300,
                    ResourceUtils.extractDrawableResource(getContext(), pictureName), pictureName));
        }
        return avatarViewModels;
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

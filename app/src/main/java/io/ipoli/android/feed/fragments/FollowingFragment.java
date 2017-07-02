package io.ipoli.android.feed.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.BaseFragment;
import io.ipoli.android.app.ui.EmptyStateRecyclerView;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.feed.data.Profile;
import io.ipoli.android.feed.ui.ProfileBinder;
import io.ipoli.android.feed.ui.ProfileViewHolder;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/2/17.
 */
public class FollowingFragment extends BaseFragment {

    private static final String PLAYER_ID = "player_id";
    private String playerId;
    private FirebaseRecyclerAdapter<Profile, ProfileViewHolder> adapter;

    public static FollowersFragment newInstance(String playerId) {
        FollowersFragment fragment = new FollowersFragment();
        Bundle args = new Bundle();
        args.putString(PLAYER_ID, playerId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() == null || StringUtils.isEmpty(getArguments().getString(PLAYER_ID))) {
            throw new IllegalArgumentException("Player id is required");
        }
        playerId = getArguments().getString(PLAYER_ID);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        App.getAppComponent(getContext()).inject(this);
        super.onCreateView(inflater, container, savedInstanceState);
        EmptyStateRecyclerView postList = (EmptyStateRecyclerView) inflater.inflate(R.layout.fragment_profile_list, container, false);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        postList.setLayoutManager(layoutManager);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("/profiles");

        adapter = new FirebaseRecyclerAdapter<Profile, ProfileViewHolder>(Profile.class,
                R.layout.follower_item,
                ProfileViewHolder.class,
                ref.orderByChild("followers/" + playerId).equalTo(true).limitToLast(100)) {
            @Override
            protected void populateViewHolder(ProfileViewHolder profileViewHolder, Profile profile, int position) {
                ProfileBinder.bind(profileViewHolder, profile, playerId);
            }
        };

        postList.setAdapter(adapter);
        return postList;
    }

    @Override
    public void onDestroyView() {
        adapter.cleanup();
        super.onDestroyView();
    }
    @Override
    protected boolean useOptionsMenu() {
        return false;
    }
}

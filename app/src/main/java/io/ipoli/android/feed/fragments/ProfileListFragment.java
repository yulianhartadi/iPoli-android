package io.ipoli.android.feed.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;

import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.BaseFragment;
import io.ipoli.android.app.ui.EmptyStateRecyclerView;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.feed.data.Profile;
import io.ipoli.android.feed.events.FollowPlayerEvent;
import io.ipoli.android.feed.events.ShowProfileEvent;
import io.ipoli.android.feed.events.UnfollowPlayerEvent;
import io.ipoli.android.feed.ui.ProfileListBinder;
import io.ipoli.android.feed.ui.ProfileListViewHolder;

import static io.ipoli.android.feed.persistence.FirebaseFeedPersistenceService.profilesPath;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/2/17.
 */
public class ProfileListFragment extends BaseFragment {

    private static final String PLAYER_ID = "player_id";
    private static final String PROFILE_LIST_TYPE = "profile_list_type";

    public static final String LIST_TYPE_FOLLOWERS = "following";
    public static final String LIST_TYPE_FOLLOWING = "followers";

    private String playerId;
    private FirebaseRecyclerAdapter<Profile, ProfileListViewHolder> adapter;
    private String listType;

    public static ProfileListFragment newInstance(String playerId, String listType) {
        ProfileListFragment fragment = new ProfileListFragment();
        Bundle args = new Bundle();
        args.putString(PLAYER_ID, playerId);
        args.putString(PROFILE_LIST_TYPE, listType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments == null) {
            throw new IllegalArgumentException("Player id and list type are required");
        }
        if (StringUtils.isEmpty(arguments.getString(PLAYER_ID))) {
            throw new IllegalArgumentException("Player id is required");
        }
        if (StringUtils.isEmpty(arguments.getString(PROFILE_LIST_TYPE))) {
            throw new IllegalArgumentException("Player id is required");
        }

        playerId = arguments.getString(PLAYER_ID);
        listType = arguments.getString(PROFILE_LIST_TYPE);

        if (!listType.equals(LIST_TYPE_FOLLOWERS) && !listType.equals(LIST_TYPE_FOLLOWING)) {
            throw new IllegalArgumentException("Unknown list type " + listType);
        }
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

        DatabaseReference profilesPath = profilesPath().toReference();
        adapter = new FirebaseRecyclerAdapter<Profile, ProfileListViewHolder>(Profile.class,
                R.layout.follower_item,
                ProfileListViewHolder.class,
                profilesPath.orderByChild(listType + "/" + playerId).equalTo(true).limitToLast(100)) {
            @Override
            protected void populateViewHolder(ProfileListViewHolder holder, Profile profile, int position) {
                ProfileListBinder.bind(holder, profile, getPlayerId());
                holder.itemView.setOnClickListener(v -> postEvent(new ShowProfileEvent(profile.getId())));
                holder.follow.setEnabled(true);
                holder.follow.setOnClickListener(v -> {
                    v.setEnabled(false);
                    postEvent(new FollowPlayerEvent(profile));
                });
                holder.following.setEnabled(true);
                holder.following.setOnClickListener(v -> {
                    v.setEnabled(false);
                    postEvent(new UnfollowPlayerEvent(profile));
                });
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
}
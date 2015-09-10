package com.curiousily.ipoli.user.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.curiousily.ipoli.Constants;
import com.curiousily.ipoli.app.api.APIClient;
import com.curiousily.ipoli.app.api.AsyncAPICallback;
import com.curiousily.ipoli.user.User;
import com.curiousily.ipoli.user.api.request.CreateUserRequest;
import com.curiousily.ipoli.user.events.LoadUserEvent;
import com.curiousily.ipoli.user.events.UserLoadedEvent;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.ads.identifier.AdvertisingIdClient.Info;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/31/15.
 */
public class UserStorageService {

    private final APIClient client;
    private final Bus bus;
    private final Context context;

    public UserStorageService(APIClient client, Bus bus, Context context) {
        this.client = client;
        this.bus = bus;
        this.context = context;
    }

    private void saveUser(CreateUserRequest request) {
        client.createUser(request, new AsyncAPICallback<User>() {
            @Override
            public void success(User user, Response response) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(Constants.KEY_USER_ID, user.id);
                editor.apply();
                postUserLoadedEvent(user.id);
            }
        });
    }

    @Subscribe
    public void onLoadUser(LoadUserEvent e) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (preferences.contains(Constants.KEY_USER_ID)) {
            String userId = preferences.getString(Constants.KEY_USER_ID, "");
            postUserLoadedEvent(userId);
        } else {
            GetAdvertisingIdTask task = new GetAdvertisingIdTask(context, new AdvertisingIdListener() {
                @Override
                public void onAdvertisingIdReady(String advertisingId, boolean isLimitAdTrackingEnabled) {
                    saveUser(new CreateUserRequest(advertisingId));
                }

                @Override
                public void onAdvertisingIdError(Throwable error) {

                }
            });
            task.execute();
        }
    }

    private void postUserLoadedEvent(String userId) {
        User user = new User(userId);
        bus.post(new UserLoadedEvent(user));
    }

    public interface AdvertisingIdListener {
        void onAdvertisingIdReady(String advertisingId, boolean isLimitAdTrackingEnabled);

        void onAdvertisingIdError(Throwable error);
    }

    private static class GetAdvertisingIdTask extends AsyncTask<Void, Void, AdvertisingIdClient.Info> {

        private final AdvertisingIdListener listener;
        private final Context context;
        private Throwable error;

        public GetAdvertisingIdTask(Context context, AdvertisingIdListener listener) {
            this.listener = listener;
            this.context = context;
        }

        @Override
        protected Info doInBackground(Void... _) {
            Info adInfo = null;
            try {
                adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context);
            } catch (Exception e) {
                error = e;
            }
            return adInfo;
        }

        @Override
        protected void onPostExecute(Info adInfo) {
            if (error != null) {
                listener.onAdvertisingIdError(error);
            } else {
                listener.onAdvertisingIdReady(adInfo.getId(), adInfo.isLimitAdTrackingEnabled());
            }
        }
    }

}
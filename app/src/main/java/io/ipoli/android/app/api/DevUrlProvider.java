package io.ipoli.android.app.api;

import java.net.URL;

import io.ipoli.android.ApiConstants;

import static io.ipoli.android.app.api.UrlProvider.getURL;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/30/17.
 */
public class DevUrlProvider implements UrlProvider {

    @Override
    public URL sync() {
        return getURL(ApiConstants.DEV_SYNC_URL);
    }

    @Override
    public URL api() {
        return getURL(ApiConstants.DEV_API_URL);
    }

    @Override
    public URL createUser() {
        return getURL(ApiConstants.DEV_API_URL + "users/");
    }

    @Override
    public URL migrateUser(String firebasePlayerId) {
        return getURL(ApiConstants.DEV_API_URL + "migrations/" + firebasePlayerId);
    }


}

package io.ipoli.android.app.api;

import java.net.MalformedURLException;
import java.net.URL;

import io.ipoli.android.ApiConstants;

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
    public URL getMembershipStatus() {
        return getURL(ApiConstants.DEV_API_URL + "subscriptions/");
    }

    private static URL getURL(String path) {
        try {
            return new URL(path);
        } catch (MalformedURLException e) {
            return null;
        }
    }
}

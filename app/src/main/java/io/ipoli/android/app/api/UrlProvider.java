package io.ipoli.android.app.api;

import java.net.URL;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/30/17.
 */

public interface UrlProvider {
    URL sync();

    URL api();

    URL createUser();

    URL migrateUser(String firebasePlayerId);
}

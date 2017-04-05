package io.ipoli.android.app.modules;

import android.content.Context;
import android.util.Log;

import com.couchbase.lite.Database;
import com.couchbase.lite.Manager;
import com.couchbase.lite.android.AndroidContext;
import com.google.firebase.crash.FirebaseCrash;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.ipoli.android.BuildConfig;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/4/17.
 */
@Module
public class CouchbaseModule {

    @Provides
    @Singleton
    public Database provideDatabase(Context context) {
        try {
            Manager manager = new Manager(new AndroidContext(context), Manager.DEFAULT_OPTIONS);
            if (BuildConfig.DEBUG) {
                Manager.enableLogging("CouchDb", Log.VERBOSE);
            }
            return manager.getDatabase("ipoli_db");
        } catch (Exception e) {
            e.printStackTrace();
            FirebaseCrash.report(new RuntimeException("Unable to create/get database", e));
            return null;
        }
    }
}

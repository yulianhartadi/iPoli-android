package io.ipoli.android.app.modules;

import android.content.Context;
import android.util.Log;

import com.couchbase.lite.Database;
import com.couchbase.lite.Manager;
import com.couchbase.lite.android.AndroidContext;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

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
            Manager.enableLogging("CouchDb", Log.VERBOSE);
            return manager.getDatabase("ipoli_db");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

package io.ipoli.android.app.sync;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/23/16.
 */
public interface Remotable<T> {
    void setRemoteId(String remoteId);

    String getRemoteId();

    void setNeedsSync();

    boolean needsSyncWithRemote();

    void updateLocal(T remoteObject);

    T updateRemote();

    void setSyncedWithRemote();
}
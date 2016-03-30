package io.ipoli.android.app.net;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/23/16.
 */
public interface RemoteObject<T> {
    void setRemoteId(String remoteId);

    String getRemoteId();

    void setNeedsSync();

    boolean needsSyncWithRemote();

    void setSyncedWithRemote();

    void markUpdated();
}
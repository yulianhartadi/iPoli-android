package io.ipoli.android.app.net;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/23/16.
 */
public interface RemoteObject<T> {

    void setId(String id);

    String getId();

    void setNeedsSync();

    boolean needsSyncWithRemote();

    void setSyncedWithRemote();

    void markUpdated();

    void setRemoteObject();

    boolean isRemoteObject();
}
package io.ipoli.android.app.net;

import java.util.Date;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/23/16.
 */
public interface RemoteObject<T> {

    void setId(String id);

    String getId();

    void markUpdated();

    Date getCreatedAt();
}
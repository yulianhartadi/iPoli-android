package io.ipoli.android.avatar.persistence;

import io.ipoli.android.app.persistence.PersistenceService;
import io.ipoli.android.avatar.Avatar;
import io.ipoli.android.quest.persistence.OnDataChangedListener;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/10/16.
 */
public interface AvatarPersistenceService extends PersistenceService<Avatar> {

    void find(OnDataChangedListener<Avatar> listener);

    void listen(OnDataChangedListener<Avatar> listener);
}
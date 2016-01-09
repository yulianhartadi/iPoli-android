package io.ipoli.android.assistant.persistence;

import io.ipoli.android.assistant.Assistant;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/9/16.
 */
public interface AssistantPersistenceService {
    Assistant save(Assistant assistant);
    Assistant find();
}

package io.ipoli.android.chat.persistence;

import java.util.List;

import io.ipoli.android.chat.Message;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/10/16.
 */
public interface MessagePersistenceService {

    Message save(Message message);

    List<Message> findAll();
}

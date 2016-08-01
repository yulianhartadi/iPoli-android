package io.ipoli.android.app.services.readers;

import java.util.List;

import me.everything.providers.android.calendar.Event;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/11/16.
 */
public interface AndroidCalendarListPersistenceService<T> {

    void save(List<Event> events);
}

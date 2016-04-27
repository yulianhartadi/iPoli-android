package io.ipoli.android.app.scheduling;

import java.util.List;

import io.ipoli.android.app.scheduling.dto.FindSlotsRequest;
import io.ipoli.android.app.scheduling.dto.Slot;
import retrofit2.http.Body;
import retrofit2.http.POST;
import rx.Observable;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/27/16.
 */
public interface SchedulingAPIService {

    @POST("slots")
    Observable<List<Slot>> findSlots(@Body FindSlotsRequest findSlotsRequest);
}

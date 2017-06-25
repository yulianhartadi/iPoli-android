package io.ipoli.android.app.sync;

public class InstanceData {

    public final long eventId;
    public final int startMinute;
    public final long begin;
    public final long end;

    public InstanceData(long eventId, int startMinute, long begin, long end) {
        this.eventId = eventId;
        this.startMinute = startMinute;
        this.begin = begin;
        this.end = end;
    }
}
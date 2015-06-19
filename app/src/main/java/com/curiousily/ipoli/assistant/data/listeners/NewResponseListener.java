package com.curiousily.ipoli.assistant.data.listeners;

import android.util.Log;

import com.curiousily.ipoli.EventBus;
import com.curiousily.ipoli.assistant.data.events.NewResponseEvent;
import com.curiousily.ipoli.assistant.data.models.Response;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;

import java.util.Map;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/19/15.
 */
public class NewResponseListener implements ChildEventListener {

    private final static long START_TIME = System.currentTimeMillis();

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        @SuppressWarnings("unchecked")
        Map<String, Object> v = (Map<String, Object>) dataSnapshot.getValue();
        long createdAt = (long) v.get("created_at");
        if (createdAt >= START_TIME) {
            Response r = new Response();
            r.intent = (String) v.get("intent");
            r.userId = (String) v.get("user_id");
            r.text = (String) v.get("text");
            Log.d("PoliVoice", r.intent);
            EventBus.get().post(new NewResponseEvent(r));
        }
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {

    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onCancelled(FirebaseError firebaseError) {

    }
}

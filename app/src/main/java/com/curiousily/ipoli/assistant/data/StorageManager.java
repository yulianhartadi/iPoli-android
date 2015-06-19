package com.curiousily.ipoli.assistant.data;

import com.curiousily.ipoli.FirebaseConstants;
import com.curiousily.ipoli.assistant.data.listeners.NewResponseListener;
import com.curiousily.ipoli.auth.FirebaseUserAuthenticator;
import com.firebase.client.Firebase;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/17/15.
 */
public class StorageManager {
    private final Firebase firebase = new Firebase(FirebaseConstants.URL);

    public StorageManager() {
        firebase.child("responses").orderByChild("user_id").equalTo(FirebaseUserAuthenticator.getUser().id).addChildEventListener(new NewResponseListener());
    }

    public void save(String name, Object object) {
        firebase.child(name).push().setValue(object);
    }
}

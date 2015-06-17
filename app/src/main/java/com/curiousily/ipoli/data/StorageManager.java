package com.curiousily.ipoli.data;

import com.curiousily.ipoli.FirebaseConstants;
import com.firebase.client.Firebase;

import java.util.HashMap;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/17/15.
 */
public class StorageManager {
    private final Firebase firebase = new Firebase(FirebaseConstants.URL);

    public void save(String name, Object object) {
        firebase.child(name).push().setValue(object);
    }
}

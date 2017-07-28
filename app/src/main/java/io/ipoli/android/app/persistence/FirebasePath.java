package io.ipoli.android.app.persistence;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Map;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/4/17.
 */
public class FirebasePath {

    private String path;

    public class AddMapEntry {

        private final Map<String, Object> map;
        private final String path;

        private AddMapEntry(Map<String, Object> map, String path) {
            this.map = map;
            this.path = path;
        }

        public void withValue(Object value) {
            map.put(path, value);
        }
    }

    public FirebasePath(String initialPath) {
        this.path = initialPath;
    }

    public FirebasePath add(String pathSegment) {
        return new FirebasePath(path + "/" + pathSegment);
    }

    public FirebasePath add(Integer pathSegment) {
        return new FirebasePath(path + "/" + pathSegment);
    }

    @Override
    public String toString() {
        return path;
    }

    public DatabaseReference toReference(FirebaseDatabase database) {
        return database.getReference(path);
    }

    public DatabaseReference toReference() {
        return FirebaseDatabase.getInstance().getReference(path);
    }

    public AddMapEntry update(Map<String, Object> map) {
        return new AddMapEntry(map, path);
    }
}

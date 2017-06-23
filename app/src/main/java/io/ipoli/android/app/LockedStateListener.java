package io.ipoli.android.app;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/31/17.
 */

public interface LockedStateListener {
    boolean isLocked();

    void onLockedAction();
}

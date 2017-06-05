package io.ipoli.android.app.ui.typewriter;

import android.os.Handler;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/5/17.
 */
abstract class Repeater implements Runnable {
    protected Handler mHandler = new Handler();
    private Runnable mDoneRunnable;
    private long mDelay;

    public Repeater(Runnable doneRunnable, long delay) {
        mDoneRunnable = doneRunnable;
        mDelay = delay;
    }

    protected void done() {
        mDoneRunnable.run();
    }

    protected void delayAndRepeat() {
        mHandler.postDelayed(this, mDelay);
    }
}

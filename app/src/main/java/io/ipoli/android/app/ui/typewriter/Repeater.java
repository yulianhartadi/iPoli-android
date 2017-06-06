package io.ipoli.android.app.ui.typewriter;

import android.os.Handler;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/5/17.
 */
abstract class Repeater implements Runnable {

    private boolean isRunning;
    private Handler handler = new Handler();
    private Runnable doneRunnable;
    private long delay;

    public Repeater(Runnable doneRunnable, long delay) {
        this.doneRunnable = doneRunnable;
        this.delay = delay;
        this.isRunning = true;
    }

    protected void done() {
        doneRunnable.run();
    }

    protected void delayAndRepeat() {
        if (!isRunning) {
            return;
        }
        handler.postDelayed(this, delay);
    }

    public void stop() {
        isRunning = false;
    }
}

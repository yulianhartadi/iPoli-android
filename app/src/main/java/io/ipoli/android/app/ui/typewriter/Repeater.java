package io.ipoli.android.app.ui.typewriter;

import android.os.Handler;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/5/17.
 */
abstract class Repeater implements Runnable {
    protected Handler handler = new Handler();
    private Runnable doneRunnable;
    private long delay;

    public Repeater(Runnable doneRunnable, long delay) {
        this.doneRunnable = doneRunnable;
        this.delay = delay;
    }

    protected void done() {
        doneRunnable.run();
    }

    protected void delayAndRepeat() {
        handler.postDelayed(this, delay);
    }
}

package io.ipoli.android.app.ui.typewriter;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/5/17.
 */
class TypePauser extends Repeater {

    private boolean isPaused = false;

    public TypePauser(long delay, Runnable doneRunnable) {
        super(doneRunnable, delay);
    }

    @Override
    public void run() {
        if (isPaused) {
            done();
            return;
        }

        isPaused = true;
        delayAndRepeat();
    }
}

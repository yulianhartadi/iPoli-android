package io.ipoli.android.app.ui.typewriter;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/5/17.
 */
class TypeRunnable extends Repeater {

    Runnable runnable;

    public TypeRunnable(Runnable runnable, Runnable doneRunnable) {
        super(doneRunnable, 0);

        this.runnable = runnable;
    }

    @Override
    public void run() {
        runnable.run();
        done();
    }
}

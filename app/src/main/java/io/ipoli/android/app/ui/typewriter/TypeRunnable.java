package io.ipoli.android.app.ui.typewriter;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/5/17.
 */
class TypeRunnable extends Repeater {

    Runnable mRunnable;

    public TypeRunnable(Runnable runnable, Runnable doneRunnable) {
        super(doneRunnable, 0);

        mRunnable = runnable;
    }

    @Override
    public void run() {
        mRunnable.run();
        done();
    }
}

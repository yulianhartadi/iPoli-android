package io.ipoli.android.app.ui;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatEditText;
import android.text.InputType;
import android.util.AttributeSet;

import java.util.LinkedList;
import java.util.Queue;

public class TypewriterView extends AppCompatEditText {

    public static final int TYPE_SPEED = 50;
    public static final int DELETE_SPEED = 40;
    public static final int PAUSE_DELAY = 1000;

    private boolean isRunning = false;

    private Queue<Repeater> mRunnableQueue = new LinkedList<>();

    private Runnable mRunNextRunnable = this::runNext;

    public TypewriterView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setBackgroundColor(Color.TRANSPARENT);
        setCursorAtEnd();
        setCursorVisible(true);
        setRawInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        setFocusable(false);
        setOnTouchListener((v, event) -> true);
    }

    public TypewriterView type(CharSequence text, long speed) {
        mRunnableQueue.add(new TextAdder(text, speed, mRunNextRunnable));
        return runNextIfNotRunning();
    }

    public TypewriterView type(CharSequence text) {
        return type(text, TYPE_SPEED);
    }

    public TypewriterView delete(CharSequence text, long speed) {
        mRunnableQueue.add(new TextEraser(text, speed, mRunNextRunnable));
        return runNextIfNotRunning();
    }

    public TypewriterView delete(CharSequence text) {
        return delete(text, DELETE_SPEED);
    }

    public TypewriterView pause(long millis) {
        mRunnableQueue.add(new TypePauser(millis, mRunNextRunnable));
        return runNextIfNotRunning();
    }

    public TypewriterView run(Runnable runnable) {
        mRunnableQueue.add(new TypeRunnable(runnable, mRunNextRunnable));
        return runNextIfNotRunning();
    }

    public TypewriterView clear() {
        mRunnableQueue.add(new TypeRunnable(new TextClearer(), mRunNextRunnable));
        return runNextIfNotRunning();
    }

    @NonNull
    private TypewriterView runNextIfNotRunning() {
        if (!isRunning) runNext();
        return this;
    }

    public TypewriterView pause() {
        return pause(PAUSE_DELAY);
    }

    private void setCursorAtEnd() {
        setSelection(getText().length());
    }

    private void runNext() {
        isRunning = true;
        Repeater next = mRunnableQueue.poll();

        if (next == null) {
            isRunning = false;
            return;
        }

        next.run();
    }

    private abstract class Repeater implements Runnable {
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

    private class TextAdder extends Repeater {

        private CharSequence mTextToAdd;

        public TextAdder(CharSequence textToAdd, long speed, Runnable doneRunnable) {
            super(doneRunnable, speed);

            mTextToAdd = textToAdd;
        }

        @Override
        public void run() {
            if (mTextToAdd.length() == 0) {
                done();
                return;
            }

            char first = mTextToAdd.charAt(0);
            mTextToAdd = mTextToAdd.subSequence(1, mTextToAdd.length());

            setText(getText().toString() + first);
            setCursorAtEnd();
            delayAndRepeat();
        }
    }

    private class TextEraser extends Repeater {

        private CharSequence mTextToRemove;

        public TextEraser(CharSequence textToRemove, long speed, Runnable doneRunnable) {
            super(doneRunnable, speed);

            mTextToRemove = textToRemove;
        }

        @Override
        public void run() {
            if (mTextToRemove.length() == 0) {
                done();
                return;
            }

            char last = mTextToRemove.charAt(mTextToRemove.length() - 1);
            mTextToRemove = mTextToRemove.subSequence(0, mTextToRemove.length() - 1);

            CharSequence text = getText();

            if (text.charAt(text.length() - 1) == last) {
                setText(text.subSequence(0, text.length() - 1));
            }

            setCursorAtEnd();
            delayAndRepeat();
        }
    }

    private class TextClearer implements Runnable {

        @Override
        public void run() {
            setText("");
        }
    }

    private class TypePauser extends Repeater {

        boolean hasPaused = false;

        public TypePauser(long delay, Runnable doneRunnable) {
            super(doneRunnable, delay);
        }

        @Override
        public void run() {
            if (hasPaused) {
                done();
                return;
            }

            hasPaused = true;
            delayAndRepeat();
        }
    }

    private class TypeRunnable extends Repeater {

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
}
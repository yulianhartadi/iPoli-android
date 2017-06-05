package io.ipoli.android.app.ui.typewriter;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatEditText;
import android.text.InputType;
import android.util.AttributeSet;

import java.util.LinkedList;
import java.util.Queue;

import io.ipoli.android.app.ui.typewriter.repeaters.Repeater;
import io.ipoli.android.app.ui.typewriter.repeaters.TextAdder;
import io.ipoli.android.app.ui.typewriter.repeaters.TextEraser;
import io.ipoli.android.app.ui.typewriter.repeaters.TypePauser;
import io.ipoli.android.app.ui.typewriter.repeaters.TypeRunnable;

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
        mRunnableQueue.add(new TextAdder(this, text, speed, mRunNextRunnable));
        return runNextIfNotRunning();
    }

    public TypewriterView type(CharSequence text) {
        return type(text, TYPE_SPEED);
    }

    public TypewriterView delete(CharSequence text, long speed) {
        mRunnableQueue.add(new TextEraser(this, text, speed, mRunNextRunnable));
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
        mRunnableQueue.add(new TypeRunnable(new TextClearer(this), mRunNextRunnable));
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

    void setCursorAtEnd() {
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

}
package io.ipoli.android.app.ui.typewriter;

import android.content.Context;
import android.graphics.Color;
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
    private boolean isStopped = false;

    private Queue<Repeater> repeaters = new LinkedList<>();

    private Runnable nextRunnable = this::runNext;

    private Repeater currentRepeater;

    public TypewriterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setKeyListener(null);
        setBackgroundColor(Color.TRANSPARENT);
        setCursorVisible(true);
        setCursorAtEnd();
        setRawInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        setOnTouchListener((v, event) -> true);
    }

    public TypewriterView type(CharSequence text) {
        repeaters.add(new TextAdder(this, text, TYPE_SPEED, nextRunnable));
        return runNextIfRunning();
    }

    public TypewriterView delete(CharSequence text) {
        repeaters.add(new TextEraser(this, text, DELETE_SPEED, nextRunnable));
        return runNextIfRunning();
    }

    public TypewriterView pause(long millis) {
        repeaters.add(new TypePauser(millis, nextRunnable));
        return runNextIfRunning();
    }

    public TypewriterView run(Runnable runnable) {
        repeaters.add(new TypeRunnable(runnable, nextRunnable));
        return runNextIfRunning();
    }

    public TypewriterView clear() {
        repeaters.add(new TypeRunnable(new TextClearer(this), nextRunnable));
        return runNextIfRunning();
    }

    @NonNull
    private TypewriterView runNextIfRunning() {
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
        currentRepeater = repeaters.poll();

        if (currentRepeater == null) {
            isRunning = false;
            return;
        }
        if (isStopped) {
            return;
        }
        currentRepeater.run();
    }

    public void stop() {
        isStopped = true;
        if (currentRepeater != null) {
            currentRepeater.stop();
        }
    }
}
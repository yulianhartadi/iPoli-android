package io.ipoli.android.app.ui.typewriter;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/5/17.
 */
class TextAdder extends Repeater {

    private TypewriterView typewriterView;
    private CharSequence mTextToAdd;

    public TextAdder(TypewriterView typewriterView, CharSequence textToAdd, long speed, Runnable doneRunnable) {
        super(doneRunnable, speed);
        this.typewriterView = typewriterView;

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

        typewriterView.setText(typewriterView.getText().toString() + first);
        typewriterView.setCursorAtEnd();
        delayAndRepeat();
    }
}

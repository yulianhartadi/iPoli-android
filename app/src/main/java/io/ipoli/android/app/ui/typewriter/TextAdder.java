package io.ipoli.android.app.ui.typewriter;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/5/17.
 */
class TextAdder extends Repeater {

    private TypewriterView typewriterView;
    private CharSequence textToAdd;

    public TextAdder(TypewriterView typewriterView, CharSequence textToAdd, long speed, Runnable doneRunnable) {
        super(doneRunnable, speed);
        this.typewriterView = typewriterView;

        this.textToAdd = textToAdd;
    }

    @Override
    public void run() {
        if (textToAdd.length() == 0) {
            done();
            return;
        }

        char first = textToAdd.charAt(0);
        textToAdd = textToAdd.subSequence(1, textToAdd.length());

        typewriterView.setText(typewriterView.getText().toString() + first);
        typewriterView.setCursorAtEnd();
        delayAndRepeat();
    }
}

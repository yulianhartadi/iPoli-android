package io.ipoli.android.app.ui.typewriter;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/5/17.
 */
class TextEraser extends Repeater {

    private TypewriterView typewriterView;
    private CharSequence textToRemove;

    public TextEraser(TypewriterView typewriterView, CharSequence textToRemove, long speed, Runnable doneRunnable) {
        super(doneRunnable, speed);
        this.typewriterView = typewriterView;

        this.textToRemove = textToRemove;
    }

    @Override
    public void run() {
        if (textToRemove.length() == 0) {
            done();
            return;
        }

        char last = textToRemove.charAt(textToRemove.length() - 1);
        textToRemove = textToRemove.subSequence(0, textToRemove.length() - 1);

        CharSequence text = typewriterView.getText();

        if (text.charAt(text.length() - 1) == last) {
            typewriterView.setText(text.subSequence(0, text.length() - 1));
        }

        typewriterView.setCursorAtEnd();
        delayAndRepeat();
    }
}

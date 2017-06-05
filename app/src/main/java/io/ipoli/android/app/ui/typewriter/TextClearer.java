package io.ipoli.android.app.ui.typewriter;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/5/17.
 */
class TextClearer implements Runnable {
    private TypewriterView typewriterView;

    public TextClearer(TypewriterView typewriterView) {
        this.typewriterView = typewriterView;
    }

    @Override
    public void run() {
        typewriterView.setText("");
    }
}

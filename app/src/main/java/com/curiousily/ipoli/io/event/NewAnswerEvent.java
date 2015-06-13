package com.curiousily.ipoli.io.event;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/13/15.
 */
public class NewAnswerEvent {
    private final String answer;

    public NewAnswerEvent(String answer) {
        this.answer = answer;
    }

    public String getAnswer() {
        return answer;
    }
}

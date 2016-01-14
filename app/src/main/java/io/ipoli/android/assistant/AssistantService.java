package io.ipoli.android.assistant;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
public interface AssistantService {

    Assistant getAssistant();
    void changeAvatar(String newAvatar);

    void onPlayerMessage(String text);

    void start();
}

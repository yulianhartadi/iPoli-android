package com.curiousily.ipoli.assistant;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/13/15.
 */
public interface OutputHandler {
    void showResponse(String response);
    void showQuery(String query);
    void shutdown();
}

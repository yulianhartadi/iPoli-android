package com.curiousily.ipoli.assistant.data.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/19/15.
 */
public class Query {
    public String text;
    @JsonProperty("user_id")
    public String userId;

    private Query(String text, String userId) {
        this.text = text;
        this.userId = userId;
    }


    public static Query from(String text, String userId) {
        return new Query(text, userId);
    }
}

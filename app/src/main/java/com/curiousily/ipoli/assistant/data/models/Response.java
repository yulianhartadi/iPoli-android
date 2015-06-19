package com.curiousily.ipoli.assistant.data.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/19/15.
 */
public class Response {
    public String intent;
    public String text;
    @JsonProperty("user_id")
    public String userId;
    @JsonProperty("created_at")
    public Date createdAt;
}

package io.ipoli.android.app.net;

import com.google.gson.Gson;

import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/25/16.
 */
public class JsonRequestBodyBuilder {
    private HashMap<String, Object> params;

    public JsonRequestBodyBuilder() {
        params = new HashMap<>();
    }

    public JsonRequestBodyBuilder param(String name, Object value) {
        params.put(name, value);
        return this;
    }

    public RequestBody build() {
        Gson gson = new Gson();
        return RequestBody.create(MediaType.parse("application/json"), gson.toJson(params));
    }


}

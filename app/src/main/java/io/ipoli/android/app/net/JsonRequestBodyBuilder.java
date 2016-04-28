package io.ipoli.android.app.net;

import android.content.Context;

import com.google.gson.Gson;

import java.util.HashMap;

import javax.inject.Inject;

import io.ipoli.android.app.App;
import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/25/16.
 */
public class JsonRequestBodyBuilder {

    @Inject
    Gson gson;

    private HashMap<String, Object> params;

    public JsonRequestBodyBuilder(Context context) {
        params = new HashMap<>();
        App.getAppComponent(context).inject(this);
    }

    public JsonRequestBodyBuilder param(String name, Object value) {
        params.put(name, value);
        return this;
    }

    public RequestBody build() {
        return RequestBody.create(MediaType.parse("application/json"), gson.toJson(params));
    }

}
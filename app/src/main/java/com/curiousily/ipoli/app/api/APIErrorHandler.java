package com.curiousily.ipoli.app.api;

import android.content.Context;

import com.curiousily.ipoli.R;

import retrofit.ErrorHandler;
import retrofit.RetrofitError;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/10/15.
 */
public class APIErrorHandler implements ErrorHandler {
    private final Context context;

    public APIErrorHandler(Context context) {
        this.context = context;
    }

    @Override
    public Throwable handleError(RetrofitError cause) {
        switch (cause.getKind()) {
            case NETWORK:
                return new Exception(context.getString(R.string.error_server_unreachable_message));
            default:
                return new Exception(context.getString(R.string.error_server_response));
        }
    }
}

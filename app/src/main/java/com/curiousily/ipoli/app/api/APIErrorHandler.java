package com.curiousily.ipoli.app.api;

import android.content.Context;
import android.util.Log;

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
        Log.d("iPoli API error", cause.getKind().name(), cause);
        int errorRes = cause.getKind() == RetrofitError.Kind.NETWORK ? R.string.error_server_unreachable_message : R.string.error_server_response;
        return new Exception(context.getString(errorRes));
    }
}

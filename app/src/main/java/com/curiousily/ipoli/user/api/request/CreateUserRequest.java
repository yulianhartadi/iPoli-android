package com.curiousily.ipoli.user.api.request;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/2/15.
 */
public class CreateUserRequest {
    public static final String PROVIDER_ANONYMOUS = "ANONYMOUS";
    public final String uid;
    public final String provider;

    public CreateUserRequest(String uid, String provider) {
        this.uid = uid;
        this.provider = provider;
    }

    public CreateUserRequest(String uid) {
        this(uid, PROVIDER_ANONYMOUS);
    }
}

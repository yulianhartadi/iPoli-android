package io.ipoli.android.player;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/16/17.
 */

public class LoginProvider {
    public enum Provider {
        FACEBOOK, GOOGLE;
    }

    private String id;
    private String provider;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    @JsonIgnore
    public void setProviderType(Provider provider) {
        this.provider = provider.name();
    }

    @JsonIgnore
    public Provider getProviderType() {
        return Provider.valueOf(provider);
    }
}

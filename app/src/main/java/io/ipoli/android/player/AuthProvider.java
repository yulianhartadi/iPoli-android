package io.ipoli.android.player;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/16/17.
 */

public class AuthProvider {
    public enum Provider {
        FACEBOOK, GOOGLE;
    }

    private String id;
    private String provider;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String picture;

    public AuthProvider() {
    }

    public AuthProvider(String id, Provider provider) {
        this.id = id;
        setProviderType(provider);
    }

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

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

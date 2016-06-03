package io.ipoli.android.player;

import java.util.Date;

import io.ipoli.android.app.utils.DateUtils;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/31/16.
 */
public class AuthProvider extends RealmObject{

    @Required
    @PrimaryKey
    private String id;
    private String provider;

    @Required
    private Date createdAt;

    @Required
    private Date updatedAt;

    public AuthProvider() {

    }

    public AuthProvider(String id, String provider) {
        this.id = id;
        this.provider = provider;
        this.createdAt = DateUtils.nowUTC();
        this.updatedAt = DateUtils.nowUTC();
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getProvider() {
        return provider;
    }
}
package io.ipoli.android.store.avatars

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 8/20/17.
 */
data class AvatarModel(val code: Int,
                       val name: String,
                       val price: Int,
                       val picture: Int,
                       val isBought: Boolean)
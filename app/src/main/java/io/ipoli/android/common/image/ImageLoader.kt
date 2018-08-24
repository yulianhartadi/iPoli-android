package io.ipoli.android.common.image

import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import io.ipoli.android.R

interface ImageLoader {
    fun loadMotivationalImage(
        imageUrl: String,
        view: ImageView,
        onReady: () -> Unit,
        onError: (Exception?) -> Unit
    )

    fun loadTodayImage(
        imageUrl: String,
        view: ImageView,
        onReady: () -> Unit,
        onError: (Exception?) -> Unit
    )
}

class AndroidImageLoader : ImageLoader {

    override fun loadMotivationalImage(
        imageUrl: String,
        view: ImageView,
        onReady: () -> Unit,
        onError: (Exception?) -> Unit
    ) {

        val reqOps =
            RequestOptions()
                .centerCrop()
                .error(R.drawable.fallback_plan_day)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)

        Glide.with(view.context)
            .load(imageUrl)
            .apply(reqOps)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    onError(e)
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    onReady()
                    return false
                }
            })
            .into(view)
    }

    override fun loadTodayImage(
        imageUrl: String,
        view: ImageView,
        onReady: () -> Unit,
        onError: (Exception?) -> Unit
    ) {
        val reqOps =
            RequestOptions()
                .centerCrop()
                .dontAnimate()
                .error(R.drawable.fallback_today)

        Glide.with(view.context)
            .load(imageUrl)
            .apply(reqOps)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    onError(e)
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    onReady()
                    return false
                }
            })
            .into(view)

    }

}
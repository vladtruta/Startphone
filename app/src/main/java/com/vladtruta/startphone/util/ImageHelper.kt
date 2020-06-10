package com.vladtruta.startphone.util

import android.app.Activity
import android.content.Context
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import java.io.File

@Suppress("unused")
object ImageHelper {
    fun loadImage(
        activity: Activity,
        imageView: ImageView,
        imageUrl: String?,
        signature: String? = ""
    ) {
        loadImage(activity, null, null, imageView, imageUrl, signature)
    }

    fun loadImage(
        fragment: Fragment,
        imageView: ImageView,
        imageUrl: String?,
        signature: String? = ""
    ) {
        loadImage(null, fragment, null, imageView, imageUrl, signature)
    }

    fun loadImage(
        context: Context,
        imageView: ImageView,
        imageUrl: String?,
        signature: String? = ""
    ) {
        loadImage(null, null, context, imageView, imageUrl, signature)
    }

    fun loadLocalImage(
        activity: Activity,
        imageView: ImageView,
        imageFile: File,
        signature: String? = ""
    ) {
        loadImage(activity, null, null, imageView, imageFile.path, signature)
    }

    private fun loadImage(
        activity: Activity?, fragment: Fragment?, context: Context?,
        imageView: ImageView, imageUrl: String?, signature: String? = ""
    ) {
        val requestManager = when {
            activity != null -> Glide.with(activity)
            fragment != null -> Glide.with(fragment)
            context != null -> Glide.with(context)
            else -> null
        }

        requestManager?.apply {
            load(imageUrl)
                .apply(getOptions(signature))
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imageView)
        }
    }

    private fun getOptions(signature: String?): RequestOptions {
        return RequestOptions()
            .centerCrop()
            .signature(ObjectKey(signature ?: ""))
    }
}

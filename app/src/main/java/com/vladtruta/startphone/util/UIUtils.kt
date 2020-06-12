package com.vladtruta.startphone.util

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.*
import androidx.core.content.ContextCompat
import org.koin.core.KoinComponent
import org.koin.core.inject

object UIUtils: KoinComponent {
    
    private val startphoneApp by inject<StartphoneApp>()
    
    fun getDimension(@DimenRes resId: Int): Int {
        return startphoneApp.resources.getDimensionPixelSize(resId)
    }

    fun getDimensionFloat(@DimenRes resId: Int): Float {
        return startphoneApp.resources.getDimension(resId)
    }

    fun getString(@StringRes resId: Int, vararg args: Any?): String {
        return startphoneApp.resources.getString(resId, *args)
    }

    fun getDrawable(@DrawableRes resId: Int): Drawable? {
        return ContextCompat.getDrawable(startphoneApp, resId)
    }

    @ColorInt
    fun getColor(@ColorRes resId: Int): Int {
        return ContextCompat.getColor(startphoneApp, resId)
    }

    fun getColorStateList(@ColorRes resId: Int): ColorStateList? {
        return ContextCompat.getColorStateList(startphoneApp, resId)
    }

    fun dpToPx(dp: Float): Int {
        return (dp * startphoneApp.resources.displayMetrics.density).toInt()
    }

    fun showSoftKeyboardFor(view: View) {
        if (view.requestFocus()) {
            val imm =
                view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    fun hideKeyboardFrom(view: View) {
        val inputMethodManager =
            view.context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)

        view.clearFocus()
    }
}
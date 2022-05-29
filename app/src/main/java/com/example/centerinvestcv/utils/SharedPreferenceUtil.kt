package com.example.centerinvestcv.utils

import android.content.Context
import androidx.appcompat.app.AppCompatActivity

object SharedPreferenceUtil {

    private const val PREFERENCE_FILE_KEY = "com.example.catsapp.PREFERENCE_FILE_KEY"

    private const val IS_HIDE_DATA_ENABLED = "IS_HIDE_DATA_ENABLED"

    var Context.isHideDataEnabled: Boolean
        get() = getSharedPreferences(
            PREFERENCE_FILE_KEY,
            AppCompatActivity.MODE_PRIVATE
        ).getBoolean(IS_HIDE_DATA_ENABLED, false)
        set(value) = getSharedPreferences(
            PREFERENCE_FILE_KEY,
            AppCompatActivity.MODE_PRIVATE
        ).edit().putBoolean(IS_HIDE_DATA_ENABLED, value).apply()

}
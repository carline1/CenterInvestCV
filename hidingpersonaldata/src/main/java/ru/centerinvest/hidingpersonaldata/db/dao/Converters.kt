package ru.centerinvest.hidingpersonaldata.db.dao

import androidx.room.TypeConverter
import com.google.gson.Gson

class Converters {

    @TypeConverter
    fun fromString(value: String): FloatArray = Gson().fromJson(value, FloatArray::class.java)

    @TypeConverter
    fun fromFloatArray(list: FloatArray): String = Gson().toJson(list)

}
package com.example.centerinvestcv.db.dao

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.centerinvestcv.db.dao.FaceEntity.Companion.TABLE_NAME

@Entity(tableName = TABLE_NAME)
data class FaceEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "faceImage")
    val faceImage: FloatArray

) {
    companion object {
        const val TABLE_NAME = "faces_table"
    }
}
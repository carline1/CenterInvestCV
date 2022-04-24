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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FaceEntity

        if (id != other.id) return false
        if (!faceImage.contentEquals(other.faceImage)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + faceImage.contentHashCode()
        return result
    }
}
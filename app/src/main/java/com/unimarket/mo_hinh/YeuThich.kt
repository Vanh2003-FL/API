package com.unimarket.mo_hinh

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/** Bảng yeu_thich: người dùng lưu tin nào. Khoá chính ghép nên mỗi người chỉ lưu một tin một lần. */
@Entity(
    tableName = "yeu_thich",
    primaryKeys = ["nguoiDungId", "tinDangId"],
    foreignKeys = [
        ForeignKey(entity = NguoiDung::class, parentColumns = ["id"], childColumns = ["nguoiDungId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = TinDang::class, parentColumns = ["id"], childColumns = ["tinDangId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("tinDangId")]
)
data class YeuThich(
    val nguoiDungId: Long,
    val tinDangId: Long,
    val ngayTao: Long = System.currentTimeMillis()
)

package com.unimarket.mo_hinh

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Bảng cuoc_tro_chuyen: một phòng chat giữa người mua và người bán về MỘT tin đăng.
 * Mỗi cặp (tin đăng, người mua) chỉ có một phòng, lịch hẹn cũng nằm trong phòng này.
 */
@Entity(
    tableName = "cuoc_tro_chuyen",
    foreignKeys = [
        ForeignKey(entity = TinDang::class, parentColumns = ["id"], childColumns = ["tinDangId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = NguoiDung::class, parentColumns = ["id"], childColumns = ["nguoiMuaId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = NguoiDung::class, parentColumns = ["id"], childColumns = ["nguoiBanId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index(value = ["tinDangId", "nguoiMuaId"], unique = true), Index("nguoiMuaId"), Index("nguoiBanId")]
)
data class CuocTroChuyen(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tinDangId: Long,
    val nguoiMuaId: Long,
    val nguoiBanId: Long,
    val tinNhanCuoi: String = "",
    val chuaDocNguoiMua: Int = 0,        // số tin chưa đọc của người mua
    val chuaDocNguoiBan: Int = 0,        // số tin chưa đọc của người bán
    val capNhatLuc: Long = System.currentTimeMillis()
)

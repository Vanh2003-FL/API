package com.unimarket.mo_hinh

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Bảng bao_cao: người dùng báo cáo tin vi phạm.
 * Chỉ mục duy nhất (tinDangId, nguoiBaoCaoId) → mỗi người chỉ báo cáo một tin một lần.
 */
@Entity(
    tableName = "bao_cao",
    foreignKeys = [
        ForeignKey(entity = TinDang::class, parentColumns = ["id"], childColumns = ["tinDangId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = NguoiDung::class, parentColumns = ["id"], childColumns = ["nguoiBaoCaoId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index(value = ["tinDangId", "nguoiBaoCaoId"], unique = true), Index("nguoiBaoCaoId")]
)
data class BaoCao(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tinDangId: Long,
    val nguoiBaoCaoId: Long,
    val lyDo: String,                    // Lừa đảo / Hàng cấm / Sai danh mục / Thông tin sai sự thật / Spam / Khác
    val ghiChu: String = "",
    val trangThai: String = "CHO_XU_LY", // CHO_XU_LY / DA_XU_LY / BO_QUA
    val ngayTao: Long = System.currentTimeMillis()
)

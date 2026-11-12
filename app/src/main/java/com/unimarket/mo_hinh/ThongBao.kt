package com.unimarket.mo_hinh

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Bảng thong_bao: thông báo của từng người dùng.
 * loaiLienKet + lienKetId cho biết bấm vào thông báo thì mở màn nào (phòng chat, tin của tôi...).
 */
@Entity(
    tableName = "thong_bao",
    foreignKeys = [ForeignKey(
        entity = NguoiDung::class,
        parentColumns = ["id"],
        childColumns = ["nguoiNhanId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("nguoiNhanId")]
)
data class ThongBao(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nguoiNhanId: Long,
    val loai: String,                    // DAT_MUA / DOI_HEN / CHOT_HEN / NHAC_HEN / HUY_HEN / DA_DANG / SAP_HET_HAN / MOI_DANH_GIA / DANH_GIA / MOI_DAT_MUA
    val tieuDe: String,
    val noiDung: String,
    val loaiLienKet: String? = null,     // CHAT / TIN_CUA_TOI / TIN_DANG
    val lienKetId: Long? = null,
    val daDoc: Boolean = false,
    val daDay: Boolean = false,          // đã hiện lên máy (banner hoặc thông báo hệ thống) chưa
    val thoiGian: Long = System.currentTimeMillis()
)

package com.unimarket.mo_hinh

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Bảng tin_nhan: từng tin nhắn trong phòng chat.
 * loai = VAN_BAN (tin nhắn chữ) hoặc SU_KIEN (thẻ sự kiện lịch hẹn: đặt mua, đổi, chốt, huỷ...).
 */
@Entity(
    tableName = "tin_nhan",
    foreignKeys = [ForeignKey(
        entity = CuocTroChuyen::class,
        parentColumns = ["id"],
        childColumns = ["cuocTroChuyenId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("cuocTroChuyenId")]
)
data class TinNhan(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cuocTroChuyenId: Long,
    val nguoiGuiId: Long,
    val loai: String = "VAN_BAN",        // VAN_BAN / SU_KIEN
    val noiDung: String = "",
    val suKien: String? = null,          // DAT_MUA / DOI_HEN / CHOT_HEN / HUY_HEN / NHAC_HEN / DA_BAN / MOI_DANH_GIA
    val diaDiem: String? = null,         // địa điểm tại thời điểm xảy ra sự kiện
    val thoiGianHen: Long? = null,
    val thoiGian: Long = System.currentTimeMillis()
)

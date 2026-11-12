package com.unimarket.mo_hinh

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Bảng lich_hen: lịch hẹn giao dịch, gắn với một phòng chat.
 * Trạng thái: CHO_XAC_NHAN → DA_CHOT → HOAN_TAT, hoặc DA_HUY.
 * nguoiDeXuatId = người đưa ra địa điểm/giờ hiện tại; NGƯỜI CÒN LẠI mới được bấm xác nhận.
 */
@Entity(
    tableName = "lich_hen",
    foreignKeys = [
        ForeignKey(entity = CuocTroChuyen::class, parentColumns = ["id"], childColumns = ["cuocTroChuyenId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = TinDang::class, parentColumns = ["id"], childColumns = ["tinDangId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("cuocTroChuyenId"), Index("tinDangId")]
)
data class LichHen(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cuocTroChuyenId: Long,
    val tinDangId: Long,
    val nguoiMuaId: Long,
    val nguoiBanId: Long,
    val maTruong: String,
    val diaDiem: String,
    val thoiGianHen: Long,
    val loiNhan: String = "",
    val trangThai: String = "CHO_XAC_NHAN",
    val nguoiDeXuatId: Long,
    val lyDoHuy: String? = null,
    val nguoiHuyId: Long? = null,
    val soSao: Int? = null,              // đánh giá người bán sau giao dịch (1–5)
    val nhanXet: String? = null,
    val ngayTao: Long = System.currentTimeMillis(),
    val ngayChot: Long? = null,
    val hetHanLuc: Long = System.currentTimeMillis() + 24L * 60 * 60 * 1000 // tự huỷ nếu 24h không ai xác nhận
)

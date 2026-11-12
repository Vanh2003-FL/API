package com.unimarket.du_lieu.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.unimarket.mo_hinh.LichHen

/** Các câu lệnh đọc/ghi bảng lich_hen. */
@Dao
interface LichHenDao {

    @Insert
    suspend fun them(lichHen: LichHen): Long

    @Update
    suspend fun capNhat(lichHen: LichHen)

    @Query("SELECT * FROM lich_hen WHERE id = :id")
    suspend fun layTheoId(id: Long): LichHen?

    /** Điểm đánh giá trung bình của người bán (null nếu chưa có đánh giá). */
    @Query("SELECT AVG(soSao) FROM lich_hen WHERE nguoiBanId = :nguoiBanId AND soSao IS NOT NULL")
    suspend fun diemTrungBinh(nguoiBanId: Long): Double?

    @Query("SELECT COUNT(*) FROM lich_hen WHERE nguoiBanId = :nguoiBanId AND soSao IS NOT NULL")
    suspend fun soDanhGia(nguoiBanId: Long): Int

    /** Lịch hẹn mới nhất của một phòng chat (để hiện ở thanh ghim). */
    @Query("SELECT * FROM lich_hen WHERE cuocTroChuyenId = :cuocId ORDER BY ngayTao DESC, id DESC LIMIT 1")
    suspend fun layMoiNhatCuaCuoc(cuocId: Long): LichHen?

    /** Các lịch hẹn còn hiệu lực (chờ xác nhận hoặc đã chốt) của một tin. */
    @Query("SELECT * FROM lich_hen WHERE tinDangId = :tinDangId AND trangThai IN ('CHO_XAC_NHAN', 'DA_CHOT')")
    suspend fun layDangHoatDongCuaTin(tinDangId: Long): List<LichHen>

    /** BR-33: số yêu cầu mua đang chờ của một người mua (tối đa 5). */
    /** Mọi lịch còn hiệu lực – dùng cho tác vụ tự động (nhắc hẹn, tự huỷ) và hẹn giờ báo thức. */
    @Query("SELECT * FROM lich_hen WHERE trangThai IN ('CHO_XAC_NHAN', 'DA_CHOT')")
    suspend fun layDangHoatDong(): List<LichHen>

    @Query("SELECT COUNT(*) FROM lich_hen WHERE nguoiMuaId = :nguoiMuaId AND trangThai = 'CHO_XAC_NHAN'")
    suspend fun demChoCuaNguoiMua(nguoiMuaId: Long): Int
}

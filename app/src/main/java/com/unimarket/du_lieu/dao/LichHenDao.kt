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
}

package com.unimarket.du_lieu.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.unimarket.mo_hinh.TinDang

/** Các câu lệnh đọc/ghi bảng tin_dang. */
@Dao
interface TinDangDao {

    @Insert
    suspend fun them(tin: TinDang): Long

    @Insert
    suspend fun themNhieu(ds: List<TinDang>): List<Long>

    @Update
    suspend fun capNhat(tin: TinDang)

    @Query("SELECT * FROM tin_dang WHERE id = :id")
    suspend fun layTheoId(id: Long): TinDang?

    /** Tin mới nhất đang hiển thị (đang bán hoặc đang giao dịch). */
    @Query(
        "SELECT * FROM tin_dang WHERE trangThai IN ('DANG_BAN', 'DANG_GIAO_DICH') " +
            "ORDER BY ngayTao DESC LIMIT :gioiHan"
    )
    suspend fun layTinMoi(gioiHan: Int): List<TinDang>

    /** Đếm tin của một người bán theo trạng thái (dùng cho ô "Đang bán / Đã bán" ở Tài khoản). */
    @Query("SELECT COUNT(*) FROM tin_dang WHERE nguoiBanId = :nguoiBanId AND trangThai = :trangThai")
    suspend fun demTheoNguoiBan(nguoiBanId: Long, trangThai: String): Int

    @Query("SELECT COUNT(*) FROM tin_dang")
    suspend fun dem(): Int
}

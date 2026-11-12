package com.unimarket.du_lieu.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.unimarket.mo_hinh.ThongBao

/** Các câu lệnh đọc/ghi bảng thong_bao. */
@Dao
interface ThongBaoDao {

    @Insert
    suspend fun them(thongBao: ThongBao): Long

    @Query("SELECT * FROM thong_bao WHERE nguoiNhanId = :nguoiDungId ORDER BY thoiGian DESC")
    suspend fun layCuaNguoiDung(nguoiDungId: Long): List<ThongBao>

    @Query("SELECT COUNT(*) FROM thong_bao WHERE nguoiNhanId = :nguoiDungId AND daDoc = 0")
    suspend fun demChuaDoc(nguoiDungId: Long): Int
}

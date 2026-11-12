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

    @Query("SELECT * FROM thong_bao WHERE id = :id")
    suspend fun layTheoId(id: Long): ThongBao?

    @Query("UPDATE thong_bao SET daDoc = 1 WHERE id = :id")
    suspend fun danhDauDaDoc(id: Long)

    @Query("UPDATE thong_bao SET daDoc = 1 WHERE nguoiNhanId = :nguoiDungId")
    suspend fun danhDauTatCaDaDoc(nguoiDungId: Long)

    /** Thông báo chưa hiện lên máy (chưa banner / chưa đẩy thông báo hệ thống). */
    @Query("SELECT * FROM thong_bao WHERE nguoiNhanId = :nguoiDungId AND daDay = 0 ORDER BY thoiGian")
    suspend fun layChuaDay(nguoiDungId: Long): List<ThongBao>

    @Query("UPDATE thong_bao SET daDay = 1 WHERE nguoiNhanId = :nguoiDungId")
    suspend fun danhDauDaDay(nguoiDungId: Long)
}

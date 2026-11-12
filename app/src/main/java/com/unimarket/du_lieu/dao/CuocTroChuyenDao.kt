package com.unimarket.du_lieu.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.unimarket.mo_hinh.CuocTroChuyen

/** Các câu lệnh đọc/ghi bảng cuoc_tro_chuyen. */
@Dao
interface CuocTroChuyenDao {

    @Insert
    suspend fun them(cuoc: CuocTroChuyen): Long

    @Update
    suspend fun capNhat(cuoc: CuocTroChuyen)

    @Query("SELECT * FROM cuoc_tro_chuyen WHERE id = :id")
    suspend fun layTheoId(id: Long): CuocTroChuyen?

    /** Tìm phòng chat của người mua với một tin (mỗi cặp chỉ có một phòng). */
    @Query("SELECT * FROM cuoc_tro_chuyen WHERE tinDangId = :tinDangId AND nguoiMuaId = :nguoiMuaId LIMIT 1")
    suspend fun timPhong(tinDangId: Long, nguoiMuaId: Long): CuocTroChuyen?

    /** Các phòng chat có mặt người dùng (là người mua hoặc người bán), mới hoạt động nhất lên đầu. */
    @Query("SELECT * FROM cuoc_tro_chuyen WHERE nguoiMuaId = :nguoiDungId OR nguoiBanId = :nguoiDungId ORDER BY capNhatLuc DESC")
    suspend fun layCuaNguoiDung(nguoiDungId: Long): List<CuocTroChuyen>

    /** Các phòng chat về một tin (người bán dùng khi chọn "bán cho ai"). */
    @Query("SELECT * FROM cuoc_tro_chuyen WHERE tinDangId = :tinDangId ORDER BY capNhatLuc DESC")
    suspend fun layTheoTin(tinDangId: Long): List<CuocTroChuyen>

    /** Số người đang hỏi mua một tin = số phòng chat của tin đó. */
    @Query("SELECT COUNT(*) FROM cuoc_tro_chuyen WHERE tinDangId = :tinDangId")
    suspend fun demTheoTin(tinDangId: Long): Int

    /** Tổng số tin nhắn chưa đọc của một người (hiện số trên tab Tin nhắn). */
    @Query(
        "SELECT COALESCE(SUM(CASE WHEN nguoiMuaId = :nguoiDungId THEN chuaDocNguoiMua ELSE chuaDocNguoiBan END), 0) " +
            "FROM cuoc_tro_chuyen WHERE nguoiMuaId = :nguoiDungId OR nguoiBanId = :nguoiDungId"
    )
    suspend fun tongChuaDoc(nguoiDungId: Long): Int
}

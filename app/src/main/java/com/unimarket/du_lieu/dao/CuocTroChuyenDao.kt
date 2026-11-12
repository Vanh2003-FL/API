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
}

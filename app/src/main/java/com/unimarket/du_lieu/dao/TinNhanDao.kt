package com.unimarket.du_lieu.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.unimarket.mo_hinh.TinNhan

/** Các câu lệnh đọc/ghi bảng tin_nhan. */
@Dao
interface TinNhanDao {

    @Insert
    suspend fun them(tinNhan: TinNhan): Long

    @Query("SELECT * FROM tin_nhan WHERE cuocTroChuyenId = :cuocId ORDER BY thoiGian, id")
    suspend fun layTheoCuoc(cuocId: Long): List<TinNhan>

    @Insert
    suspend fun themNhieu(ds: List<TinNhan>)
}

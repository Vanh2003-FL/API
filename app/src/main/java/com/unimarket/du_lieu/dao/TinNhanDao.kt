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

    /** Lượt gửi tin nhắn của người dùng (bỏ tin hệ thống) – để đếm người dùng hoạt động (KPI-04). */
    @Query("SELECT nguoiGuiId AS nguoiDungId, thoiGian FROM tin_nhan WHERE nguoiGuiId != 0")
    suspend fun layHoatDong(): List<com.unimarket.xu_ly_nghiep_vu.ThongKe.HoatDong>
}

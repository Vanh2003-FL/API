package com.unimarket.du_lieu.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.unimarket.mo_hinh.NguoiDung

/** Các câu lệnh đọc/ghi bảng nguoi_dung. */
@Dao
interface NguoiDungDao {

    @Insert
    suspend fun them(nguoiDung: NguoiDung): Long

    @Insert
    suspend fun themNhieu(ds: List<NguoiDung>): List<Long>

    @Update
    suspend fun capNhat(nguoiDung: NguoiDung)

    @Query("SELECT * FROM nguoi_dung WHERE id = :id")
    suspend fun layTheoId(id: Long): NguoiDung?

    @Query("SELECT * FROM nguoi_dung WHERE email = :email LIMIT 1")
    suspend fun timTheoEmail(email: String): NguoiDung?

    @Query("SELECT * FROM nguoi_dung ORDER BY id")
    suspend fun layTatCa(): List<NguoiDung>

    @Query("SELECT COUNT(*) FROM nguoi_dung")
    suspend fun dem(): Int

    /** Id các quản trị viên đang hoạt động (để gửi thông báo kiểm duyệt). */
    @Query("SELECT id FROM nguoi_dung WHERE vaiTro = 'QUAN_TRI' AND biKhoa = 0")
    suspend fun layIdQuanTri(): List<Long>
}

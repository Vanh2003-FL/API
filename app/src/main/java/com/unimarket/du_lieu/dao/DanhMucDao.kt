package com.unimarket.du_lieu.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.unimarket.mo_hinh.DanhMuc

/** Các câu lệnh đọc/ghi bảng danh_muc. */
@Dao
interface DanhMucDao {

    @Insert
    suspend fun them(danhMuc: DanhMuc)

    @Insert
    suspend fun themNhieu(ds: List<DanhMuc>)

    @Update
    suspend fun capNhat(danhMuc: DanhMuc)

    @Query("SELECT * FROM danh_muc ORDER BY maCha, thuTu")
    suspend fun layTatCa(): List<DanhMuc>

    @Query("SELECT * FROM danh_muc WHERE ma = :ma")
    suspend fun layTheoMa(ma: String): DanhMuc?

    /** Số tin (mọi trạng thái trừ đã xoá) của từng danh mục con. */
    @Query("SELECT danhMucCon AS ma, COUNT(*) AS so FROM tin_dang WHERE trangThai != 'DA_XOA' GROUP BY danhMucCon")
    suspend fun demTinTheoCon(): List<SoTin>

    data class SoTin(val ma: String, val so: Int)
}

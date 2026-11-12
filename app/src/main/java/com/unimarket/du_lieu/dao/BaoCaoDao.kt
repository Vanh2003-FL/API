package com.unimarket.du_lieu.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.unimarket.mo_hinh.BaoCao

/** Các câu lệnh đọc/ghi bảng bao_cao. */
@Dao
interface BaoCaoDao {

    // Báo cáo trùng (cùng người, cùng tin) sẽ bị chặn bởi chỉ mục duy nhất
    @Insert
    suspend fun them(baoCao: BaoCao): Long

    @Insert
    suspend fun themNhieu(ds: List<BaoCao>)

    @Query("SELECT COUNT(*) FROM bao_cao WHERE tinDangId = :tinDangId AND nguoiBaoCaoId = :nguoiDungId")
    suspend fun daBaoCao(tinDangId: Long, nguoiDungId: Long): Int

    @Query("SELECT * FROM bao_cao WHERE tinDangId = :tinDangId ORDER BY ngayTao")
    suspend fun layTheoTin(tinDangId: Long): List<BaoCao>
}

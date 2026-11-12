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

    @Query("SELECT * FROM bao_cao")
    suspend fun layTatCa(): List<BaoCao>

    @Query("SELECT * FROM bao_cao WHERE trangThai = 'CHO_XU_LY' ORDER BY ngayTao")
    suspend fun layChoXuLy(): List<BaoCao>

    /** Đóng mọi báo cáo đang chờ của một tin: DA_XU_LY (đã gỡ tin) hoặc BO_QUA (giữ tin). */
    @Query("UPDATE bao_cao SET trangThai = :trangThaiMoi WHERE tinDangId = :tinDangId AND trangThai = 'CHO_XU_LY'")
    suspend fun dongBaoCaoCuaTin(tinDangId: Long, trangThaiMoi: String)
}

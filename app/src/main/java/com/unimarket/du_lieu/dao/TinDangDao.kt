package com.unimarket.du_lieu.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.unimarket.mo_hinh.TinDang

/** Các câu lệnh đọc/ghi bảng tin_dang. */
@Dao
interface TinDangDao {

    @Insert
    suspend fun them(tin: TinDang): Long

    @Insert
    suspend fun themNhieu(ds: List<TinDang>): List<Long>

    @Update
    suspend fun capNhat(tin: TinDang)

    @Query("SELECT * FROM tin_dang WHERE id = :id")
    suspend fun layTheoId(id: Long): TinDang?

    /** Tin mới nhất đang hiển thị (đang bán hoặc đang giao dịch). */
    @Query(
        "SELECT * FROM tin_dang WHERE trangThai IN ('DANG_BAN', 'DANG_GIAO_DICH') " +
            "ORDER BY ngayTao DESC LIMIT :gioiHan"
    )
    suspend fun layTinMoi(gioiHan: Int): List<TinDang>

    /** Tất cả tin người mua nhìn thấy được (dùng cho trang chủ, tìm kiếm, danh mục). */
    @Query("SELECT * FROM tin_dang WHERE trangThai IN ('DANG_BAN', 'DANG_GIAO_DICH') ORDER BY ngayTao DESC")
    suspend fun layDangHienThi(): List<TinDang>

    /** Mẫu cho gợi ý giá: cùng danh mục con, đang bán hoặc đã bán, đăng từ mốc thời gian, tối đa 100 tin mới nhất. */
    @Query(
        "SELECT * FROM tin_dang WHERE danhMucCon = :danhMucCon " +
            "AND trangThai IN ('DANG_BAN', 'DANG_GIAO_DICH', 'DA_BAN') AND ngayTao >= :tuNgay " +
            "ORDER BY ngayTao DESC LIMIT 100"
    )
    suspend fun layMauGoiYGia(danhMucCon: String, tuNgay: Long): List<TinDang>

    /** Tin của một người bán (trừ tin đã xoá), dùng cho màn Tin của tôi. */
    @Query("SELECT * FROM tin_dang WHERE nguoiBanId = :nguoiBanId AND trangThai != 'DA_XOA' ORDER BY ngayCapNhat DESC")
    suspend fun layCuaNguoiBan(nguoiBanId: Long): List<TinDang>

    /** Tin quá 30 ngày mà chưa bán thì chuyển sang Hết hạn. Trả về số tin bị đổi. */
    @Query("UPDATE tin_dang SET trangThai = 'HET_HAN' WHERE trangThai = 'DANG_BAN' AND ngayHetHan < :bayGio")
    suspend fun capNhatHetHan(bayGio: Long): Int

    /** Mỗi lần người khác mở chi tiết tin thì tăng lượt xem thêm 1. */
    @Query("UPDATE tin_dang SET luotXem = luotXem + 1 WHERE id = :id")
    suspend fun tangLuotXem(id: Long)

    /** Đếm tin của một người bán theo trạng thái (dùng cho ô "Đang bán / Đã bán" ở Tài khoản). */
    @Query("SELECT COUNT(*) FROM tin_dang WHERE nguoiBanId = :nguoiBanId AND trangThai = :trangThai")
    suspend fun demTheoNguoiBan(nguoiBanId: Long, trangThai: String): Int

    /** Tin đang bán chưa được báo "sắp hết hạn" (lọc tiếp bằng LichNhac.canBaoSapHetHan). */
    @Query("SELECT * FROM tin_dang WHERE trangThai = 'DANG_BAN' AND daBaoSapHetHan = 0")
    suspend fun layDangBanChuaBaoHetHan(): List<TinDang>

    @Query("SELECT COUNT(*) FROM tin_dang")
    suspend fun dem(): Int

    /** Mọi tin (kể cả đã xoá mềm) – dùng cho thống kê của quản trị viên (BR-09). */
    @Query("SELECT * FROM tin_dang")
    suspend fun layTatCa(): List<TinDang>

    /** Tin còn hiển thị của một người bán (để gỡ khi khoá tài khoản). */
    @Query("SELECT * FROM tin_dang WHERE nguoiBanId = :nguoiBanId AND trangThai IN ('DANG_BAN', 'DANG_GIAO_DICH', 'BI_AN')")
    suspend fun layConHienCuaNguoiBan(nguoiBanId: Long): List<TinDang>
}

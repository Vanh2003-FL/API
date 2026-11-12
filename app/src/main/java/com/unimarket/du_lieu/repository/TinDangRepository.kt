package com.unimarket.du_lieu.repository

import android.content.Context
import androidx.room.withTransaction
import com.unimarket.du_lieu.database.AppDatabase
import com.unimarket.mo_hinh.BaoCao
import com.unimarket.mo_hinh.NguoiDung
import com.unimarket.mo_hinh.TinDang
import com.unimarket.mo_hinh.YeuThich
import com.unimarket.tien_ich.HangSo

/** Xử lý tin đăng phía người mua: xem, lưu (yêu thích), báo cáo vi phạm, tin tương tự. */
class TinDangRepository(context: Context) {

    private val db = AppDatabase.layDb(context)
    private val dao = db.tinDangDao()

    // ===================== XEM TIN =====================
    suspend fun layTinMoi(gioiHan: Int = 40): List<TinDang> = dao.layTinMoi(gioiHan)

    /** Tất cả tin đang hiển thị; việc lọc theo phạm vi/từ khoá làm ở LocTinDang. */
    suspend fun layDangHienThi(): List<TinDang> {
        dao.capNhatHetHan(System.currentTimeMillis()) // tin quá 30 ngày tự chuyển Hết hạn
        return dao.layDangHienThi()
    }

    suspend fun layTheoId(id: Long): TinDang? = dao.layTheoId(id)

    suspend fun tangLuotXem(id: Long) = dao.tangLuotXem(id)

    /** Tin cùng danh mục con (thiếu thì lấy cùng danh mục cha), bỏ chính tin đang xem. */
    suspend fun layTinTuongTu(tin: TinDang, gioiHan: Int = 6): List<TinDang> {
        val ds = dao.layDangHienThi().filter { it.id != tin.id }
        val cungCon = ds.filter { it.danhMucCon == tin.danhMucCon }
        val cungCha = ds.filter { it.danhMucCha == tin.danhMucCha && it.danhMucCon != tin.danhMucCon }
        return (cungCon + cungCha).take(gioiHan)
    }

    // ===================== NGƯỜI BÁN =====================
    data class ThongTinNguoiBan(
        val nguoiDung: NguoiDung?,
        val diemTrungBinh: Double?,
        val soDanhGia: Int,
        val soLanBan: Int
    )

    suspend fun thongTinNguoiBan(nguoiBanId: Long): ThongTinNguoiBan = ThongTinNguoiBan(
        nguoiDung = db.nguoiDungDao().layTheoId(nguoiBanId),
        diemTrungBinh = db.lichHenDao().diemTrungBinh(nguoiBanId),
        soDanhGia = db.lichHenDao().soDanhGia(nguoiBanId),
        soLanBan = dao.demTheoNguoiBan(nguoiBanId, HangSo.TIN_DA_BAN)
    )

    /** Số tin "Đang bán" và "Đã bán" của một người, dùng ở màn Tài khoản. */
    suspend fun demDangBanVaDaBan(nguoiBanId: Long): Pair<Int, Int> =
        dao.demTheoNguoiBan(nguoiBanId, HangSo.TIN_DANG_BAN) to dao.demTheoNguoiBan(nguoiBanId, HangSo.TIN_DA_BAN)

    // ===================== YÊU THÍCH =====================
    suspend fun demDaLuu(nguoiDungId: Long): Int = db.yeuThichDao().demCuaNguoiDung(nguoiDungId)

    suspend fun layIdDaLuu(nguoiDungId: Long): Set<Long> = db.yeuThichDao().layIdTinDaLuu(nguoiDungId).toSet()

    suspend fun layTinDaLuu(nguoiDungId: Long): List<TinDang> = db.yeuThichDao().layTinDaLuu(nguoiDungId)

    /** Bấm trái tim: chưa lưu thì lưu, đã lưu thì bỏ. Trả về true nếu SAU khi bấm tin đang được lưu. */
    suspend fun doiYeuThich(nguoiDungId: Long, tinDangId: Long): Boolean {
        val yt = db.yeuThichDao()
        return if (yt.daLuu(nguoiDungId, tinDangId) > 0) {
            yt.xoa(nguoiDungId, tinDangId)
            false
        } else {
            yt.them(YeuThich(nguoiDungId, tinDangId))
            true
        }
    }

    // ===================== BÁO CÁO VI PHẠM =====================
    enum class KetQuaBaoCao { DA_GHI_NHAN, DA_BAO_CAO_TRUOC, TIN_CUA_MINH, TIN_TU_AN }

    /**
     * Ghi một báo cáo. Quy tắc:
     * - Mỗi người chỉ báo cáo một tin một lần; không tự báo cáo tin của mình.
     * - Đủ 3 báo cáo thì tin tự ẩn (BI_AN), chờ quản trị viên xử lý.
     * Tất cả chạy trong MỘT giao dịch (transaction) để số liệu không bị lệch.
     */
    suspend fun baoCao(tinDangId: Long, nguoiDungId: Long, lyDo: String): KetQuaBaoCao = db.withTransaction {
        val tin = dao.layTheoId(tinDangId) ?: error("Không tìm thấy tin")
        if (tin.nguoiBanId == nguoiDungId) return@withTransaction KetQuaBaoCao.TIN_CUA_MINH
        if (db.baoCaoDao().daBaoCao(tinDangId, nguoiDungId) > 0) return@withTransaction KetQuaBaoCao.DA_BAO_CAO_TRUOC

        db.baoCaoDao().them(BaoCao(tinDangId = tinDangId, nguoiBaoCaoId = nguoiDungId, lyDo = lyDo))
        val soMoi = tin.soBaoCao + 1
        val tuAn = soMoi >= HangSo.NGUONG_BAO_CAO_TU_AN && tin.trangThai == HangSo.TIN_DANG_BAN
        dao.capNhat(
            tin.copy(
                soBaoCao = soMoi,
                trangThai = if (tuAn) HangSo.TIN_BI_AN else tin.trangThai,
                ngayCapNhat = System.currentTimeMillis()
            )
        )
        if (tuAn) KetQuaBaoCao.TIN_TU_AN else KetQuaBaoCao.DA_GHI_NHAN
    }
}

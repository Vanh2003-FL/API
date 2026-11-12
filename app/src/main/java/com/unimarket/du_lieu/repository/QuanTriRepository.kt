package com.unimarket.du_lieu.repository

import android.content.Context
import androidx.room.withTransaction
import com.unimarket.du_lieu.database.AppDatabase
import com.unimarket.mo_hinh.BaoCao
import com.unimarket.mo_hinh.NguoiDung
import com.unimarket.mo_hinh.ThongBao
import com.unimarket.mo_hinh.TinDang
import com.unimarket.tien_ich.HangSo
import com.unimarket.xu_ly_nghiep_vu.ThongKe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.OutputStream

/**
 * Nghiệp vụ của QUẢN TRỊ VIÊN: số liệu thống kê (UC-54), xử lý tin bị báo cáo (UC-51),
 * khoá/mở tài khoản (UC-52), xem bảng dữ liệu và xuất CSV / file .db.
 */
class QuanTriRepository(context: Context) {

    private val appContext = context.applicationContext
    private val db = AppDatabase.layDb(appContext)
    private val tinDao = db.tinDangDao()
    private val ndDao = db.nguoiDungDao()
    private val baoCaoDao = db.baoCaoDao()

    companion object {
        /** Các bảng được phép xem / xuất (đúng tên bảng trong SQLite). */
        val DS_BANG = listOf(
            "nguoi_dung", "tin_dang", "yeu_thich", "cuoc_tro_chuyen", "tin_nhan",
            "lich_hen", "thong_bao", "bao_cao", "danh_muc"
        )
    }

    // ===================== THỐNG KÊ =====================

    data class DuLieuThongKe(
        val dsTin: List<TinDang>,
        val dsNguoiDung: List<NguoiDung>,
        val dsLich: List<com.unimarket.mo_hinh.LichHen>,
        val dsBaoCao: List<BaoCao>,
        val dsHoatDong: List<ThongKe.HoatDong>
    )

    /** Đọc toàn bộ bản ghi cần cho KPI. Dữ liệu đồ án nhỏ nên tính trong bộ nhớ cho dễ hiểu. */
    suspend fun layDuLieuThongKe(): DuLieuThongKe {
        val dsTin = tinDao.layTatCa()
        val dsLich = db.lichHenDao().layTatCa()
        // Hoạt động = đăng tin + gửi tin nhắn + gửi yêu cầu mua
        val hoatDong = db.tinNhanDao().layHoatDong() +
            dsTin.map { ThongKe.HoatDong(it.nguoiBanId, it.ngayTao) } +
            dsLich.map { ThongKe.HoatDong(it.nguoiMuaId, it.ngayTao) }
        return DuLieuThongKe(dsTin, ndDao.layTatCa(), dsLich, baoCaoDao.layTatCa(), hoatDong)
    }

    // ===================== KIỂM DUYỆT (UC-51) =====================

    data class TinBiBaoCao(val tin: TinDang, val nguoiBan: NguoiDung?, val dsBaoCao: List<BaoCao>)

    /** Các tin đang có báo cáo chờ xử lý; tin đã tự ẩn (đủ 3 báo cáo) lên đầu. */
    suspend fun layTinBiBaoCao(): List<TinBiBaoCao> =
        baoCaoDao.layChoXuLy().groupBy { it.tinDangId }.mapNotNull { (tinId, ds) ->
            val tin = tinDao.layTheoId(tinId) ?: return@mapNotNull null
            TinBiBaoCao(tin, ndDao.layTheoId(tin.nguoiBanId), ds)
        }.sortedWith(compareByDescending<TinBiBaoCao> { it.tin.trangThai == HangSo.TIN_BI_AN }.thenByDescending { it.dsBaoCao.size })

    suspend fun demTinChoXuLy(): Int = baoCaoDao.layChoXuLy().map { it.tinDangId }.toSet().size

    /** Giữ tin: báo cáo bị bỏ qua, tin đang tạm ẩn được hiện lại, đếm báo cáo về 0. */
    suspend fun giuTin(tinId: Long) = db.withTransaction {
        val tin = tinDao.layTheoId(tinId) ?: error("Tin không còn tồn tại.")
        baoCaoDao.dongBaoCaoCuaTin(tinId, "BO_QUA")
        val dangAn = tin.trangThai == HangSo.TIN_BI_AN
        tinDao.capNhat(
            tin.copy(
                trangThai = if (dangAn) HangSo.TIN_DANG_BAN else tin.trangThai,
                soBaoCao = 0, ngayCapNhat = System.currentTimeMillis()
            )
        )
        if (dangAn) thongBaoNguoiBan(tin, "Tin đã được hiển thị lại", "Quản trị viên đã xem xét “${tin.tieuDe}” và cho hiển thị lại.")
    }

    /** Gỡ tin vĩnh viễn (BI_GO); có thể kèm khoá luôn người bán (UC-52). */
    suspend fun goTin(tinId: Long, khoaNguoiBan: Boolean) = db.withTransaction {
        val tin = tinDao.layTheoId(tinId) ?: error("Tin không còn tồn tại.")
        val lyDo = baoCaoDao.layTheoTin(tinId).filter { it.trangThai == "CHO_XU_LY" }
            .groupingBy { it.lyDo }.eachCount().maxByOrNull { it.value }?.key ?: "vi phạm quy định"
        baoCaoDao.dongBaoCaoCuaTin(tinId, "DA_XU_LY")
        goMotTin(tin, "Tin đã bị quản trị viên gỡ")
        thongBaoNguoiBan(tin, "Tin đã bị gỡ", "“${tin.tieuDe}” đã bị gỡ vì: $lyDo.")
        if (khoaNguoiBan) khoaTaiKhoan(tin.nguoiBanId)
    }

    // ===================== NGƯỜI DÙNG (UC-52) =====================

    data class DongNguoiDung(val nguoiDung: NguoiDung, val soTin: Int, val soDaBan: Int, val soBiBaoCao: Int)

    suspend fun layDsNguoiDung(): List<DongNguoiDung> {
        val dsTin = tinDao.layTatCa().filter { it.trangThai != HangSo.TIN_DA_XOA }
        val tinBiBaoCao = baoCaoDao.layTatCa().map { it.tinDangId }.toSet()
        return ndDao.layTatCa().map { nd ->
            val cuaNguoi = dsTin.filter { it.nguoiBanId == nd.id }
            DongNguoiDung(nd, cuaNguoi.size, cuaNguoi.count { it.trangThai == HangSo.TIN_DA_BAN }, cuaNguoi.count { it.id in tinBiBaoCao })
        }
    }

    /** Khoá: không đăng nhập được nữa, mọi tin còn hiện bị gỡ. Mở khoá: đăng nhập lại được (tin đã gỡ không tự hiện lại). */
    suspend fun doiKhoa(nguoiDungId: Long, khoa: Boolean) = db.withTransaction {
        val nd = ndDao.layTheoId(nguoiDungId) ?: error("Không tìm thấy người dùng.")
        if (nd.vaiTro == HangSo.VAI_TRO_QUAN_TRI) error("Không thể khoá tài khoản quản trị viên.")
        if (khoa) khoaTaiKhoan(nguoiDungId) else ndDao.capNhat(nd.copy(biKhoa = false))
    }

    private suspend fun khoaTaiKhoan(nguoiDungId: Long) {
        val nd = ndDao.layTheoId(nguoiDungId) ?: return
        ndDao.capNhat(nd.copy(biKhoa = true))
        for (t in tinDao.layConHienCuaNguoiBan(nguoiDungId)) goMotTin(t, "Người bán đã bị khoá tài khoản")
    }

    private suspend fun goMotTin(tin: TinDang, lyDoHuyHen: String) {
        tinDao.capNhat(tin.copy(trangThai = HangSo.TIN_BI_GO, dayTinDen = null, ngayCapNhat = System.currentTimeMillis()))
        GiaoDichRepository(appContext).huyLichCuaTin(tin.id, lyDoHuyHen)
    }

    private suspend fun thongBaoNguoiBan(tin: TinDang, tieuDe: String, noiDung: String) {
        db.thongBaoDao().them(
            ThongBao(nguoiNhanId = tin.nguoiBanId, loai = "KIEM_DUYET", tieuDe = tieuDe, noiDung = noiDung,
                loaiLienKet = "TIN_CUA_TOI", lienKetId = tin.id)
        )
    }

    // ===================== XEM DỮ LIỆU & XUẤT FILE =====================

    data class Bang(val ten: String, val cot: List<String>, val dong: List<List<String?>>, val tongDong: Int)

    /** Đọc một bảng bằng câu SQL thô (giống mở bằng DB Browser for SQLite), tối đa gioiHan dòng. */
    suspend fun docBang(ten: String, gioiHan: Int = 300): Bang = withContext(Dispatchers.IO) {
        require(ten in DS_BANG) { "Bảng không hợp lệ" }
        val tong = db.query("SELECT COUNT(*) FROM $ten", null).use { c -> if (c.moveToFirst()) c.getInt(0) else 0 }
        db.query("SELECT * FROM $ten LIMIT $gioiHan", null).use { c ->
            val cot = c.columnNames.toList()
            val dong = mutableListOf<List<String?>>()
            while (c.moveToNext()) dong += cot.indices.map { i -> if (c.isNull(i)) null else c.getString(i) }
            Bang(ten, cot, dong, tong)
        }
    }

    /** Xuất CẢ bảng ra CSV (UTF-8 có BOM để Excel đọc đúng tiếng Việt). */
    suspend fun xuatCsv(ten: String, ra: OutputStream): Int = withContext(Dispatchers.IO) {
        require(ten in DS_BANG) { "Bảng không hợp lệ" }
        var soDong = 0
        ra.bufferedWriter(Charsets.UTF_8).use { w ->
            w.write("﻿")
            db.query("SELECT * FROM $ten", null).use { c ->
                w.write(c.columnNames.joinToString(",") { oCsv(it) })
                w.write("\n")
                while (c.moveToNext()) {
                    w.write((0 until c.columnCount).joinToString(",") { i -> if (c.isNull(i)) "" else oCsv(c.getString(i)) })
                    w.write("\n")
                    soDong++
                }
            }
        }
        soDong
    }

    /** Ghi một chuỗi CSV có sẵn (ví dụ bảng KPI) ra file, thêm BOM. */
    suspend fun ghiChuoi(noiDung: String, ra: OutputStream) = withContext(Dispatchers.IO) {
        ra.bufferedWriter(Charsets.UTF_8).use { it.write("﻿" + noiDung) }
    }

    /** Chép nguyên file unimarket.db để mở bằng DB Browser for SQLite. */
    suspend fun xuatFileDb(ra: OutputStream): Long = withContext(Dispatchers.IO) {
        // Room dùng chế độ WAL: dồn phần ghi tạm (-wal) vào file chính trước khi chép
        db.query("PRAGMA wal_checkpoint(FULL)", null).use { it.moveToFirst() }
        val file: File = appContext.getDatabasePath(AppDatabase.TEN_FILE)
        file.inputStream().use { vao -> ra.use { vao.copyTo(it) } }
    }

    /** Bọc ô CSV trong dấu nháy kép nếu có dấu phẩy, nháy kép hoặc xuống dòng. */
    private fun oCsv(giaTri: String): String =
        if (giaTri.any { it == ',' || it == '"' || it == '\n' || it == '\r' }) "\"" + giaTri.replace("\"", "\"\"") + "\"" else giaTri
}

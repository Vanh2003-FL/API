package com.unimarket.xu_ly_nghiep_vu

import com.unimarket.mo_hinh.BaoCao
import com.unimarket.mo_hinh.LichHen
import com.unimarket.mo_hinh.NguoiDung
import com.unimarket.mo_hinh.TinDang
import com.unimarket.tien_ich.HangSo
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

/**
 * Tính các chỉ số KPI cho màn Quản trị (SCR-24, UC-54). Định nghĩa theo bảng KPI trong 02-phan-tich-kinh-te.md.
 * Chỉ gồm hàm thuần: nhận danh sách bản ghi, trả về con số → kiểm thử được và dễ giải thích khi bảo vệ.
 * Tỷ lệ trả về dạng 0..1, null nếu mẫu số bằng 0 (hiển thị "—").
 */
object ThongKe {

    enum class Khoang(val ten: String, val soNgay: Int?) {
        BAY_NGAY("7 ngày", 7),
        BA_MUOI_NGAY("30 ngày", 30),
        TAT_CA("Tất cả", null)
    }

    /** Một lượt hoạt động của người dùng (đăng tin, nhắn tin, đặt mua...) để đếm người dùng hoạt động. */
    data class HoatDong(val nguoiDungId: Long, val thoiGian: Long)

    data class KetQua(
        val giaoDichThanhCong: Int,          // KPI-01 số tin chuyển "Đã bán" trong kỳ (North Star)
        val nguoiDungMoi: Int,               // KPI-02
        val nguoiDungHoatDong: Int,          // KPI-04 (trong kỳ, thay cho MAU khi kỳ = 30 ngày)
        val soTinDang: Int,                  // số tin đăng trong kỳ (mẫu số của KPI-05)
        val tyLeBanDuoc: Double?,            // KPI-05 tin đăng trong kỳ đã bán / tin đăng trong kỳ
        val thoiGianBanTB: Double?,          // KPI-06 số ngày trung bình từ đăng tới bán
        val tyLeDungGiaGoiY: Double?,        // KPI-07 |giá rao − gợi ý| ≤ 10% / tin có gợi ý
        val mape: Double?,                   // KPI-08 sai số gợi ý giá TB |giá bán − gợi ý| / giá bán
        val soMauMape: Int,
        val tienTietKiem: Long,              // KPI-09 Σ (giá mua mới − giá bán) của tin đã bán có giá gốc
        val tyLeTinBiBaoCao: Double?,        // KPI-11
        val doanhThuMoPhong: Long,           // KPI-12 doanh thu đẩy tin (mô phỏng)
        val tyLeHenDuocXacNhan: Double?,     // KPI-13 lịch từng được chốt / tổng yêu cầu mua
        val tyLeHenThanhGiaoDich: Double?,   // KPI-14 lịch hoàn tất / lịch từng được chốt
        val soYeuCauMua: Int
    )

    /** Mốc bắt đầu kỳ thống kê (0 = tất cả). */
    fun tuNgay(khoang: Khoang, bayGio: Long): Long =
        khoang.soNgay?.let { bayGio - it * HangSo.MOT_NGAY } ?: 0L

    fun tinh(
        dsTin: List<TinDang>,
        dsNguoiDung: List<NguoiDung>,
        dsLich: List<LichHen>,
        dsBaoCao: List<BaoCao>,
        dsHoatDong: List<HoatDong>,
        tuNgay: Long
    ): KetQua {
        val tinTrongKy = dsTin.filter { it.ngayTao >= tuNgay }
        val daBanTrongKy = dsTin.filter { it.trangThai == HangSo.TIN_DA_BAN && (it.ngayBan ?: 0) >= tuNgay }

        val coGoiY = tinTrongKy.filter { (it.giaGoiY ?: 0) > 0 }
        val dungGoiY = coGoiY.count { abs(it.gia - it.giaGoiY!!) <= 0.10 * it.giaGoiY }

        val mauMape = daBanTrongKy.filter { (it.giaGoiY ?: 0) > 0 && (it.giaBan ?: 0) > 0 }
        val mape = mauMape.map { abs(it.giaBan!! - it.giaGoiY!!).toDouble() / it.giaBan }.takeIf { it.isNotEmpty() }?.average()

        val tietKiem = daBanTrongKy.filter { it.giaGoc != null }.sumOf { maxOf(it.giaGoc!! - (it.giaBan ?: it.gia), 0L) }

        val tinBiBaoCao = dsBaoCao.map { it.tinDangId }.toSet()
        val lichTrongKy = dsLich.filter { it.ngayTao >= tuNgay }
        val daChot = lichTrongKy.filter { it.ngayChot != null }

        return KetQua(
            giaoDichThanhCong = daBanTrongKy.size,
            nguoiDungMoi = dsNguoiDung.count { it.ngayTao >= tuNgay && it.vaiTro != HangSo.VAI_TRO_QUAN_TRI },
            nguoiDungHoatDong = dsHoatDong.filter { it.thoiGian >= tuNgay }.map { it.nguoiDungId }.toSet().size,
            soTinDang = tinTrongKy.size,
            tyLeBanDuoc = tiLe(tinTrongKy.count { it.trangThai == HangSo.TIN_DA_BAN }, tinTrongKy.size),
            thoiGianBanTB = daBanTrongKy.map { (it.ngayBan!! - it.ngayTao).toDouble() / HangSo.MOT_NGAY }
                .takeIf { it.isNotEmpty() }?.average(),
            tyLeDungGiaGoiY = tiLe(dungGoiY, coGoiY.size),
            mape = mape,
            soMauMape = mauMape.size,
            tienTietKiem = tietKiem,
            tyLeTinBiBaoCao = tiLe(tinTrongKy.count { it.id in tinBiBaoCao }, tinTrongKy.size),
            doanhThuMoPhong = tinTrongKy.sumOf { it.doanhThuDayTin },
            tyLeHenDuocXacNhan = tiLe(daChot.size, lichTrongKy.size),
            tyLeHenThanhGiaoDich = tiLe(daChot.count { it.trangThai == HangSo.LICH_HOAN_TAT }, daChot.size),
            soYeuCauMua = lichTrongKy.size
        )
    }

    private fun tiLe(tu: Int, mau: Int): Double? = if (mau == 0) null else tu.toDouble() / mau

    // ===================== DỮ LIỆU BIỂU ĐỒ =====================

    /** Một cột ngày: số tin đăng và số tin bán trong ngày đó. */
    data class Ngay(val nhan: String, val dang: Int, val ban: Int)

    /** Số tin đăng / bán theo từng ngày trong soNgay ngày gần nhất (cũ → mới). */
    fun theoNgay(dsTin: List<TinDang>, soNgay: Int, bayGio: Long): List<Ngay> {
        val dinhDang = SimpleDateFormat("dd/MM", Locale("vi"))
        fun dauNgay(t: Long) = Calendar.getInstance().apply {
            timeInMillis = t
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val homNay = dauNgay(bayGio)
        return (soNgay - 1 downTo 0).map { lui ->
            val batDau = Calendar.getInstance().apply { timeInMillis = homNay; add(Calendar.DAY_OF_YEAR, -lui) }.timeInMillis
            val ketThuc = Calendar.getInstance().apply { timeInMillis = batDau; add(Calendar.DAY_OF_YEAR, 1) }.timeInMillis
            Ngay(
                nhan = dinhDang.format(Date(batDau)),
                dang = dsTin.count { it.ngayTao in batDau until ketThuc },
                ban = dsTin.count { it.trangThai == HangSo.TIN_DA_BAN && (it.ngayBan ?: -1) in batDau until ketThuc }
            )
        }
    }

    /** Một thanh: tên danh mục con, số tin, mã danh mục cha (để tô màu). */
    data class PhanBo(val ten: String, val so: Int, val maCha: String)

    /** Phân bổ tin đăng trong kỳ theo danh mục con, nhiều nhất trước, tối đa `toiDa` thanh. */
    fun theoDanhMuc(dsTin: List<TinDang>, tuNgay: Long, toiDa: Int = 8): List<PhanBo> =
        dsTin.filter { it.ngayTao >= tuNgay }
            .groupBy { it.danhMucCon }
            .map { (ma, ds) -> PhanBo(HangSo.timDanhMucCon(ma)?.ten ?: ma, ds.size, HangSo.timChaCua(ma).ma) }
            .sortedByDescending { it.so }
            .take(toiDa)

    // ===================== XUẤT CSV =====================

    /** Bảng KPI + số liệu theo ngày dạng CSV (dán vào Excel để làm phần đánh giá trong báo cáo). */
    fun taoCsv(khoang: Khoang, kq: KetQua, dsNgay: List<Ngay>): String {
        fun pt(x: Double?) = x?.let { String.format(Locale.US, "%.1f", it * 100) } ?: ""
        fun so(x: Double?) = x?.let { String.format(Locale.US, "%.1f", it) } ?: ""
        val sb = StringBuilder()
        sb.append("Kỳ thống kê,${khoang.ten}\n\n")
        sb.append("Mã,Chỉ số,Giá trị,Đơn vị\n")
        sb.append("KPI-01,Giao dịch thành công,${kq.giaoDichThanhCong},giao dịch\n")
        sb.append("KPI-02,Người dùng mới,${kq.nguoiDungMoi},người\n")
        sb.append("KPI-04,Người dùng hoạt động,${kq.nguoiDungHoatDong},người\n")
        sb.append("KPI-05,Tỷ lệ bán được,${pt(kq.tyLeBanDuoc)},%\n")
        sb.append("KPI-06,Thời gian bán trung bình,${so(kq.thoiGianBanTB)},ngày\n")
        sb.append("KPI-07,Tỷ lệ dùng giá gợi ý,${pt(kq.tyLeDungGiaGoiY)},%\n")
        sb.append("KPI-08,Sai số gợi ý giá (MAPE),${pt(kq.mape)},% (${kq.soMauMape} mẫu)\n")
        sb.append("KPI-09,Tiền sinh viên tiết kiệm,${kq.tienTietKiem},đồng\n")
        sb.append("KPI-11,Tỷ lệ tin bị báo cáo,${pt(kq.tyLeTinBiBaoCao)},%\n")
        sb.append("KPI-12,Doanh thu mô phỏng,${kq.doanhThuMoPhong},đồng\n")
        sb.append("KPI-13,Tỷ lệ yêu cầu mua được xác nhận,${pt(kq.tyLeHenDuocXacNhan)},%\n")
        sb.append("KPI-14,Tỷ lệ hẹn thành giao dịch,${pt(kq.tyLeHenThanhGiaoDich)},%\n")
        sb.append("\nNgày,Tin đăng,Tin bán\n")
        dsNgay.forEach { sb.append("${it.nhan},${it.dang},${it.ban}\n") }
        return sb.toString()
    }
}

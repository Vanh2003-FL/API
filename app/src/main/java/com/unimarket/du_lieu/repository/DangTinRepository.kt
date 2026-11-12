package com.unimarket.du_lieu.repository

import android.content.Context
import androidx.room.withTransaction
import com.unimarket.du_lieu.database.AppDatabase
import com.unimarket.mo_hinh.ThongBao
import com.unimarket.mo_hinh.TinDang
import com.unimarket.tien_ich.DinhDang
import com.unimarket.tien_ich.HangSo
import com.unimarket.xu_ly_nghiep_vu.GoiYGia
import com.unimarket.xu_ly_nghiep_vu.KiemTraTinDang
import java.util.Calendar

/**
 * Xử lý phía NGƯỜI BÁN: gợi ý giá, đăng tin, sửa tin, đẩy tin, gia hạn, xoá.
 * (Đánh dấu đã bán nằm ở GiaoDichRepository vì liên quan lịch hẹn và người mua.)
 * Lỗi nghiệp vụ được ném ra kèm câu tiếng Việt để màn hình hiển thị.
 */
class DangTinRepository(context: Context) {

    private val db = AppDatabase.layDb(context)
    private val dao = db.tinDangDao()

    // ===================== GỢI Ý GIÁ =====================
    /**
     * Lấy mẫu từ database (cùng danh mục con, 90 ngày gần nhất) rồi chạy thuật toán GoiYGia.
     * boQuaId: id tin đang sửa (không tự so sánh với chính nó).
     */
    suspend fun goiYGia(
        tieuDe: String,
        danhMucCon: String,
        tinhTrang: String,
        thanhPho: String,
        giaGoc: Long?,
        namMua: Int?,
        boQuaId: Long = 0
    ): GoiYGia.KetQua {
        val tuNgay = System.currentTimeMillis() - GoiYGia.SO_NGAY_LAY_MAU * HangSo.MOT_NGAY
        val mau = GoiYGia.chonMau(tieuDe, dao.layMauGoiYGia(danhMucCon, tuNgay), boQuaId)
        return GoiYGia.tinh(
            dsMau = mau,
            tinhTrang = tinhTrang,
            thanhPho = thanhPho,
            laSach = HangSo.timChaCua(danhMucCon).ma == HangSo.SACH,
            maDanhMucCon = danhMucCon,
            giaGoc = giaGoc,
            namMua = namMua,
            namHienTai = Calendar.getInstance().get(Calendar.YEAR)
        )
    }

    // ===================== ĐĂNG / SỬA TIN =====================
    /** Đăng tin mới. Ghi luôn một thông báo "Tin đã đăng" cho người bán. Trả về id tin. */
    suspend fun dangTin(tin: TinDang): Long = db.withTransaction {
        KiemTraTinDang.tuKhoaCam(tin.tieuDe, tin.moTa)?.let {
            error("Tin có nội dung không được phép (“$it”). Vui lòng sửa lại.")
        }
        if (dao.demTheoNguoiBan(tin.nguoiBanId, HangSo.TIN_DANG_BAN) >= KiemTraTinDang.SO_TIN_DANG_BAN_TOI_DA) {
            error("Bạn đang có ${KiemTraTinDang.SO_TIN_DANG_BAN_TOI_DA} tin đang bán. Hãy đánh dấu đã bán hoặc xoá bớt tin cũ.")
        }
        val bayGio = System.currentTimeMillis()
        val id = dao.them(
            tin.copy(
                id = 0,
                trangThai = HangSo.TIN_DANG_BAN,
                tuKhoa = DinhDang.boDau("${tin.tieuDe} ${tin.moTa}"),
                xacNhanDiaDiemLuc = bayGio,
                ngayTao = bayGio,
                ngayCapNhat = bayGio,
                ngayHetHan = bayGio + HangSo.SO_NGAY_HIEN_TIN * HangSo.MOT_NGAY
            )
        )
        db.thongBaoDao().them(
            ThongBao(
                nguoiNhanId = tin.nguoiBanId,
                loai = "DA_DANG",
                tieuDe = "Tin đã đăng",
                noiDung = "“${tin.tieuDe}” · Địa điểm giao dịch: ${tin.diemGiaoDich}, ${tin.maTruong}",
                loaiLienKet = "TIN_CUA_TOI",
                lienKetId = id
            )
        )
        id
    }

    /** Sửa tin: giữ nguyên lượt xem, báo cáo, hạn đăng...; địa điểm đã được xác nhận lại. */
    suspend fun capNhatTin(tin: TinDang) {
        KiemTraTinDang.tuKhoaCam(tin.tieuDe, tin.moTa)?.let {
            error("Tin có nội dung không được phép (“$it”). Vui lòng sửa lại.")
        }
        val bayGio = System.currentTimeMillis()
        dao.capNhat(
            tin.copy(
                tuKhoa = DinhDang.boDau("${tin.tieuDe} ${tin.moTa}"),
                xacNhanDiaDiemLuc = bayGio,
                ngayCapNhat = bayGio
            )
        )
    }

    suspend fun layTheoId(id: Long): TinDang? = dao.layTheoId(id)

    // ===================== TIN CỦA TÔI =====================
    /** Tin của người bán; trước đó chuyển các tin quá hạn sang "Hết hạn". */
    suspend fun layTinCuaToi(nguoiBanId: Long): List<TinDang> {
        dao.capNhatHetHan(System.currentTimeMillis())
        return dao.layCuaNguoiBan(nguoiBanId)
    }

    /** Số người đã lưu từng tin: map id tin → số lượt lưu. */
    suspend fun demLuotLuu(dsTin: List<TinDang>): Map<Long, Int> =
        dsTin.associate { it.id to db.yeuThichDao().demTheoTin(it.id) }

    /** Đẩy tin (thanh toán mô phỏng): tin lên mục Nổi bật thêm soNgay ngày, cộng doanh thu. */
    suspend fun dayTin(tinId: Long, soNgay: Int, phi: Long) {
        val tin = dao.layTheoId(tinId) ?: return
        val bayGio = System.currentTimeMillis()
        val tuLuc = maxOf(bayGio, tin.dayTinDen ?: 0)
        dao.capNhat(
            tin.copy(
                dayTinDen = tuLuc + soNgay * HangSo.MOT_NGAY,
                soLanDayTin = tin.soLanDayTin + 1,
                doanhThuDayTin = tin.doanhThuDayTin + phi,
                ngayCapNhat = bayGio
            )
        )
    }

    /** Gia hạn thêm 30 ngày, tối đa 2 lần mỗi tin. */
    suspend fun giaHan(tinId: Long) {
        val tin = dao.layTheoId(tinId) ?: return
        if (tin.soLanGiaHan >= HangSo.SO_LAN_GIA_HAN_TOI_DA) error("Tin đã hết lượt gia hạn, hãy đăng tin mới.")
        val bayGio = System.currentTimeMillis()
        dao.capNhat(
            tin.copy(
                trangThai = HangSo.TIN_DANG_BAN,
                soLanGiaHan = tin.soLanGiaHan + 1,
                ngayHetHan = bayGio + HangSo.SO_NGAY_HIEN_TIN * HangSo.MOT_NGAY,
                ngayCapNhat = bayGio
            )
        )
    }

    /** Xoá mềm: chỉ đổi trạng thái DA_XOA để vẫn giữ số liệu thống kê. */
    suspend fun xoaTin(tinId: Long) {
        val tin = dao.layTheoId(tinId) ?: return
        dao.capNhat(tin.copy(trangThai = HangSo.TIN_DA_XOA, ngayCapNhat = System.currentTimeMillis()))
    }
}

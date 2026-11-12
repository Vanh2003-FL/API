package com.unimarket.xu_ly_nghiep_vu

import com.unimarket.mo_hinh.BaoCao
import com.unimarket.mo_hinh.LichHen
import com.unimarket.mo_hinh.NguoiDung
import com.unimarket.mo_hinh.TinDang
import com.unimarket.tien_ich.HangSo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Kiểm thử công thức KPI theo bảng trong 02-phan-tich-kinh-te.md (mục KPI). */
class ThongKeTest {

    private val bayGio = 1_800_000_000_000L
    private val NGAY = HangSo.MOT_NGAY
    private val tuNgay = bayGio - 7 * NGAY

    private fun tin(
        id: Long, gia: Long, truocNgay: Int, trangThai: String = HangSo.TIN_DANG_BAN,
        giaBan: Long? = null, banTruocNgay: Int? = null, giaGoiY: Long? = null, giaGoc: Long? = null, doanhThu: Long = 0
    ) = TinDang(
        id = id, nguoiBanId = 1, tieuDe = "Tin $id", danhMucCha = "SACH", danhMucCon = "GIAO_TRINH", gia = gia,
        tinhTrang = "TOT", maTruong = "NEU", thanhPho = "Hà Nội", diemGiaoDich = "Thư viện", trangThai = trangThai,
        giaBan = giaBan, ngayBan = banTruocNgay?.let { bayGio - it * NGAY }, giaGoiY = giaGoiY, giaGoc = giaGoc,
        doanhThuDayTin = doanhThu, ngayTao = bayGio - truocNgay * NGAY
    )

    private fun lich(id: Long, truocNgay: Int, trangThai: String, daChot: Boolean) = LichHen(
        id = id, cuocTroChuyenId = 1, tinDangId = 1, nguoiMuaId = 2, nguoiBanId = 1, maTruong = "NEU", diaDiem = "Thư viện",
        thoiGianHen = bayGio, trangThai = trangThai, nguoiDeXuatId = 2, ngayTao = bayGio - truocNgay * NGAY,
        ngayChot = if (daChot) bayGio - truocNgay * NGAY + 1000 else null
    )

    private val dsTin = listOf(
        // đăng 5 ngày trước, bán 3 ngày trước: 2 ngày; gợi ý 50k, bán 45k → sai số 5k/45k ≈ 11,1%; giá bìa 80k → tiết kiệm 35k
        tin(1, 48_000, 5, HangSo.TIN_DA_BAN, giaBan = 45_000, banTruocNgay = 3, giaGoiY = 50_000, giaGoc = 80_000),
        // đăng 6 ngày trước, bán 2 ngày trước: 4 ngày; gợi ý 100k, bán 80k → sai số 25%
        tin(2, 100_000, 6, HangSo.TIN_DA_BAN, giaBan = 80_000, banTruocNgay = 2, giaGoiY = 100_000),
        // đang bán, rao lệch gợi ý 30% → không tính "dùng giá gợi ý"; có đẩy tin 10k
        tin(3, 130_000, 1, giaGoiY = 100_000, doanhThu = 10_000),
        // đăng 20 ngày trước → ngoài kỳ 7 ngày
        tin(4, 60_000, 20, HangSo.TIN_DA_BAN, giaBan = 60_000, banTruocNgay = 15, giaGoiY = 60_000)
    )

    private fun tinh() = ThongKe.tinh(
        dsTin = dsTin,
        dsNguoiDung = listOf(
            NguoiDung(email = "a", matKhauMaHoa = "", hoTen = "A", ngayTao = bayGio - NGAY),
            NguoiDung(email = "b", matKhauMaHoa = "", hoTen = "B", ngayTao = bayGio - 30 * NGAY),
            NguoiDung(email = "qt", matKhauMaHoa = "", hoTen = "QT", vaiTro = HangSo.VAI_TRO_QUAN_TRI, ngayTao = bayGio)
        ),
        dsLich = listOf(
            lich(1, 4, HangSo.LICH_HOAN_TAT, true),
            lich(2, 3, HangSo.LICH_DA_CHOT, true),
            lich(3, 2, HangSo.LICH_DA_HUY, false),
            lich(4, 1, HangSo.LICH_CHO_XAC_NHAN, false)
        ),
        dsBaoCao = listOf(BaoCao(tinDangId = 3, nguoiBaoCaoId = 5, lyDo = "Spam")),
        dsHoatDong = listOf(
            ThongKe.HoatDong(1, bayGio - NGAY), ThongKe.HoatDong(1, bayGio - 2 * NGAY),
            ThongKe.HoatDong(2, bayGio - 3 * NGAY), ThongKe.HoatDong(9, bayGio - 30 * NGAY)
        ),
        tuNgay = tuNgay
    )

    @Test
    fun kpi01_02_04_demTrongKy() {
        val kq = tinh()
        assertEquals(2, kq.giaoDichThanhCong)   // tin 4 bán ngoài kỳ
        assertEquals(1, kq.nguoiDungMoi)        // không tính quản trị viên
        assertEquals(2, kq.nguoiDungHoatDong)   // người 1 và 2 (người 9 ngoài kỳ), mỗi người đếm 1 lần
    }

    @Test
    fun kpi05_06_tyLeBanVaThoiGianBan() {
        val kq = tinh()
        assertEquals(3, kq.soTinDang)
        assertEquals(2.0 / 3, kq.tyLeBanDuoc!!, 1e-9)
        assertEquals(3.0, kq.thoiGianBanTB!!, 1e-9)   // (2 + 4) / 2 ngày
    }

    @Test
    fun kpi07_08_09_giaGoiYVaTietKiem() {
        val kq = tinh()
        assertEquals(2.0 / 3, kq.tyLeDungGiaGoiY!!, 1e-9) // tin 1 (lệch 4%), tin 2 (0%) đạt; tin 3 lệch 30%
        assertEquals((5_000.0 / 45_000 + 20_000.0 / 80_000) / 2, kq.mape!!, 1e-9) // chia cho GIÁ BÁN thực tế
        assertEquals(2, kq.soMauMape)
        assertEquals(35_000L, kq.tienTietKiem)
    }

    @Test
    fun kpi11_12_13_14() {
        val kq = tinh()
        assertEquals(1.0 / 3, kq.tyLeTinBiBaoCao!!, 1e-9)
        assertEquals(10_000L, kq.doanhThuMoPhong)
        assertEquals(0.5, kq.tyLeHenDuocXacNhan!!, 1e-9)    // 2 lịch từng chốt / 4 yêu cầu
        assertEquals(0.5, kq.tyLeHenThanhGiaoDich!!, 1e-9)  // 1 hoàn tất / 2 từng chốt
    }

    @Test
    fun khongCoDuLieu_traVeNull() {
        val kq = ThongKe.tinh(emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), 0)
        assertNull(kq.tyLeBanDuoc)
        assertNull(kq.mape)
        assertNull(kq.thoiGianBanTB)
        assertEquals(0, kq.giaoDichThanhCong)
    }

    @Test
    fun bieuDoTheoNgay_dungSoCotVaSoLieu() {
        val ds = ThongKe.theoNgay(dsTin, 7, bayGio)
        assertEquals(7, ds.size)
        assertEquals(3, ds.sumOf { it.dang })  // tin 1, 2, 3 đăng trong 7 ngày (tin 4 ngoài)
        assertEquals(2, ds.sumOf { it.ban })
    }
}

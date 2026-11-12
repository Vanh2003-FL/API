package com.unimarket.xu_ly_nghiep_vu

import com.unimarket.mo_hinh.TinDang
import com.unimarket.xu_ly_nghiep_vu.GoiYGia.DoTinCay
import com.unimarket.xu_ly_nghiep_vu.GoiYGia.Mau
import com.unimarket.xu_ly_nghiep_vu.GoiYGia.PhamVi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Kiểm thử thuật toán gợi ý giá với đúng các ví dụ trong tài liệu thiết kế (docs/04, mục 4.6.4).
 * Chạy: gradlew testDebugUnitTest
 */
class GoiYGiaTest {

    /** 7 mẫu "Giáo trình Kinh tế vi mô" ở Hà Nội của ví dụ 4.6.4. */
    private val mauViDu = listOf(
        Mau(40_000, "TOT", daBan = true, thanhPho = "Hà Nội"),
        Mau(50_000, "NHU_MOI", daBan = false, thanhPho = "Hà Nội"),
        Mau(35_000, "KHA", daBan = true, thanhPho = "Hà Nội"),
        Mau(45_000, "TOT", daBan = false, thanhPho = "Hà Nội"),
        Mau(30_000, "CU", daBan = true, thanhPho = "Hà Nội"),
        Mau(55_000, "NHU_MOI", daBan = true, thanhPho = "Hà Nội"),
        Mau(150_000, "TOT", daBan = false, thanhPho = "Hà Nội")   // giá bất thường
    )

    private fun tinhViDu(tinhTrang: String = "TOT") =
        GoiYGia.tinh(mauViDu, tinhTrang, "Hà Nội", laSach = true, maDanhMucCon = "GIAO_TRINH", giaGoc = null, namMua = null, namHienTai = 2026)

    @Test
    fun viDu464_giaoTrinhKinhTeViMo_ra43000() {
        val kq = tinhViDu()
        assertEquals(43_000L, kq.giaGoiY)       // 50.000 × 0,85 = 42.500 → làm tròn 43.000
        assertEquals(43_000L, kq.giaThap)       // P25 = 50.000 × 0,85
        assertEquals(47_000L, kq.giaCao)        // P75 = 55.000 × 0,85 = 46.750 → 47.000
        assertEquals(6, kq.soMau)               // còn 6 mẫu sau khi loại ngoại lai
        assertEquals(1, kq.soBiLoai)            // loại mẫu 150.000đ
        assertEquals(4, kq.soDaBan)
        assertEquals(DoTinCay.TRUNG_BINH, kq.doTinCay)
        assertEquals(PhamVi.THANH_PHO, kq.phamVi)
    }

    @Test
    fun doiTinhTrang_giaGoiYDoiTheoHeSo() {
        assertEquals(50_000L, tinhViDu("NHU_MOI").giaGoiY)  // 50.000 × 1,00
        assertEquals(35_000L, tinhViDu("KHA").giaGoiY)      // 50.000 × 0,70
        assertEquals(25_000L, tinhViDu("CU").giaGoiY)       // 50.000 × 0,50
    }

    @Test
    fun tuPhanVi_vaLoaiNgoaiLai() {
        val g = listOf(47_059.0, 50_000.0, 50_000.0, 52_941.0, 55_000.0, 60_000.0, 176_471.0)
        val (q1, q3) = GoiYGia.tuPhanVi(g)
        assertEquals(50_000.0, q1, 0.5)
        assertEquals(60_000.0, q3, 0.5)          // khoảng chấp nhận [35.000; 75.000]
    }

    @Test
    fun itHon5MauCungThanhPho_dungToanQuoc() {
        val mau = mauViDu.take(4).map { it.copy(thanhPho = "TP.HCM") }
        val kq = GoiYGia.tinh(mau, "TOT", "Hà Nội", true, "GIAO_TRINH", null, null, 2026)
        assertEquals(PhamVi.TOAN_QUOC, kq.phamVi)
        assertEquals(DoTinCay.THAP, kq.doTinCay) // 3–4 mẫu
    }

    @Test
    fun khauHao_laptopViDuTaiLieu() {
        // Laptop giá gốc 15 triệu, mua 2024, tình trạng Tốt, năm 2026 → 15tr × 60% × 0,85
        val kq = GoiYGia.tinh(emptyList(), "TOT", "Hà Nội", false, "LAPTOP", 15_000_000, 2024, 2026)
        assertEquals(7_650_000L, kq.giaGoiY)
        assertEquals(6_500_000L, kq.giaThap)
        assertEquals(8_800_000L, kq.giaCao)
        assertEquals(DoTinCay.UOC_TINH, kq.doTinCay)
        assertEquals(PhamVi.KHAU_HAO, kq.phamVi)
    }

    @Test
    fun khauHao_sachTheoGiaBia() {
        // Sách giá bìa 80.000đ, tình trạng Tốt → 50% = 40.000 (khoảng 40%–60%)
        val kq = GoiYGia.tinh(emptyList(), "TOT", "Hà Nội", true, "THAM_KHAO", 80_000, null, 2026)
        assertEquals(40_000L, kq.giaGoiY)
        assertEquals(32_000L, kq.giaThap)
        assertEquals(48_000L, kq.giaCao)
    }

    @Test
    fun khauHao_toiThieuConLai20Phantram() {
        // Điện thoại mua 10 năm trước: 1 − 25% × 10 < 0 → giữ tối thiểu 20%
        val kq = GoiYGia.tinh(emptyList(), "NHU_MOI", "Hà Nội", false, "DIEN_THOAI", 10_000_000, 2016, 2026)
        assertEquals(2_000_000L, kq.giaGoiY)
    }

    @Test
    fun khongCoGiaGoc_khongDuDuLieu() {
        val kq = GoiYGia.tinh(emptyList(), "TOT", "Hà Nội", false, "LAPTOP", null, null, 2026)
        assertFalse(kq.coGoiY)
        assertEquals(DoTinCay.KHONG_DU, kq.doTinCay)
    }

    @Test
    fun lamTronTheoQuyTac() {
        assertEquals(43_000L, GoiYGia.lamTron(42_500.0))
        assertEquals(125_000L, GoiYGia.lamTron(123_400.0))      // bội 5.000
        assertEquals(6_500_000L, GoiYGia.lamTron(6_502_500.0))  // bội 50.000
    }

    @Test
    fun chonMau_theoDoTuongDongTieuDe() {
        fun tin(id: Long, tieuDe: String, trangThai: String = "DANG_BAN", giaBan: Long? = null) = TinDang(
            id = id, nguoiBanId = 1, tieuDe = tieuDe, danhMucCha = "SACH", danhMucCon = "GIAO_TRINH", gia = 45_000,
            tinhTrang = "TOT", maTruong = "NEU", thanhPho = "Hà Nội", diemGiaoDich = "Thư viện", trangThai = trangThai, giaBan = giaBan
        )
        val ds = listOf(
            tin(1, "Giáo trình Kinh tế vi mô – NEU"),
            tin(2, "Combo giáo trình Kinh tế vi mô + sách bài tập"),
            tin(3, "Sách Kinh tế lượng (Nguyễn Quang Dong)"),      // chỉ giống 2/6 từ → loại
            tin(4, "Giáo trình Kinh tế vi mô", "DA_BAN", giaBan = 40_000),
            tin(5, "Giáo trình Kinh tế vi mô", "DANG_BAN")          // chính tin đang sửa → bỏ qua
        )
        val mau = GoiYGia.chonMau("Giáo trình Kinh tế vi mô", ds, boQuaId = 5)
        assertEquals(3, mau.size)
        assertTrue(mau.any { it.daBan && it.gia == 40_000L })     // tin đã bán lấy giá bán thật
    }

    @Test
    fun canhBaoKhiGiaLechHon50Phantram() {
        val kq = tinhViDu()
        assertNull(GoiYGia.canhBaoLech(50_000, kq))
        assertTrue(GoiYGia.canhBaoLech(100_000, kq)!!.contains("cao"))
        assertTrue(GoiYGia.canhBaoLech(15_000, kq)!!.contains("thấp"))
    }
}

package com.unimarket.xu_ly_nghiep_vu

import com.unimarket.mo_hinh.NguoiDung
import com.unimarket.tien_ich.DinhDang
import com.unimarket.tien_ich.MaHoa
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Kiểm thử quy tắc hồ sơ, định dạng và mã hoá mật khẩu (chạy trên máy tính, không cần máy ảo). */
class KiemTraHoSoTest {

    private val hoSoDu = NguoiDung(
        email = "lan@unimarket.vn", matKhauMaHoa = "x", hoTen = "Nguyễn Thị Lan",
        soDienThoai = "0912345678", maTruong = "NEU", thanhPho = "Hà Nội", diemGiaoDich = "Thư viện"
    )

    @Test
    fun hoSoDayDu_khongThieuMucNao() {
        assertTrue(KiemTraHoSo.mucConThieu(hoSoDu).isEmpty())
    }

    @Test
    fun taiKhoanMoi_thieuBaMuc() {
        val moi = NguoiDung(email = "vy@unimarket.vn", matKhauMaHoa = "x", hoTen = "Đinh Khánh Vy")
        assertEquals(
            listOf(KiemTraHoSo.MUC_SO_DIEN_THOAI, KiemTraHoSo.MUC_TRUONG, KiemTraHoSo.MUC_DIEM_GIAO_DICH),
            KiemTraHoSo.mucConThieu(moi)
        )
    }

    @Test
    fun diemKhac_canGhiChuToiThieu10KyTu() {
        assertTrue(KiemTraHoSo.MUC_DIEM_GIAO_DICH in KiemTraHoSo.mucConThieu(hoSoDu.copy(diemGiaoDich = "Khác", ghiChuDiem = "KTX")))
        assertTrue(KiemTraHoSo.mucConThieu(hoSoDu.copy(diemGiaoDich = "Khác", ghiChuDiem = "Sảnh KTX khu B")).isEmpty())
    }

    @Test
    fun soDienThoai() {
        assertTrue(KiemTraHoSo.soDienThoaiHopLe("0912 345 678"))
        assertFalse(KiemTraHoSo.soDienThoaiHopLe("912345678"))   // thiếu số 0
        assertFalse(KiemTraHoSo.soDienThoaiHopLe("09123456789")) // 11 số
    }

    @Test
    fun dangKy() {
        assertNull(KiemTraHoSo.loiDangKy("Lan", "lan@gmail.com", "123456", "123456"))
        assertEquals("Email không hợp lệ", KiemTraHoSo.loiDangKy("Lan", "lan@", "123456", "123456"))
        assertEquals("Mật khẩu nhập lại không khớp", KiemTraHoSo.loiDangKy("Lan", "lan@gmail.com", "123456", "654321"))
    }

    @Test
    fun dinhDangGiaVaBoDau() {
        assertEquals("45.000đ", DinhDang.gia(45_000))
        assertEquals("7.650.000đ", DinhDang.gia(7_650_000))
        assertEquals("Tặng miễn phí", DinhDang.gia(0))
        assertEquals("giao trinh kinh te vi mo", DinhDang.boDau("Giáo trình Kinh tế vi mô"))
        assertEquals("do dung", DinhDang.boDau("Đồ dùng"))
        assertEquals("TL", DinhDang.vietTat("Nguyễn Thị Lan"))
    }

    @Test
    fun maHoaMatKhau_khongLuuMatKhauGoc() {
        val bam = MaHoa.bamMatKhau("Lan@UniMarket.vn", "123456")
        assertEquals(64, bam.length)
        assertEquals(bam, MaHoa.bamMatKhau("lan@unimarket.vn", "123456")) // email không phân biệt hoa thường
        assertNotEquals(bam, MaHoa.bamMatKhau("huy@unimarket.vn", "123456")) // cùng mật khẩu, khác người → khác chuỗi
    }
}

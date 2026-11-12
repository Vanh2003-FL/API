package com.unimarket.xu_ly_nghiep_vu

import com.unimarket.mo_hinh.LichHen
import com.unimarket.mo_hinh.TinDang
import com.unimarket.tien_ich.DinhDang
import com.unimarket.tien_ich.HangSo
import com.unimarket.xu_ly_nghiep_vu.QuyTacLichHen.Kieu
import com.unimarket.xu_ly_nghiep_vu.QuyTacLichHen.Nut
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

/** Kiểm thử quy tắc lịch hẹn BR-33 → BR-41 và nội dung thanh ghim theo bảng 3.3.1. */
class QuyTacLichHenTest {

    // Mốc cố định: 10:00 ngày 12/11/2026 để kết quả không phụ thuộc giờ chạy test
    private val bayGio = Calendar.getInstance().apply { set(2026, Calendar.NOVEMBER, 12, 10, 0, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis

    private val NGUOI_MUA = 2L
    private val NGUOI_BAN = 1L

    private val tin = TinDang(
        id = 10, nguoiBanId = NGUOI_BAN, tieuDe = "Giáo trình Kinh tế vi mô", danhMucCha = "SACH", danhMucCon = "GIAO_TRINH",
        gia = 45_000, tinhTrang = "TOT", maTruong = "NEU", thanhPho = "Hà Nội", diemGiaoDich = "Cổng chính"
    )

    private fun lich(trangThai: String = HangSo.LICH_CHO_XAC_NHAN, deXuat: Long = NGUOI_MUA) = LichHen(
        id = 5, cuocTroChuyenId = 3, tinDangId = 10, nguoiMuaId = NGUOI_MUA, nguoiBanId = NGUOI_BAN, maTruong = "NEU",
        diaDiem = "Thư viện", thoiGianHen = bayGio + 3 * HangSo.MOT_GIO, trangThai = trangThai, nguoiDeXuatId = deXuat
    )

    // ---------- BR-37: giờ hẹn ----------
    @Test
    fun gioHen_phaiSauItNhat30Phut() {
        assertNotNull(QuyTacLichHen.loiGioHen(bayGio + 10 * HangSo.MOT_PHUT, bayGio))
        assertNotNull(QuyTacLichHen.loiGioHen(bayGio - HangSo.MOT_GIO, bayGio))
        assertNull(QuyTacLichHen.loiGioHen(bayGio + 30 * HangSo.MOT_PHUT, bayGio))
    }

    @Test
    fun gioHen_khongQua7Ngay() {
        assertNull(QuyTacLichHen.loiGioHen(bayGio + 7 * HangSo.MOT_NGAY, bayGio))
        assertNotNull(QuyTacLichHen.loiGioHen(bayGio + 7 * HangSo.MOT_NGAY + HangSo.MOT_PHUT, bayGio))
    }

    // ---------- BR-34: địa điểm ----------
    @Test
    fun diaDiem_coSanHoacTuNhapDu10KyTu() {
        assertNull(QuyTacLichHen.loiDiaDiem("Thư viện"))
        assertNotNull(QuyTacLichHen.loiDiaDiem(""))
        assertNotNull(QuyTacLichHen.loiDiaDiem("KTX B"))
        assertNull(QuyTacLichHen.loiDiaDiem("Ghế đá trước nhà A2"))
    }

    // ---------- Gợi ý giờ ----------
    @Test
    fun goiYGio_buoiSangCoHomNay17h() {
        val goiY = QuyTacLichHen.goiYGioHen(bayGio).map { it.first }
        assertEquals(listOf("Hôm nay 17:00", "Mai 11:30", "Mai 17:00"), goiY)
    }

    @Test
    fun goiYGio_sau16h30BoHomNay() {
        val toi = bayGio + 7 * HangSo.MOT_GIO // 17:00
        val goiY = QuyTacLichHen.goiYGioHen(toi)
        assertEquals(listOf("Mai 11:30", "Mai 17:00", "Ngày kia 11:30"), goiY.map { it.first })
        assertTrue(goiY.all { QuyTacLichHen.loiGioHen(it.second, toi) == null })
    }

    // ---------- BR-35: ai được xác nhận ----------
    @Test
    fun nguoiDeXuat_khongTuXacNhan() {
        assertEquals(NGUOI_BAN, QuyTacLichHen.nguoiPhaiXacNhan(lich(deXuat = NGUOI_MUA)))
        assertEquals(NGUOI_MUA, QuyTacLichHen.nguoiPhaiXacNhan(lich(deXuat = NGUOI_BAN)))
        assertTrue(QuyTacLichHen.choToiXacNhan(lich(), NGUOI_BAN))
        assertFalse(QuyTacLichHen.choToiXacNhan(lich(), NGUOI_MUA))
        assertFalse(QuyTacLichHen.choToiXacNhan(lich(HangSo.LICH_DA_CHOT), NGUOI_BAN))
    }

    @Test
    fun dangHoatDong_chiChoVaDaChot() {
        assertTrue(QuyTacLichHen.dangHoatDong(lich(HangSo.LICH_CHO_XAC_NHAN)))
        assertTrue(QuyTacLichHen.dangHoatDong(lich(HangSo.LICH_DA_CHOT)))
        assertFalse(QuyTacLichHen.dangHoatDong(lich(HangSo.LICH_HOAN_TAT)))
        assertFalse(QuyTacLichHen.dangHoatDong(lich(HangSo.LICH_DA_HUY)))
        assertFalse(QuyTacLichHen.dangHoatDong(null))
    }

    // ---------- Thanh ghim theo bảng 3.3.1 ----------
    private fun ghim(l: LichHen?, laNguoiMua: Boolean, t: TinDang = tin) =
        QuyTacLichHen.thanhGhim(l, t, laNguoiMua, "Phạm Đức Huy", bayGio)

    @Test
    fun ghim_chuaDatMua() {
        assertEquals(listOf(Nut.DAT_MUA), ghim(null, true).dsNut)
        assertEquals(listOf(Nut.MOI_DAT_MUA), ghim(null, false).dsNut)
    }

    @Test
    fun ghim_choNguoiBan() {
        val mua = ghim(lich(), true)
        assertEquals("Chờ người bán xác nhận", mua.tieuDe)
        assertEquals(listOf(Nut.HUY_YEU_CAU), mua.dsNut)
        val ban = ghim(lich(), false)
        assertEquals("Phạm Đức Huy muốn mua", ban.tieuDe)
        assertEquals(listOf(Nut.XAC_NHAN, Nut.DOI, Nut.TU_CHOI), ban.dsNut)
        assertEquals(Kieu.CHO, ban.kieu)
    }

    @Test
    fun ghim_nguoiBanDeXuat_nguoiMuaDongY() {
        assertEquals(listOf(Nut.DONG_Y, Nut.DE_XUAT_KHAC, Nut.HUY_YEU_CAU), ghim(lich(deXuat = NGUOI_BAN), true).dsNut)
        assertEquals("Chờ người mua đồng ý", ghim(lich(deXuat = NGUOI_BAN), false).tieuDe)
    }

    @Test
    fun ghim_daChot_nguoiBanCoNutDaBan() {
        assertEquals(listOf(Nut.HUY_HEN), ghim(lich(HangSo.LICH_DA_CHOT), true).dsNut)
        assertEquals(listOf(Nut.DA_BAN, Nut.HUY_HEN), ghim(lich(HangSo.LICH_DA_CHOT), false).dsNut)
    }

    @Test
    fun ghim_tinDaChotVoiNguoiKhac_nguoiMuaXepHang() {
        val g = ghim(lich(), true, tin.copy(trangThai = HangSo.TIN_DANG_GIAO_DICH))
        assertEquals("Người bán đã hẹn với người khác", g.tieuDe)
        assertTrue(g.moTa.startsWith("Bạn đang xếp hàng"))
    }

    @Test
    fun ghim_hoanTat_chiNguoiMuaDanhGiaMotLan() {
        val daBan = tin.copy(trangThai = HangSo.TIN_DA_BAN, giaBan = 43_000)
        assertEquals(listOf(Nut.DANH_GIA), ghim(lich(HangSo.LICH_HOAN_TAT), true, daBan).dsNut)
        assertEquals("Đã bán 43.000đ", ghim(lich(HangSo.LICH_HOAN_TAT), false, daBan).tieuDe)
        assertTrue(ghim(lich(HangSo.LICH_HOAN_TAT).copy(soSao = 5), true, daBan).dsNut.isEmpty())
    }

    @Test
    fun ghim_daHuy_tinHetBanThiKhongDatLai() {
        val huy = lich(HangSo.LICH_DA_HUY).copy(lyDoHuy = "Không tiện giờ hẹn")
        assertEquals("Đã huỷ: Không tiện giờ hẹn", ghim(huy, true).tieuDe)
        assertEquals(listOf(Nut.DAT_MUA_LAI), ghim(huy, true).dsNut)
        assertTrue(ghim(huy, true, tin.copy(trangThai = HangSo.TIN_DA_BAN)).dsNut.isEmpty())
    }

    @Test
    fun nhanTrangThai_theoGocNhinNguoiXem() {
        assertEquals("Chờ bạn xác nhận", QuyTacLichHen.nhanTrangThai(lich(), NGUOI_BAN).first)
        assertEquals("Chờ người bán", QuyTacLichHen.nhanTrangThai(lich(), NGUOI_MUA).first)
        assertEquals("Đã mua", QuyTacLichHen.nhanTrangThai(lich(HangSo.LICH_HOAN_TAT), NGUOI_MUA).first)
        assertEquals("Chưa đặt mua", QuyTacLichHen.nhanTrangThai(null, NGUOI_MUA).first)
    }

    @Test
    fun traLoiNhanh_theoAiPhaiXacNhan() {
        // Người mua đề xuất: người bán (phải xác nhận) thấy "Ok bạn", người mua thấy lời nhắc
        assertEquals("Ok bạn", QuyTacLichHen.cauTraLoiNhanh(lich(), laNguoiMua = false).first())
        assertEquals("Bạn xác nhận giúp mình nhé", QuyTacLichHen.cauTraLoiNhanh(lich(), laNguoiMua = true).first())
        // Người bán đề xuất đổi: ngược lại
        assertEquals("Ok bạn", QuyTacLichHen.cauTraLoiNhanh(lich(deXuat = NGUOI_BAN), laNguoiMua = true).first())
    }

    // ---------- Định dạng giờ hẹn ----------
    @Test
    fun dinhDangGioHen() {
        assertEquals("13:00 hôm nay", DinhDang.gioHen(bayGio + 3 * HangSo.MOT_GIO, bayGio))
        assertEquals("11:30 mai", DinhDang.gioHen(bayGio + HangSo.MOT_NGAY + 90 * HangSo.MOT_PHUT, bayGio))
        // 14/11/2026 là thứ Bảy
        assertEquals("10:00 thứ 7, 14/11", DinhDang.gioHen(bayGio + 2 * HangSo.MOT_NGAY, bayGio))
    }
}

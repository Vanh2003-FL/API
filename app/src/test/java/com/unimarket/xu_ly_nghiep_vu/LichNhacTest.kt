package com.unimarket.xu_ly_nghiep_vu

import com.unimarket.mo_hinh.LichHen
import com.unimarket.mo_hinh.TinDang
import com.unimarket.tien_ich.HangSo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Kiểm thử tác vụ tự động: nhắc hẹn trước 1 giờ (BR-37), tự huỷ sau 24 giờ (BR-15), báo tin sắp hết hạn. */
class LichNhacTest {

    private val bayGio = 1_800_000_000_000L
    private val PHUT = HangSo.MOT_PHUT
    private val GIO = HangSo.MOT_GIO
    private val NGAY = HangSo.MOT_NGAY

    private fun lich(trangThai: String, hen: Long, hetHan: Long = bayGio + NGAY, daNhac: Long? = null) = LichHen(
        cuocTroChuyenId = 1, tinDangId = 1, nguoiMuaId = 2, nguoiBanId = 1, maTruong = "NEU", diaDiem = "Thư viện",
        thoiGianHen = hen, trangThai = trangThai, nguoiDeXuatId = 2, hetHanLuc = hetHan, daNhacLuc = daNhac
    )

    private fun tin(hetHan: Long, daBao: Boolean = false, trangThai: String = HangSo.TIN_DANG_BAN) = TinDang(
        nguoiBanId = 1, tieuDe = "Quạt", danhMucCha = "DO_DUNG", danhMucCon = "GIA_DUNG", gia = 100_000,
        tinhTrang = "TOT", maTruong = "NEU", thanhPho = "Hà Nội", diemGiaoDich = "Căng tin",
        trangThai = trangThai, ngayHetHan = hetHan, daBaoSapHetHan = daBao
    )

    @Test
    fun nhacHen_chiTrongKhoang1GioTruocVaChuaNhac() {
        assertFalse(LichNhac.canNhacHen(lich(HangSo.LICH_DA_CHOT, bayGio + 61 * PHUT), bayGio))  // còn hơn 1 giờ
        assertTrue(LichNhac.canNhacHen(lich(HangSo.LICH_DA_CHOT, bayGio + 60 * PHUT), bayGio))
        assertTrue(LichNhac.canNhacHen(lich(HangSo.LICH_DA_CHOT, bayGio + 5 * PHUT), bayGio))
        assertFalse(LichNhac.canNhacHen(lich(HangSo.LICH_DA_CHOT, bayGio - PHUT), bayGio))       // đã qua giờ hẹn
        assertFalse(LichNhac.canNhacHen(lich(HangSo.LICH_DA_CHOT, bayGio + 30 * PHUT, daNhac = bayGio - PHUT), bayGio))
        assertFalse(LichNhac.canNhacHen(lich(HangSo.LICH_CHO_XAC_NHAN, bayGio + 30 * PHUT), bayGio)) // chưa chốt
    }

    @Test
    fun tuHuy_khiQuaHanPhanHoi() {
        assertTrue(LichNhac.quaHanPhanHoi(lich(HangSo.LICH_CHO_XAC_NHAN, bayGio + NGAY, hetHan = bayGio), bayGio))
        assertFalse(LichNhac.quaHanPhanHoi(lich(HangSo.LICH_CHO_XAC_NHAN, bayGio + NGAY, hetHan = bayGio + PHUT), bayGio))
        assertFalse(LichNhac.quaHanPhanHoi(lich(HangSo.LICH_DA_CHOT, bayGio + NGAY, hetHan = bayGio - GIO), bayGio)) // đã chốt thì không tự huỷ
    }

    @Test
    fun baoSapHetHan_conToiDa3NgayVaMoiTinMotLan() {
        assertTrue(LichNhac.canBaoSapHetHan(tin(bayGio + 3 * NGAY), bayGio))
        assertFalse(LichNhac.canBaoSapHetHan(tin(bayGio + 3 * NGAY + GIO), bayGio))
        assertFalse(LichNhac.canBaoSapHetHan(tin(bayGio + NGAY, daBao = true), bayGio))
        assertFalse(LichNhac.canBaoSapHetHan(tin(bayGio - GIO), bayGio))                              // đã hết hạn
        assertFalse(LichNhac.canBaoSapHetHan(tin(bayGio + NGAY, trangThai = HangSo.TIN_DA_BAN), bayGio))
    }

    @Test
    fun conLai_vietChoNguoiDoc() {
        assertEquals("1 giờ", LichNhac.conLai(bayGio + 60 * PHUT, bayGio))
        assertEquals("1 giờ", LichNhac.conLai(bayGio + 56 * PHUT, bayGio))
        assertEquals("40 phút", LichNhac.conLai(bayGio + 40 * PHUT, bayGio))
        assertEquals(3, LichNhac.soNgayConLai(bayGio + 2 * NGAY + GIO, bayGio))
    }

    @Test
    fun baoThucTiepTheo_laMocSomNhat() {
        val dsLich = listOf(
            lich(HangSo.LICH_DA_CHOT, hen = bayGio + 5 * GIO),                         // nhắc lúc +4 giờ
            lich(HangSo.LICH_CHO_XAC_NHAN, hen = bayGio + 2 * NGAY, hetHan = bayGio + 3 * GIO), // tự huỷ lúc +3 giờ
            lich(HangSo.LICH_DA_CHOT, hen = bayGio + 2 * GIO, daNhac = bayGio)        // đã nhắc → bỏ qua
        )
        val dsTin = listOf(tin(bayGio + 10 * NGAY))                                      // báo lúc +7 ngày
        assertEquals(bayGio + 3 * GIO, LichNhac.thoiDiemKiemTraTiepTheo(dsLich, dsTin, bayGio))
        assertEquals(bayGio + 7 * NGAY, LichNhac.thoiDiemKiemTraTiepTheo(emptyList(), dsTin, bayGio))
        assertNull(LichNhac.thoiDiemKiemTraTiepTheo(emptyList(), emptyList(), bayGio))
    }

    @Test
    fun baoThucTiepTheo_mocDaQuaThiKiemTraSau1Phut() {
        val quaHan = lich(HangSo.LICH_CHO_XAC_NHAN, hen = bayGio + NGAY, hetHan = bayGio - GIO)
        assertEquals(bayGio + PHUT, LichNhac.thoiDiemKiemTraTiepTheo(listOf(quaHan), emptyList(), bayGio))
    }
}

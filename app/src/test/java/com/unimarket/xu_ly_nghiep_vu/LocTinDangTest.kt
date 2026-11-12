package com.unimarket.xu_ly_nghiep_vu

import com.unimarket.mo_hinh.TinDang
import com.unimarket.tien_ich.DinhDang
import com.unimarket.xu_ly_nghiep_vu.LocTinDang.DieuKien
import com.unimarket.xu_ly_nghiep_vu.LocTinDang.PhamVi
import com.unimarket.xu_ly_nghiep_vu.LocTinDang.SapXep
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Kiểm thử tìm kiếm không dấu, phạm vi, bộ lọc và sắp xếp. */
class LocTinDangTest {

    private fun tin(id: Long, tieuDe: String, con: String, cha: String, gia: Long, tt: String, truong: String, tp: String, tuoi: Long) =
        TinDang(
            id = id, nguoiBanId = 1, tieuDe = tieuDe, danhMucCha = cha, danhMucCon = con, gia = gia, tinhTrang = tt,
            maTruong = truong, thanhPho = tp, diemGiaoDich = "Thư viện", tuKhoa = DinhDang.boDau(tieuDe), ngayTao = 1_000_000L - tuoi
        )

    private val ds = listOf(
        tin(1, "Giáo trình Kinh tế vi mô – NEU", "GIAO_TRINH", "SACH", 45_000, "TOT", "NEU", "Hà Nội", 2),
        tin(2, "Laptop Dell Inspiron 14", "LAPTOP", "DIEN_TU", 7_650_000, "TOT", "NEU", "Hà Nội", 20),
        tin(3, "Quạt cây Senko 3 cánh", "GIA_DUNG", "DO_DUNG", 180_000, "TOT", "HUST", "Hà Nội", 5),
        tin(4, "Máy tính Casio FX-580VN X", "MAY_TINH_CAM_TAY", "DIEN_TU", 350_000, "NHU_MOI", "FTU", "Hà Nội", 24),
        tin(5, "Giáo trình Kinh tế vĩ mô (Mankiw)", "GIAO_TRINH", "SACH", 60_000, "NHU_MOI", "UEH", "TP.HCM", 8)
    )

    private fun ids(dk: DieuKien) = LocTinDang.loc(ds, dk).map { it.id }

    @Test
    fun timKhongDau_vanRaTinCoDau() {
        assertEquals(listOf(1L), ids(DieuKien(tuKhoa = "kinh te vi mo neu")))
        assertEquals(listOf(3L), ids(DieuKien(tuKhoa = "QUAT")))
    }

    @Test
    fun moiTuKhoaDeuPhaiXuatHien() {
        assertEquals(listOf(4L), ids(DieuKien(tuKhoa = "casio 580")))
        assertTrue(ids(DieuKien(tuKhoa = "casio 570")).isEmpty())
    }

    @Test
    fun timTheoTenDanhMucCon() {
        // "Máy tính cầm tay" là tên danh mục con, không có trong tiêu đề
        assertEquals(listOf(4L), ids(DieuKien(tuKhoa = "cam tay")))
    }

    @Test
    fun phamVi_truong_thanhPho_toanQuoc() {
        val goc = DieuKien(maTruong = "NEU", thanhPho = "Hà Nội")
        assertEquals(setOf(1L, 2L), ids(goc.copy(phamVi = PhamVi.TRUONG)).toSet())
        assertEquals(setOf(1L, 2L, 3L, 4L), ids(goc.copy(phamVi = PhamVi.THANH_PHO)).toSet())
        assertEquals(5, ids(goc.copy(phamVi = PhamVi.TOAN_QUOC)).size)
    }

    @Test
    fun boLoc_danhMuc_gia_tinhTrang() {
        assertEquals(setOf(1L, 5L), ids(DieuKien(danhMucCha = "SACH")).toSet())
        assertEquals(listOf(4L), ids(DieuKien(danhMucCha = "DIEN_TU", danhMucCon = "MAY_TINH_CAM_TAY")))
        assertEquals(setOf(1L, 5L), ids(DieuKien(giaToiDa = 100_000)).toSet())
        assertEquals(setOf(4L, 5L), ids(DieuKien(tinhTrang = setOf("NHU_MOI"))).toSet())
        assertEquals(3, DieuKien(danhMucCha = "SACH", giaToiDa = 1, tinhTrang = setOf("TOT")).soBoLoc())
    }

    @Test
    fun sapXep() {
        assertEquals(listOf(1L, 3L, 5L, 2L, 4L), ids(DieuKien(sapXep = SapXep.MOI_NHAT)))
        assertEquals(listOf(1L, 5L, 3L, 4L, 2L), ids(DieuKien(sapXep = SapXep.GIA_TANG)))
        assertEquals(2L, ids(DieuKien(sapXep = SapXep.GIA_GIAM)).first())
    }

    @Test
    fun noiBat_theoThoiHanDayTin() {
        assertTrue(LocTinDang.dangNoiBat(ds[0].copy(dayTinDen = 2_000), bayGio = 1_000))
        assertFalse(LocTinDang.dangNoiBat(ds[0].copy(dayTinDen = 500), bayGio = 1_000))
        assertFalse(LocTinDang.dangNoiBat(ds[0]))
    }
}

package com.unimarket.xu_ly_nghiep_vu

import com.unimarket.mo_hinh.TinDang
import com.unimarket.tien_ich.DinhDang
import com.unimarket.tien_ich.HangSo

/**
 * Lọc, tìm kiếm và sắp xếp tin đăng (dùng chung cho Trang chủ, Tìm kiếm, Danh mục).
 * - Tìm không dấu: gõ "kinh te vi mo" vẫn ra "Kinh tế vi mô".
 * - Mọi từ khoá đều phải xuất hiện (tìm "casio 580" chỉ ra máy Casio FX-580).
 * - Phạm vi: Trường tôi / Thành phố của tôi / Toàn quốc.
 */
object LocTinDang {

    enum class PhamVi { TRUONG, THANH_PHO, TOAN_QUOC }

    enum class SapXep(val ten: String) {
        MOI_NHAT("Mới nhất"),
        GIA_TANG("Giá tăng dần"),
        GIA_GIAM("Giá giảm dần")
    }

    /** Các mức "Giá tối đa" trong bộ lọc (null = không giới hạn). */
    val MUC_GIA: List<Pair<Long?, String>> = listOf(
        null to "Không giới hạn",
        50_000L to "Đến 50.000đ",
        200_000L to "Đến 200.000đ",
        1_000_000L to "Đến 1 triệu",
        5_000_000L to "Đến 5 triệu"
    )

    data class DieuKien(
        val tuKhoa: String = "",
        val phamVi: PhamVi = PhamVi.TOAN_QUOC,
        val maTruong: String = "",          // trường của người đang xem
        val thanhPho: String = "",          // thành phố của người đang xem
        val danhMucCha: String? = null,
        val danhMucCon: String? = null,
        val giaToiDa: Long? = null,
        val tinhTrang: Set<String> = emptySet(),
        val sapXep: SapXep = SapXep.MOI_NHAT
    ) {
        /** Số nhóm bộ lọc đang bật (hiện trên nút "Bộ lọc · 2"). */
        fun soBoLoc(): Int =
            (if (danhMucCha != null) 1 else 0) + (if (giaToiDa != null) 1 else 0) + (if (tinhTrang.isNotEmpty()) 1 else 0)
    }

    fun trongPhamVi(tin: TinDang, phamVi: PhamVi, maTruong: String, thanhPho: String): Boolean = when (phamVi) {
        PhamVi.TRUONG -> tin.maTruong == maTruong
        PhamVi.THANH_PHO -> tin.thanhPho == thanhPho
        PhamVi.TOAN_QUOC -> true
    }

    /** Tin có chứa TẤT CẢ các từ khoá không? So sánh trên chữ đã bỏ dấu. */
    fun khopTuKhoa(tin: TinDang, tuKhoa: String): Boolean {
        val cacTu = DinhDang.boDau(tuKhoa).split(Regex("\\s+")).filter { it.isNotBlank() }
        if (cacTu.isEmpty()) return true
        val tenDanhMucCon = HangSo.timDanhMucCon(tin.danhMucCon)?.ten ?: ""
        val noiDung = tin.tuKhoa + " " + DinhDang.boDau(tin.tieuDe + " " + tenDanhMucCon + " " + tin.thuocTinh)
        return cacTu.all { noiDung.contains(it) }
    }

    fun loc(ds: List<TinDang>, dk: DieuKien): List<TinDang> {
        val ketQua = ds.filter { tin ->
            trongPhamVi(tin, dk.phamVi, dk.maTruong, dk.thanhPho) &&
                khopTuKhoa(tin, dk.tuKhoa) &&
                (dk.danhMucCha == null || tin.danhMucCha == dk.danhMucCha) &&
                (dk.danhMucCon == null || tin.danhMucCon == dk.danhMucCon) &&
                (dk.giaToiDa == null || tin.gia <= dk.giaToiDa) &&
                (dk.tinhTrang.isEmpty() || tin.tinhTrang in dk.tinhTrang)
        }
        return when (dk.sapXep) {
            SapXep.MOI_NHAT -> ketQua.sortedByDescending { it.ngayTao }
            SapXep.GIA_TANG -> ketQua.sortedBy { it.gia }
            SapXep.GIA_GIAM -> ketQua.sortedByDescending { it.gia }
        }
    }

    /** Tin đang được đẩy (nổi bật) tại thời điểm hiện tại. */
    fun dangNoiBat(tin: TinDang, bayGio: Long = System.currentTimeMillis()): Boolean =
        (tin.dayTinDen ?: 0L) > bayGio
}

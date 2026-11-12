package com.unimarket.xu_ly_nghiep_vu

import com.unimarket.tien_ich.DinhDang

/**
 * Quy tắc kiểm tra tin đăng trước khi cho "Xem trước" / "Đăng tin".
 * Mỗi hàm trả về câu báo lỗi (để hiện dưới ô nhập) hoặc null nếu hợp lệ.
 */
object KiemTraTinDang {

    const val TIEU_DE_TOI_THIEU = 10
    const val TIEU_DE_TOI_DA = 80
    const val MO_TA_TOI_THIEU = 20
    const val GIA_TOI_DA = 50_000_000L
    const val SO_ANH_TOI_DA = 6
    const val SO_TIN_DANG_BAN_TOI_DA = 20

    /** Từ khoá hàng cấm / gian lận học tập (viết không dấu). Tin chứa các từ này bị chặn. */
    val TU_KHOA_CAM = listOf("ma tuy", "vu khi", "de thi", "dap an thi", "hang gia", "thuoc la dien tu")

    fun loiTieuDe(tieuDe: String): String? {
        val n = tieuDe.trim().length
        return when {
            n < TIEU_DE_TOI_THIEU -> "Tiêu đề cần ít nhất $TIEU_DE_TOI_THIEU ký tự"
            n > TIEU_DE_TOI_DA -> "Tiêu đề tối đa $TIEU_DE_TOI_DA ký tự"
            else -> null
        }
    }

    fun loiMoTa(moTa: String): String? =
        if (moTa.trim().length < MO_TA_TOI_THIEU) "Mô tả cần ít nhất $MO_TA_TOI_THIEU ký tự" else null

    /** giaNhap = null nghĩa là chưa nhập; tặng miễn phí thì không cần giá. */
    fun loiGia(giaNhap: Long?, tangMienPhi: Boolean): String? = when {
        tangMienPhi -> null
        giaNhap == null -> "Nhập giá bán hoặc chọn Tặng miễn phí"
        giaNhap <= 0 -> "Giá phải lớn hơn 0 (hoặc chọn Tặng miễn phí)"
        giaNhap > GIA_TOI_DA -> "Giá tối đa ${DinhDang.gia(GIA_TOI_DA)}"
        else -> null
    }

    /** Giáo trình bắt buộc ghi môn học để người mua tìm đúng. */
    fun loiMonHoc(maDanhMucCon: String, monHoc: String): String? =
        if (maDanhMucCon == "GIAO_TRINH" && monHoc.isBlank()) "Giáo trình cần ghi môn học" else null

    /** Trả về từ khoá cấm tìm thấy trong tiêu đề/mô tả, hoặc null. */
    fun tuKhoaCam(tieuDe: String, moTa: String): String? {
        val noiDung = DinhDang.boDau("$tieuDe $moTa")
        return TU_KHOA_CAM.firstOrNull { noiDung.contains(it) }
    }
}

package com.unimarket.xu_ly_nghiep_vu

import com.unimarket.mo_hinh.NguoiDung
import com.unimarket.tien_ich.HangSo

/**
 * Quy tắc kiểm tra hồ sơ và thông tin đăng ký.
 * Hồ sơ phải ĐẦY ĐỦ thì mới được đăng tin, đặt mua và nhắn tin (không cần xác thực thẻ sinh viên).
 */
object KiemTraHoSo {

    const val MUC_HO_TEN = "Họ và tên"
    const val MUC_SO_DIEN_THOAI = "Số điện thoại"
    const val MUC_TRUONG = "Trường"
    const val MUC_DIEM_GIAO_DICH = "Điểm giao dịch quen thuộc"

    val TAT_CA_MUC = listOf(MUC_HO_TEN, MUC_SO_DIEN_THOAI, MUC_TRUONG, MUC_DIEM_GIAO_DICH)

    /** Số điện thoại Việt Nam: 10 chữ số, bắt đầu bằng 0 (cho phép gõ có khoảng trắng). */
    fun soDienThoaiHopLe(soDienThoai: String): Boolean =
        Regex("^0\\d{9}$").matches(soDienThoai.replace(" ", ""))

    fun emailHopLe(email: String): Boolean =
        Regex("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$").matches(email.trim())

    /** Trả về danh sách mục còn thiếu; danh sách rỗng nghĩa là hồ sơ đầy đủ. */
    fun mucConThieu(nd: NguoiDung): List<String> {
        val thieu = mutableListOf<String>()
        if (nd.hoTen.trim().length < 2) thieu.add(MUC_HO_TEN)
        if (!soDienThoaiHopLe(nd.soDienThoai)) thieu.add(MUC_SO_DIEN_THOAI)
        if (HangSo.timTruong(nd.maTruong) == null) thieu.add(MUC_TRUONG)
        val thieuDiem = nd.diemGiaoDich.isBlank() ||
            (nd.diemGiaoDich == HangSo.DIEM_KHAC && nd.ghiChuDiem.trim().length < 10)
        if (thieuDiem) thieu.add(MUC_DIEM_GIAO_DICH)
        return thieu
    }

    fun hoSoDayDu(nd: NguoiDung): Boolean = mucConThieu(nd).isEmpty()

    /** Kiểm tra form đăng ký, trả về câu báo lỗi hoặc null nếu hợp lệ. */
    fun loiDangKy(hoTen: String, email: String, matKhau: String, nhapLai: String): String? = when {
        hoTen.trim().length < 2 -> "Họ và tên cần ít nhất 2 ký tự"
        !emailHopLe(email) -> "Email không hợp lệ"
        matKhau.length < 6 -> "Mật khẩu cần ít nhất 6 ký tự"
        matKhau != nhapLai -> "Mật khẩu nhập lại không khớp"
        else -> null
    }
}

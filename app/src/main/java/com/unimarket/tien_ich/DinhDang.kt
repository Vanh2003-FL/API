package com.unimarket.tien_ich

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.Normalizer

/** Các hàm định dạng chữ/số để hiển thị lên giao diện. */
object DinhDang {

    private val dinhDangSo = DecimalFormat("#,###", DecimalFormatSymbols().apply { groupingSeparator = '.' })

    /** 45000 → "45.000đ"; 0 → "Tặng miễn phí". */
    fun gia(soTien: Long): String =
        if (soTien == 0L) "Tặng miễn phí" else dinhDangSo.format(soTien) + "đ"

    /** Khoảng thời gian đã qua: "Vừa xong", "5 phút trước", "3 giờ trước", "2 ngày trước". */
    fun thoiGianTruoc(thoiDiem: Long, bayGio: Long = System.currentTimeMillis()): String {
        val phut = (bayGio - thoiDiem) / 60_000
        return when {
            phut < 1 -> "Vừa xong"
            phut < 60 -> "$phut phút trước"
            phut < 24 * 60 -> "${phut / 60} giờ trước"
            else -> "${phut / (24 * 60)} ngày trước"
        }
    }

    /** Bỏ dấu tiếng Việt và viết thường: "Kinh tế Vi mô" → "kinh te vi mo". Dùng cho tìm kiếm. */
    fun boDau(chuoi: String): String =
        Normalizer.normalize(chuoi, Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
            .replace('đ', 'd').replace('Đ', 'D')
            .lowercase()

    /** Chữ viết tắt cho ảnh đại diện: "Nguyễn Thị Lan" → "TL". */
    fun vietTat(hoTen: String): String {
        val tu = hoTen.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        if (tu.isEmpty()) return "?"
        return tu.takeLast(2).joinToString("") { it.first().uppercase() }
    }
}

package com.unimarket.tien_ich

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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

    /**
     * Giờ hẹn dễ đọc: "17:00 hôm nay", "11:30 mai", "09:00 thứ 5, 14/11".
     * bayGio truyền vào để unit test được.
     */
    fun gioHen(thoiDiem: Long, bayGio: Long = System.currentTimeMillis()): String {
        val gio = SimpleDateFormat("HH:mm", Locale("vi")).format(Date(thoiDiem))
        val lech = soNgayLech(bayGio, thoiDiem)
        return when (lech) {
            0 -> "$gio hôm nay"
            1 -> "$gio mai"
            -1 -> "$gio hôm qua"
            else -> {
                val lich = Calendar.getInstance().apply { timeInMillis = thoiDiem }
                val thu = lich.get(Calendar.DAY_OF_WEEK)
                val tenThu = if (thu == Calendar.SUNDAY) "CN" else "thứ $thu"
                "$gio $tenThu, " + SimpleDateFormat("dd/MM", Locale("vi")).format(Date(thoiDiem))
            }
        }
    }

    /** Số ngày lịch giữa hai thời điểm (bỏ qua giờ): cùng ngày = 0, ngày mai = 1. */
    fun soNgayLech(tu: Long, den: Long): Int {
        fun dauNgay(t: Long) = Calendar.getInstance().apply {
            timeInMillis = t
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        return Math.round((dauNgay(den) - dauNgay(tu)) / (24.0 * 60 * 60 * 1000)).toInt()
    }

    /** Giờ gửi tin nhắn: "14:20" nếu hôm nay, "12/11" nếu ngày khác. */
    fun gioNgan(thoiDiem: Long, bayGio: Long = System.currentTimeMillis()): String =
        SimpleDateFormat(if (soNgayLech(thoiDiem, bayGio) == 0) "HH:mm" else "dd/MM", Locale("vi")).format(Date(thoiDiem))

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

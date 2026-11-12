package com.unimarket.mo_hinh

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Bảng nguoi_dung: tài khoản và hồ sơ của sinh viên (và quản trị viên).
 * Hồ sơ "đầy đủ" khi có họ tên, số điện thoại, trường và điểm giao dịch quen thuộc.
 */
@Entity(tableName = "nguoi_dung", indices = [Index(value = ["email"], unique = true)])
data class NguoiDung(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val email: String,
    val matKhauMaHoa: String,            // mật khẩu đã băm SHA-256, không lưu mật khẩu gốc
    val hoTen: String,
    val soDienThoai: String = "",
    val zaloCungSo: Boolean = true,      // Zalo dùng chung số điện thoại
    val maTruong: String = "",           // ví dụ "NEU"
    val thanhPho: String = "",           // tự điền theo trường
    val diemGiaoDich: String = "",       // điểm quen thuộc: Cổng chính, Thư viện... hoặc "Khác"
    val ghiChuDiem: String = "",         // bắt buộc khi chọn "Khác"
    val anhDaiDien: String? = null,      // đường dẫn file ảnh trong bộ nhớ app
    val vaiTro: String = "NGUOI_DUNG",   // NGUOI_DUNG hoặc QUAN_TRI
    val biKhoa: Boolean = false,
    val ngayTao: Long = System.currentTimeMillis()
)

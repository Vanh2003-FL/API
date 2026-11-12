package com.unimarket.tien_ich

import java.security.MessageDigest

/** Mã hoá (băm) mật khẩu bằng SHA-256 trước khi lưu vào database. */
object MaHoa {

    /**
     * Ghép email vào trước mật khẩu ("muối") để hai người cùng mật khẩu vẫn có chuỗi băm khác nhau.
     * Kết quả là chuỗi 64 ký tự hex, không thể giải ngược ra mật khẩu gốc.
     */
    fun bamMatKhau(email: String, matKhau: String): String {
        val dauVao = email.trim().lowercase() + ":" + matKhau
        val bytes = MessageDigest.getInstance("SHA-256").digest(dauVao.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}

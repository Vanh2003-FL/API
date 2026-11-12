package com.unimarket.tien_ich

import android.content.Context

/**
 * Ghi nhớ ai đang đăng nhập (lưu id người dùng vào SharedPreferences).
 * Tắt app mở lại vẫn còn đăng nhập cho đến khi bấm "Đăng xuất".
 */
object PhienDangNhap {

    private const val TEN_FILE = "phien_dang_nhap"
    private const val KHOA_ID = "nguoi_dung_id"
    const val CHUA_DANG_NHAP = -1L

    private fun prefs(context: Context) =
        context.getSharedPreferences(TEN_FILE, Context.MODE_PRIVATE)

    fun luu(context: Context, nguoiDungId: Long) {
        prefs(context).edit().putLong(KHOA_ID, nguoiDungId).apply()
    }

    /** Id người đang đăng nhập, hoặc -1 nếu đang xem với tư cách khách. */
    fun layId(context: Context): Long = prefs(context).getLong(KHOA_ID, CHUA_DANG_NHAP)

    fun daDangNhap(context: Context): Boolean = layId(context) != CHUA_DANG_NHAP

    fun dangXuat(context: Context) {
        prefs(context).edit().remove(KHOA_ID).apply()
    }
}

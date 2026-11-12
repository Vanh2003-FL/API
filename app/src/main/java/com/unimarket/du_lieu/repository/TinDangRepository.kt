package com.unimarket.du_lieu.repository

import android.content.Context
import com.unimarket.du_lieu.database.AppDatabase
import com.unimarket.mo_hinh.TinDang
import com.unimarket.tien_ich.HangSo

/** Xử lý tin đăng: lấy danh sách, đếm theo trạng thái... */
class TinDangRepository(context: Context) {

    private val db = AppDatabase.layDb(context)
    private val dao = db.tinDangDao()

    /** Số tin người dùng đã lưu (yêu thích). */
    suspend fun demDaLuu(nguoiDungId: Long): Int = db.yeuThichDao().demCuaNguoiDung(nguoiDungId)

    suspend fun layTinMoi(gioiHan: Int = 40): List<TinDang> = dao.layTinMoi(gioiHan)

    suspend fun layTheoId(id: Long): TinDang? = dao.layTheoId(id)

    /** Số tin "Đang bán" và "Đã bán" của một người, dùng ở màn Tài khoản. */
    suspend fun demDangBanVaDaBan(nguoiBanId: Long): Pair<Int, Int> =
        dao.demTheoNguoiBan(nguoiBanId, HangSo.TIN_DANG_BAN) to dao.demTheoNguoiBan(nguoiBanId, HangSo.TIN_DA_BAN)
}

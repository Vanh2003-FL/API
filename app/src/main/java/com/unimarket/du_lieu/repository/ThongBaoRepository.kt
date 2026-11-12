package com.unimarket.du_lieu.repository

import android.content.Context
import com.unimarket.du_lieu.database.AppDatabase
import com.unimarket.mo_hinh.ThongBao
import com.unimarket.xu_ly_nghiep_vu.LichNhac

/** Đọc/ghi trung tâm thông báo và tính thời điểm cần chạy lại tác vụ tự động. */
class ThongBaoRepository(context: Context) {

    private val db = AppDatabase.layDb(context)
    private val dao = db.thongBaoDao()

    suspend fun layCuaNguoiDung(nguoiDungId: Long): List<ThongBao> = dao.layCuaNguoiDung(nguoiDungId)

    suspend fun demChuaDoc(nguoiDungId: Long): Int = dao.demChuaDoc(nguoiDungId)

    suspend fun layTheoId(id: Long): ThongBao? = dao.layTheoId(id)

    suspend fun danhDauDaDoc(id: Long) = dao.danhDauDaDoc(id)

    suspend fun danhDauTatCaDaDoc(nguoiDungId: Long) = dao.danhDauTatCaDaDoc(nguoiDungId)

    /** Lấy các thông báo chưa hiện lên máy rồi đánh dấu là đã hiện (mỗi thông báo chỉ bật lên một lần). */
    suspend fun layVaDanhDauDaDay(nguoiDungId: Long): List<ThongBao> {
        val ds = dao.layChuaDay(nguoiDungId)
        if (ds.isNotEmpty()) dao.danhDauDaDay(nguoiDungId)
        return ds
    }

    /** Thời điểm sớm nhất cần chạy lại tác vụ tự động (null = không còn gì). */
    suspend fun thoiDiemKiemTraTiepTheo(bayGio: Long): Long? =
        LichNhac.thoiDiemKiemTraTiepTheo(db.lichHenDao().layDangHoatDong(), db.tinDangDao().layDangBanChuaBaoHetHan(), bayGio)
}

package com.unimarket.du_lieu.repository

import android.content.Context
import com.unimarket.du_lieu.database.AppDatabase
import com.unimarket.mo_hinh.DanhMuc
import com.unimarket.tien_ich.DinhDang
import com.unimarket.tien_ich.HangSo

/**
 * Quản lý danh mục con (UC-53, FR-53): thêm, sửa tên/icon/tỷ lệ khấu hao, ẩn/hiện.
 * Sau mỗi thay đổi nạp lại bộ nhớ đệm HangSo.DS_DANH_MUC để mọi màn hình dùng ngay.
 */
class DanhMucRepository(context: Context) {

    private val dao = AppDatabase.layDb(context).danhMucDao()

    companion object {
        const val KHAU_HAO_TOI_DA = 0.6

        /** Chuyển danh mục mặc định trong code thành các dòng của bảng danh_muc (dùng khi nạp dữ liệu mẫu). */
        fun dongMacDinh(): List<DanhMuc> = HangSo.DANH_MUC_MAC_DINH.flatMap { cha ->
            cha.dsCon.mapIndexed { i, con ->
                DanhMuc(con.ma, cha.ma, con.ten, HangSo.tenIcon(con.icon), con.khauHaoNam, thuTu = i)
            }
        }
    }

    /** Đọc bảng danh_muc lên HangSo.DS_DANH_MUC. Gọi khi mở app và sau mỗi lần sửa. */
    suspend fun napVaoBoNho() {
        val ds = dao.layTatCa()
        if (ds.isEmpty()) return // database chưa nạp dữ liệu mẫu → giữ danh mục mặc định
        HangSo.datDanhMucCon(
            ds.groupBy { it.maCha }.mapValues { (_, dsCon) ->
                dsCon.map { HangSo.DanhMucCon(it.ma, it.ten, HangSo.iconTheoTen(it.tenIcon), it.khauHaoNam, it.an) }
            }
        )
    }

    suspend fun layTatCa(): List<DanhMuc> = dao.layTatCa()

    suspend fun demTinTheoCon(): Map<String, Int> = dao.demTinTheoCon().associate { it.ma to it.so }

    /** Thêm danh mục con mới vào một danh mục cha. Trả về mã vừa tạo. */
    suspend fun them(maCha: String, ten: String, tenIcon: String, khauHaoNam: Double): String {
        val dsCu = dao.layTatCa()
        kiemTra(maCha, ten, khauHaoNam, dsCu.filter { it.maCha == maCha }, boQuaMa = null)
        // Mã sinh từ tên: "Máy ảnh" → "CON_MAY_ANH" (thêm số nếu trùng)
        val goc = "CON_" + DinhDang.boDau(ten).uppercase().replace(Regex("[^A-Z0-9]+"), "_").trim('_')
        var ma = goc
        var i = 2
        while (dsCu.any { it.ma == ma }) ma = goc + "_" + i++
        dao.them(
            DanhMuc(
                ma = ma, maCha = maCha, ten = ten.trim(), tenIcon = tenIcon,
                khauHaoNam = if (maCha == HangSo.SACH) 0.0 else khauHaoNam,
                thuTu = (dsCu.filter { it.maCha == maCha }.maxOfOrNull { it.thuTu } ?: -1) + 1
            )
        )
        napVaoBoNho()
        return ma
    }

    /** Sửa tên, icon, tỷ lệ khấu hao, ẩn/hiện. Không đổi được danh mục cha (giống BR-08 với tin). */
    suspend fun capNhat(ma: String, ten: String, tenIcon: String, khauHaoNam: Double, an: Boolean) {
        val cu = dao.layTheoMa(ma) ?: error("Không tìm thấy danh mục.")
        val cungCha = dao.layTatCa().filter { it.maCha == cu.maCha }
        kiemTra(cu.maCha, ten, khauHaoNam, cungCha, boQuaMa = ma)
        if (an && cungCha.none { it.ma != ma && !it.an }) error("Phải còn ít nhất một danh mục con đang hiện trong nhóm này.")
        dao.capNhat(
            cu.copy(ten = ten.trim(), tenIcon = tenIcon, khauHaoNam = if (cu.maCha == HangSo.SACH) 0.0 else khauHaoNam, an = an)
        )
        napVaoBoNho()
    }

    private fun kiemTra(maCha: String, ten: String, khauHaoNam: Double, cungCha: List<DanhMuc>, boQuaMa: String?) {
        val t = ten.trim()
        if (HangSo.timDanhMuc(maCha) == null) error("Danh mục cha không hợp lệ.")
        if (t.length < 2 || t.length > 40) error("Tên danh mục dài từ 2 đến 40 ký tự.")
        if (cungCha.any { it.ma != boQuaMa && DinhDang.boDau(it.ten) == DinhDang.boDau(t) }) error("Đã có danh mục “$t” trong nhóm này.")
        if (khauHaoNam < 0 || khauHaoNam > KHAU_HAO_TOI_DA) error("Tỷ lệ khấu hao từ 0% đến ${(KHAU_HAO_TOI_DA * 100).toInt()}%/năm.")
    }
}

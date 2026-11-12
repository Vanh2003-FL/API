package com.unimarket.du_lieu.repository

import android.content.Context
import com.unimarket.du_lieu.database.AppDatabase
import com.unimarket.mo_hinh.NguoiDung
import com.unimarket.tien_ich.MaHoa
import com.unimarket.tien_ich.PhienDangNhap
import com.unimarket.xu_ly_nghiep_vu.KiemTraHoSo

/**
 * Xử lý tài khoản: đăng nhập, đăng ký, lấy và lưu hồ sơ.
 * Màn hình chỉ gọi các hàm ở đây, không gọi thẳng DAO.
 * Khi có lỗi nghiệp vụ, hàm sẽ ném lỗi kèm câu tiếng Việt để màn hình hiện ra.
 */
class NguoiDungRepository(context: Context) {

    private val appContext = context.applicationContext
    private val dao = AppDatabase.layDb(appContext).nguoiDungDao()

    /** Đăng nhập: so sánh chuỗi băm mật khẩu, thành công thì ghi nhớ phiên. */
    suspend fun dangNhap(email: String, matKhau: String): NguoiDung {
        val nd = dao.timTheoEmail(email.trim().lowercase())
        if (nd == null || nd.matKhauMaHoa != MaHoa.bamMatKhau(email, matKhau)) {
            error("Sai email hoặc mật khẩu")
        }
        if (nd.biKhoa) error("Tài khoản đã bị khoá, vui lòng liên hệ quản trị viên")
        PhienDangNhap.luu(appContext, nd.id)
        return nd
    }

    /** Đăng ký tài khoản mới (hồ sơ còn thiếu, người dùng bổ sung sau). */
    suspend fun dangKy(hoTen: String, email: String, matKhau: String, nhapLai: String): NguoiDung {
        KiemTraHoSo.loiDangKy(hoTen, email, matKhau, nhapLai)?.let { error(it) }
        val emailChuan = email.trim().lowercase()
        if (dao.timTheoEmail(emailChuan) != null) error("Email này đã được đăng ký")
        val moi = NguoiDung(
            email = emailChuan,
            matKhauMaHoa = MaHoa.bamMatKhau(emailChuan, matKhau),
            hoTen = hoTen.trim()
        )
        val id = dao.them(moi)
        PhienDangNhap.luu(appContext, id)
        return moi.copy(id = id)
    }

    /** Người đang đăng nhập, hoặc null nếu là khách. */
    suspend fun layNguoiDangNhap(): NguoiDung? {
        val id = PhienDangNhap.layId(appContext)
        if (id == PhienDangNhap.CHUA_DANG_NHAP) return null
        return dao.layTheoId(id)
    }

    suspend fun layTheoId(id: Long): NguoiDung? = dao.layTheoId(id)

    suspend fun luuHoSo(nguoiDung: NguoiDung) = dao.capNhat(nguoiDung)

    fun dangXuat() = PhienDangNhap.dangXuat(appContext)
}

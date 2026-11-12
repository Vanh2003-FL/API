package com.unimarket.man_hinh.quan_tri

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.unimarket.du_lieu.repository.NguoiDungRepository
import com.unimarket.mo_hinh.NguoiDung
import com.unimarket.tien_ich.HangSo
import kotlinx.coroutines.launch
import java.util.Locale

/** Dùng chung cho các màn quản trị: kiểm tra quyền và định dạng số liệu. */
object QuyenQuanTri {

    /** Chỉ tài khoản QUAN_TRI mới vào được; người khác bị đóng màn ngay. */
    fun kiemTra(activity: AppCompatActivity, tiepTuc: (NguoiDung) -> Unit) {
        activity.lifecycleScope.launch {
            val nd = NguoiDungRepository(activity).layNguoiDangNhap()
            if (nd?.vaiTro != HangSo.VAI_TRO_QUAN_TRI) {
                Toast.makeText(activity, "Chỉ quản trị viên mới vào được mục này", Toast.LENGTH_SHORT).show()
                activity.finish()
            } else {
                tiepTuc(nd)
            }
        }
    }

    /** 0.456 → "46%"; null → "—". */
    fun phanTram(x: Double?): String = x?.let { "${Math.round(it * 100)}%" } ?: "—"

    /** 4.25 → "4,3"; null → "—". */
    fun soLe(x: Double?): String = x?.let { String.format(Locale.US, "%.1f", it).replace('.', ',') } ?: "—"
}

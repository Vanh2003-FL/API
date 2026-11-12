package com.unimarket.man_hinh.chi_tiet_san_pham

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.unimarket.du_lieu.repository.TinDangRepository
import com.unimarket.man_hinh.tai_khoan.BoSungHoSo
import kotlinx.coroutines.launch

/** Bấm trái tim ở bất kỳ màn nào: cần đăng nhập, lưu/bỏ lưu vào bảng yeu_thich rồi báo lại cho màn gọi. */
object LuuTin {

    fun doi(activity: AppCompatActivity, tinDangId: Long, xong: (daLuu: Boolean) -> Unit) {
        BoSungHoSo.canDangNhap(activity) { nd ->
            activity.lifecycleScope.launch {
                val daLuu = TinDangRepository(activity).doiYeuThich(nd.id, tinDangId)
                Toast.makeText(activity, if (daLuu) "Đã lưu tin" else "Đã bỏ lưu", Toast.LENGTH_SHORT).show()
                xong(daLuu)
            }
        }
    }
}

package com.unimarket.tien_ich

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/**
 * Từ Android 15, app luôn vẽ tràn lên thanh trạng thái và thanh điều hướng.
 * Hàm này chừa khoảng trống phía trên/dưới (và chỗ cho bàn phím) để nội dung không bị che.
 * Gọi một lần sau setContentView(binding.root) ở mỗi Activity.
 */
object CanhLe {

    fun chuaVienHeThong(goc: View) {
        ViewCompat.setOnApplyWindowInsetsListener(goc) { v, insets ->
            val vien = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val banPhim = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(vien.left, vien.top, vien.right, maxOf(vien.bottom, banPhim.bottom))
            WindowInsetsCompat.CONSUMED
        }
    }
}

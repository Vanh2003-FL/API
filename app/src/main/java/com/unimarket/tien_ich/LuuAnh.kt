package com.unimarket.tien_ich

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.ImageView
import java.io.File

/**
 * Xử lý ảnh: chép ảnh người dùng chọn vào bộ nhớ riêng của app và hiển thị lại.
 * Database chỉ lưu ĐƯỜNG DẪN file ảnh, không lưu cả ảnh.
 */
object LuuAnh {

    /** Chép ảnh từ thư viện (Uri) vào thư mục files/anh của app, trả về đường dẫn file mới. */
    fun chepVaoApp(context: Context, uri: Uri, tenFile: String): String {
        val thuMuc = File(context.filesDir, "anh").apply { mkdirs() }
        val fileDich = File(thuMuc, tenFile)
        context.contentResolver.openInputStream(uri).use { vao ->
            requireNotNull(vao) { "Không đọc được ảnh" }
            fileDich.outputStream().use { ra -> vao.copyTo(ra) }
        }
        return fileDich.absolutePath
    }

    /**
     * Hiện ảnh từ file lên ImageView. Ảnh được thu nhỏ trước khi đọc để không tốn bộ nhớ.
     * Trả về false nếu file không tồn tại (khi đó nơi gọi sẽ hiện icon thay thế).
     */
    fun hienAnh(imageView: ImageView, duongDan: String?, kichThuocToiDa: Int = 600): Boolean {
        if (duongDan.isNullOrBlank() || !File(duongDan).exists()) return false
        val kichThuoc = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(duongDan, kichThuoc)
        var tyLe = 1
        while (kichThuoc.outWidth / (tyLe * 2) >= kichThuocToiDa && kichThuoc.outHeight / (tyLe * 2) >= kichThuocToiDa) {
            tyLe *= 2
        }
        val bitmap = BitmapFactory.decodeFile(duongDan, BitmapFactory.Options().apply { inSampleSize = tyLe })
            ?: return false
        imageView.imageTintList = null
        imageView.setImageBitmap(bitmap)
        return true
    }
}

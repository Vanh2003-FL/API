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

    /** Ảnh mẫu đóng gói trong app (thư mục assets) được lưu dạng "asset://anh_mau/dell_1.jpg". */
    const val TIEN_TO_ASSET = "asset://"

    /**
     * Hiện ảnh lên ImageView. Ảnh được thu nhỏ trước khi đọc để không tốn bộ nhớ.
     * Đường dẫn là file trong bộ nhớ app (ảnh người dùng chụp/chọn) hoặc "asset://..." (ảnh mẫu).
     * Trả về false nếu không đọc được (khi đó nơi gọi sẽ hiện icon thay thế).
     */
    fun hienAnh(imageView: ImageView, duongDan: String?, kichThuocToiDa: Int = 600): Boolean {
        if (duongDan.isNullOrBlank()) return false
        val laAsset = duongDan.startsWith(TIEN_TO_ASSET)
        if (!laAsset && !File(duongDan).exists()) return false
        val assets = imageView.context.assets
        // Đọc ảnh 2 lần: lần 1 chỉ lấy kích thước, lần 2 đọc ảnh đã thu nhỏ
        fun doc(tuyChon: BitmapFactory.Options) =
            if (laAsset) runCatching { assets.open(duongDan.removePrefix(TIEN_TO_ASSET)).use { BitmapFactory.decodeStream(it, null, tuyChon) } }.getOrNull()
            else BitmapFactory.decodeFile(duongDan, tuyChon)

        val kichThuoc = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        doc(kichThuoc)
        if (kichThuoc.outWidth <= 0) return false
        var tyLe = 1
        while (kichThuoc.outWidth / (tyLe * 2) >= kichThuocToiDa && kichThuoc.outHeight / (tyLe * 2) >= kichThuocToiDa) {
            tyLe *= 2
        }
        val bitmap = doc(BitmapFactory.Options().apply { inSampleSize = tyLe }) ?: return false
        imageView.imageTintList = null
        imageView.setImageBitmap(bitmap)
        return true
    }
}

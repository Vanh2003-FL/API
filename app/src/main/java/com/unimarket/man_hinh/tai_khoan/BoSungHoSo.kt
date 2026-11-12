package com.unimarket.man_hinh.tai_khoan

import android.content.Intent
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.unimarket.R
import com.unimarket.databinding.BottomSheetBoSungHoSoBinding
import com.unimarket.du_lieu.repository.NguoiDungRepository
import com.unimarket.man_hinh.dang_nhap.DangNhapActivity
import com.unimarket.xu_ly_nghiep_vu.KiemTraHoSo
import kotlinx.coroutines.launch

/**
 * "Cổng chặn hồ sơ": gọi trước mọi thao tác cần hồ sơ đầy đủ (đăng tin, đặt mua, nhắn tin).
 * - Chưa đăng nhập  → hỏi có muốn đăng nhập không.
 * - Hồ sơ thiếu     → hiện bảng liệt kê mục thiếu, bấm "Bổ sung ngay" mở màn Hồ sơ.
 *                     Lưu đủ xong, việc đang dở được chạy tiếp tự động.
 * - Hồ sơ đầy đủ    → làm luôn.
 */
object BoSungHoSo {

    /** Việc người dùng đang muốn làm, chờ bổ sung hồ sơ xong sẽ chạy. */
    private var viecDangCho: (() -> Unit)? = null

    fun kiemTra(activity: AppCompatActivity, viec: () -> Unit) {
        activity.lifecycleScope.launch {
            val nd = NguoiDungRepository(activity).layNguoiDangNhap()
            if (nd == null) {
                hoiDangNhap(activity)
                return@launch
            }
            val thieu = KiemTraHoSo.mucConThieu(nd)
            if (thieu.isEmpty()) {
                viec()
            } else {
                viecDangCho = viec
                hienBangNhac(activity, thieu)
            }
        }
    }

    /** Màn Hồ sơ gọi hàm này khi đã lưu đủ thông tin. */
    fun chayViecDangCho() {
        val viec = viecDangCho
        viecDangCho = null
        viec?.invoke()
    }

    fun boQua() {
        viecDangCho = null
    }

    // ------------------------------------------------------------------
    private fun hienBangNhac(activity: AppCompatActivity, thieu: List<String>) {
        val bang = BottomSheetDialog(activity)
        val b = BottomSheetBoSungHoSoBinding.inflate(activity.layoutInflater)
        bang.setContentView(b.root)

        // Liệt kê 4 mục: mục đã có (dấu tích xanh), mục thiếu (dấu X đỏ)
        for (muc in KiemTraHoSo.TAT_CA_MUC) {
            val coThieu = muc in thieu
            val dong = TextView(activity).apply {
                text = muc
                textSize = 15f
                compoundDrawablePadding = 12
                setPadding(0, 10, 0, 10)
                setTextColor(ContextCompat.getColor(activity, if (coThieu) R.color.chu else R.color.chu_phu))
                setCompoundDrawablesRelativeWithIntrinsicBounds(
                    if (coThieu) R.drawable.ic_close else R.drawable.ic_check, 0, 0, 0
                )
                compoundDrawableTintList = ContextCompat.getColorStateList(
                    activity, if (coThieu) R.color.loi else R.color.chinh
                )
            }
            b.dsMuc.addView(dong)
        }

        b.btnBoSung.setOnClickListener {
            bang.setOnDismissListener(null)
            bang.dismiss()
            activity.startActivity(
                Intent(activity, HoSoActivity::class.java).putExtra(HoSoActivity.EXTRA_TU_CONG_CHAN, true)
            )
        }
        b.btnDeSau.setOnClickListener { bang.dismiss() }
        bang.setOnDismissListener { boQua() }
        bang.show()
    }

    private fun hoiDangNhap(activity: AppCompatActivity) {
        AlertDialog.Builder(activity)
            .setTitle("Cần đăng nhập")
            .setMessage("Bạn cần đăng nhập để đăng tin, đặt mua và nhắn tin.")
            .setPositiveButton("Đăng nhập") { _, _ ->
                activity.startActivity(
                    Intent(activity, DangNhapActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                )
            }
            .setNegativeButton("Để sau", null)
            .show()
    }
}

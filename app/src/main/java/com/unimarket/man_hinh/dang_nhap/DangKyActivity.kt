package com.unimarket.man_hinh.dang_nhap

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.unimarket.tien_ich.CanhLe
import com.unimarket.databinding.ActivityDangKyBinding
import com.unimarket.du_lieu.repository.NguoiDungRepository
import com.unimarket.man_hinh.tai_khoan.HoSoActivity
import com.unimarket.man_hinh.trang_chu.ManHinhChinhActivity
import kotlinx.coroutines.launch

/**
 * Màn Đăng ký. Tạo tài khoản xong sẽ đăng nhập luôn và mở màn Hồ sơ để bổ sung thông tin
 * (người dùng có thể bấm "Để sau").
 */
class DangKyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDangKyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDangKyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        CanhLe.chuaVienHeThong(binding.root)
        binding.thanhTieuDe.setNavigationOnClickListener { finish() }

        val repo = NguoiDungRepository(this)
        binding.btnDangKy.setOnClickListener {
            binding.tvLoi.visibility = View.GONE
            binding.btnDangKy.isEnabled = false
            lifecycleScope.launch {
                try {
                    repo.dangKy(
                        binding.nhapHoTen.text.toString(),
                        binding.nhapEmail.text.toString(),
                        binding.nhapMatKhau.text.toString(),
                        binding.nhapNhapLai.text.toString()
                    )
                    // Mở màn chính, đặt màn Hồ sơ lên trên để người dùng điền tiếp
                    val manChinh = Intent(this@DangKyActivity, ManHinhChinhActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    val hoSo = Intent(this@DangKyActivity, HoSoActivity::class.java)
                        .putExtra(HoSoActivity.EXTRA_LAN_DAU, true)
                    startActivities(arrayOf(manChinh, hoSo))
                } catch (e: IllegalStateException) {
                    binding.tvLoi.text = e.message
                    binding.tvLoi.visibility = View.VISIBLE
                    binding.btnDangKy.isEnabled = true
                }
            }
        }
    }
}

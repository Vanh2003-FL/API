package com.unimarket.man_hinh.dang_nhap

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import com.unimarket.tien_ich.CanhLe
import com.unimarket.databinding.ActivityDangNhapBinding
import com.unimarket.du_lieu.database.AppDatabase
import com.unimarket.du_lieu.dulieu_mau.DuLieuMau
import com.unimarket.du_lieu.repository.NguoiDungRepository
import com.unimarket.man_hinh.trang_chu.ManHinhChinhActivity
import com.unimarket.tien_ich.PhienDangNhap
import kotlinx.coroutines.launch

/**
 * Màn Đăng nhập – màn mở đầu tiên của app.
 * - Nếu đã đăng nhập từ trước thì vào thẳng màn chính.
 * - Lần chạy đầu tiên sẽ nạp dữ liệu mẫu vào database.
 */
class DangNhapActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDangNhapBinding
    private lateinit var repo: NguoiDungRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDangNhapBinding.inflate(layoutInflater)
        setContentView(binding.root)
        CanhLe.chuaVienHeThong(binding.root)
        repo = NguoiDungRepository(this)

        // Khoá nút cho tới khi nạp xong dữ liệu mẫu (chỉ mất thời gian ở lần chạy đầu
        // hoặc sau khi database được tạo lại do đổi cấu trúc bảng)
        datTrangThaiNut(false)
        lifecycleScope.launch {
            DuLieuMau.napNeuTrong(AppDatabase.layDb(this@DangNhapActivity))
            // Đã đăng nhập từ trước và tài khoản vẫn còn → vào thẳng màn chính
            if (PhienDangNhap.daDangNhap(this@DangNhapActivity)) {
                if (repo.layNguoiDangNhap() != null) {
                    moManHinhChinh()
                    return@launch
                }
                PhienDangNhap.dangXuat(this@DangNhapActivity) // tài khoản không còn trong DB
            }
            datTrangThaiNut(true)
        }

        binding.btnDangNhap.setOnClickListener {
            dangNhap(binding.nhapEmail.text.toString(), binding.nhapMatKhau.text.toString())
        }
        binding.btnDangKy.setOnClickListener {
            startActivity(Intent(this, DangKyActivity::class.java))
        }
        binding.btnXemKhach.setOnClickListener { moManHinhChinh() }

        // Tạo các nút "đăng nhập nhanh" từ danh sách tài khoản mẫu
        for (tk in DuLieuMau.DS_DANG_NHAP_NHANH) {
            val chip = Chip(this).apply {
                text = tk.nhan
                setOnClickListener {
                    binding.nhapEmail.setText(tk.email)
                    binding.nhapMatKhau.setText(DuLieuMau.MAT_KHAU_MAU)
                    dangNhap(tk.email, DuLieuMau.MAT_KHAU_MAU)
                }
            }
            binding.nhomDangNhapNhanh.addView(chip)
        }
    }

    private fun dangNhap(email: String, matKhau: String) {
        binding.oEmail.error = null
        binding.oMatKhau.error = null
        if (email.isBlank()) {
            binding.oEmail.error = "Nhập email"
            return
        }
        if (matKhau.isBlank()) {
            binding.oMatKhau.error = "Nhập mật khẩu"
            return
        }
        datTrangThaiNut(false)
        lifecycleScope.launch {
            try {
                val nd = repo.dangNhap(email, matKhau)
                Toast.makeText(this@DangNhapActivity, "Xin chào ${nd.hoTen}", Toast.LENGTH_SHORT).show()
                moManHinhChinh()
            } catch (e: IllegalStateException) {
                binding.oMatKhau.error = e.message
                datTrangThaiNut(true)
            }
        }
    }

    private fun datTrangThaiNut(batDuoc: Boolean) {
        binding.btnDangNhap.isEnabled = batDuoc
        binding.btnDangKy.isEnabled = batDuoc
        binding.btnXemKhach.isEnabled = batDuoc
        for (i in 0 until binding.nhomDangNhapNhanh.childCount) {
            binding.nhomDangNhapNhanh.getChildAt(i).isEnabled = batDuoc
        }
    }

    private fun moManHinhChinh() {
        startActivity(Intent(this, ManHinhChinhActivity::class.java))
        finish()
    }
}

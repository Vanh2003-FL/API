package com.unimarket.man_hinh.tai_khoan

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.unimarket.R
import com.unimarket.databinding.ActivityDaLuuBinding
import com.unimarket.du_lieu.repository.NguoiDungRepository
import com.unimarket.du_lieu.repository.TinDangRepository
import com.unimarket.man_hinh.chi_tiet_san_pham.ChiTietSanPhamActivity
import com.unimarket.man_hinh.chi_tiet_san_pham.LuuTin
import com.unimarket.man_hinh.trang_chu.TinDangAdapter
import com.unimarket.tien_ich.CanhLe
import kotlinx.coroutines.launch

/** Màn Đã lưu: các tin người dùng bấm trái tim (đọc từ bảng yeu_thich nối với tin_dang). */
class DaLuuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDaLuuBinding
    private lateinit var adapter: TinDangAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDaLuuBinding.inflate(layoutInflater)
        setContentView(binding.root)
        CanhLe.chuaVienHeThong(binding.root)
        binding.thanhTieuDe.setNavigationOnClickListener { finish() }

        // Bỏ lưu ngay tại đây thì tin biến khỏi danh sách
        adapter = TinDangAdapter(
            khiBamTim = { tin -> LuuTin.doi(this, tin.id) { taiDuLieu() } },
            khiBam = { ChiTietSanPhamActivity.mo(this, it.id) }
        )
        binding.dsTin.layoutManager = GridLayoutManager(this, 2)
        binding.dsTin.adapter = adapter

        val trong = binding.trong
        trong.iconTrong.setImageResource(R.drawable.ic_heart)
        trong.tvTieuDeTrong.text = "Chưa lưu tin nào"
        trong.tvPhuTrong.text = "Bấm biểu tượng trái tim trên tin để lưu lại so sánh."
        trong.btnTrong1.visibility = View.VISIBLE
        trong.btnTrong1.text = "Khám phá tin mới"
        trong.btnTrong1.setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        taiDuLieu()
    }

    private fun taiDuLieu() {
        lifecycleScope.launch {
            val nd = NguoiDungRepository(this@DaLuuActivity).layNguoiDangNhap()
            if (nd == null) {
                finish()
                return@launch
            }
            val ds = TinDangRepository(this@DaLuuActivity).layTinDaLuu(nd.id)
            binding.thanhTieuDe.title = "Đã lưu (${ds.size})"
            adapter.capNhat(ds, ds.map { it.id }.toSet())
            binding.trong.root.visibility = if (ds.isEmpty()) View.VISIBLE else View.GONE
        }
    }
}

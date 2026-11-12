package com.unimarket.man_hinh.thong_bao

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.unimarket.R
import com.unimarket.databinding.ActivityThongBaoBinding
import com.unimarket.du_lieu.repository.NguoiDungRepository
import com.unimarket.du_lieu.repository.ThongBaoRepository
import com.unimarket.tien_ich.CanhLe
import com.unimarket.tien_ich.ThongBaoHeThong
import kotlinx.coroutines.launch

/**
 * Trung tâm thông báo (SCR-20, UC-37): mới nhất ở trên, mục chưa đọc có chấm xanh.
 * Bấm một thông báo → đánh dấu đã đọc và mở nội dung liên quan. Nút "Đánh dấu đã đọc" cho tất cả.
 */
class ThongBaoActivity : AppCompatActivity(), ThongBaoHeThong.CoThongBaoMoi {

    companion object {
        private const val EXTRA_MO_ID = "mo_thong_bao_id"

        /** Intent dùng khi bấm thông báo hệ thống: mở trung tâm rồi mở luôn thông báo đó. */
        fun intentMo(context: Context, thongBaoId: Long): Intent =
            Intent(context, ThongBaoActivity::class.java).putExtra(EXTRA_MO_ID, thongBaoId)
    }

    private lateinit var binding: ActivityThongBaoBinding
    private lateinit var repo: ThongBaoRepository
    private val adapter = ThongBaoAdapter { MoThongBao.mo(this, it) }
    private var toiId = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityThongBaoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        CanhLe.chuaVienHeThong(binding.root)
        repo = ThongBaoRepository(this)

        binding.thanhTieuDe.setNavigationOnClickListener { finish() }
        binding.thanhTieuDe.menu.add("Đánh dấu đã đọc").apply {
            setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            setOnMenuItemClickListener {
                lifecycleScope.launch {
                    repo.danhDauTatCaDaDoc(toiId)
                    taiDuLieu()
                }
                true
            }
        }
        binding.dsThongBao.layoutManager = LinearLayoutManager(this)
        binding.dsThongBao.adapter = adapter

        // Mở từ thông báo hệ thống → đi thẳng tới nội dung
        val moId = intent.getLongExtra(EXTRA_MO_ID, -1)
        if (savedInstanceState == null && moId > 0) {
            lifecycleScope.launch { repo.layTheoId(moId)?.let { MoThongBao.mo(this@ThongBaoActivity, it) } }
        }
    }

    override fun onResume() {
        super.onResume()
        taiDuLieu()
    }

    override fun khiCoThongBaoMoi() = taiDuLieu()

    private fun taiDuLieu() {
        lifecycleScope.launch {
            val nd = NguoiDungRepository(this@ThongBaoActivity).layNguoiDangNhap()
            if (nd == null) {
                finish()
                return@launch
            }
            toiId = nd.id
            val ds = repo.layCuaNguoiDung(nd.id)
            adapter.capNhat(ds)
            binding.trong.root.visibility = if (ds.isEmpty()) View.VISIBLE else View.GONE
            binding.trong.iconTrong.setImageResource(R.drawable.ic_bell)
            binding.trong.tvTieuDeTrong.text = "Chưa có thông báo"
            binding.trong.tvPhuTrong.text = "Khi có người muốn mua, chốt hẹn hoặc sắp tới giờ hẹn, thông báo sẽ hiện ở đây."
            binding.trong.btnTrong1.visibility = View.GONE
            binding.trong.btnTrong2.visibility = View.GONE
        }
    }
}

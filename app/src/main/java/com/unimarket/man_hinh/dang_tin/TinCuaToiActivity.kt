package com.unimarket.man_hinh.dang_tin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.google.android.material.radiobutton.MaterialRadioButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.unimarket.R
import com.unimarket.databinding.ActivityTinCuaToiBinding
import com.unimarket.databinding.BottomSheetDaBanBinding
import com.unimarket.databinding.BottomSheetDayTinBinding
import com.unimarket.databinding.ChipLocBinding
import com.unimarket.du_lieu.repository.DangTinRepository
import com.unimarket.du_lieu.repository.NguoiDungRepository
import com.unimarket.man_hinh.chi_tiet_san_pham.ChiTietSanPhamActivity
import com.unimarket.man_hinh.tai_khoan.BoSungHoSo
import com.unimarket.mo_hinh.TinDang
import com.unimarket.tien_ich.CanhLe
import com.unimarket.tien_ich.DinhDang
import com.unimarket.tien_ich.HangSo
import kotlinx.coroutines.launch

/**
 * Màn Tin của tôi: 5 tab Đang bán / Đang giao dịch / Đã bán / Hết hạn / Bị ẩn.
 * Thao tác: sửa, đánh dấu đã bán (nhập giá chốt), đẩy tin (thanh toán mô phỏng), gia hạn, xoá.
 */
class TinCuaToiActivity : AppCompatActivity(), TinCuaToiAdapter.ThaoTac {

    companion object {
        private const val EXTRA_TAB = "tab"

        fun mo(context: Context, tab: String = HangSo.TIN_DANG_BAN) {
            context.startActivity(Intent(context, TinCuaToiActivity::class.java).putExtra(EXTRA_TAB, tab))
        }

        private val DS_TAB = listOf(
            HangSo.TIN_DANG_BAN to "Đang bán",
            HangSo.TIN_DANG_GIAO_DICH to "Đang giao dịch",
            HangSo.TIN_DA_BAN to "Đã bán",
            HangSo.TIN_HET_HAN to "Hết hạn",
            HangSo.TIN_BI_AN to "Bị ẩn"
        )
    }

    private lateinit var binding: ActivityTinCuaToiBinding
    private lateinit var repo: DangTinRepository
    private val adapter = TinCuaToiAdapter(this)
    private var tabDangChon = HangSo.TIN_DANG_BAN
    private var tatCa: List<TinDang> = emptyList()
    private var luotLuu: Map<Long, Int> = emptyMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTinCuaToiBinding.inflate(layoutInflater)
        setContentView(binding.root)
        CanhLe.chuaVienHeThong(binding.root)
        repo = DangTinRepository(this)
        tabDangChon = intent.getStringExtra(EXTRA_TAB) ?: HangSo.TIN_DANG_BAN

        binding.thanhTieuDe.setNavigationOnClickListener { finish() }
        binding.dsTin.layoutManager = LinearLayoutManager(this)
        binding.dsTin.adapter = adapter

        for ((ma, ten) in DS_TAB) {
            val chip = ChipLocBinding.inflate(layoutInflater, binding.nhomTab, false).root
            chip.id = View.generateViewId()
            chip.tag = ma
            chip.text = ten
            binding.nhomTab.addView(chip)
            if (ma == tabDangChon) binding.nhomTab.check(chip.id)
        }
        binding.nhomTab.setOnCheckedStateChangeListener { g, ids ->
            tabDangChon = ids.firstOrNull()?.let { g.findViewById<Chip>(it).tag as String } ?: return@setOnCheckedStateChangeListener
            veDanhSach()
        }
    }

    override fun onResume() {
        super.onResume()
        taiDuLieu()
    }

    private fun taiDuLieu() {
        lifecycleScope.launch {
            val nd = NguoiDungRepository(this@TinCuaToiActivity).layNguoiDangNhap()
            if (nd == null) {
                finish()
                return@launch
            }
            tatCa = repo.layTinCuaToi(nd.id)
            luotLuu = repo.demLuotLuu(tatCa)
            veDanhSach()
        }
    }

    private fun veDanhSach() {
        // Cập nhật số đếm trên từng tab
        for (i in 0 until binding.nhomTab.childCount) {
            val chip = binding.nhomTab.getChildAt(i) as Chip
            val ten = DS_TAB.first { it.first == chip.tag }.second
            chip.text = "$ten (${tatCa.count { it.trangThai == chip.tag }})"
        }
        val ds = tatCa.filter { it.trangThai == tabDangChon }
        adapter.capNhat(ds, luotLuu)

        val trong = binding.trong
        trong.root.visibility = if (ds.isEmpty()) View.VISIBLE else View.GONE
        val laTabAn = tabDangChon == HangSo.TIN_BI_AN
        trong.iconTrong.setImageResource(if (laTabAn) R.drawable.ic_shield else R.drawable.ic_list)
        trong.tvTieuDeTrong.text = if (laTabAn) "Không có tin nào bị ẩn" else "Chưa có tin ở mục này"
        trong.tvPhuTrong.text = if (laTabAn) "Tin bị báo cáo nhiều lần sẽ tạm ẩn và hiện ở đây." else ""
        trong.btnTrong1.visibility = if (tabDangChon == HangSo.TIN_DANG_BAN) View.VISIBLE else View.GONE
        trong.btnTrong1.text = "Đăng tin"
        trong.btnTrong1.setOnClickListener { BoSungHoSo.kiemTra(this) { DangTinActivity.moMoi(this) } }
    }

    // ===================== THAO TÁC TRÊN MỘT TIN =====================
    override fun mo(tin: TinDang) = ChiTietSanPhamActivity.mo(this, tin.id)

    override fun sua(tin: TinDang) = DangTinActivity.moSua(this, tin.id)

    override fun daBan(tin: TinDang) {
        val bang = BottomSheetDialog(this)
        val b = BottomSheetDaBanBinding.inflate(layoutInflater)
        bang.setContentView(b.root)
        b.tvTenTin.text = "${tin.tieuDe} · giá rao ${DinhDang.gia(tin.gia)}"
        b.nhapGiaBan.setText(tin.gia.toString())
        b.btnHuy.setOnClickListener { bang.dismiss() }
        b.btnXacNhan.setOnClickListener {
            val gia = b.nhapGiaBan.text.toString().toLongOrNull()
            if (gia == null || gia < 0) {
                b.oGiaBan.error = "Nhập giá đã bán (0 nếu tặng)"
                return@setOnClickListener
            }
            lifecycleScope.launch {
                repo.danhDauDaBan(tin.id, gia)
                bang.dismiss()
                thongBao("Đã chuyển sang Đã bán với giá ${DinhDang.gia(gia)}")
                taiDuLieu()
            }
        }
        bang.show()
    }

    /** Đẩy tin: cần hồ sơ đầy đủ; chọn gói rồi "thanh toán" (mô phỏng). */
    override fun dayTin(tin: TinDang) {
        BoSungHoSo.kiemTra(this) {
            val bang = BottomSheetDialog(this)
            val b = BottomSheetDayTinBinding.inflate(layoutInflater)
            bang.setContentView(b.root)
            var goiChon = HangSo.DS_GOI_DAY_TIN.first()
            HangSo.DS_GOI_DAY_TIN.forEachIndexed { i, goi ->
                val nut = MaterialRadioButton(this).apply {
                    id = View.generateViewId()
                    text = "${goi.ten} — ${DinhDang.gia(goi.phi)}"
                    textSize = 15f
                    setPadding(8, 16, 8, 16)
                    setOnClickListener {
                        goiChon = goi
                        b.btnThanhToan.text = "Thanh toán ${DinhDang.gia(goi.phi)}"
                    }
                }
                b.nhomGoi.addView(nut)
                if (i == 0) b.nhomGoi.check(nut.id)
            }
            b.btnThanhToan.text = "Thanh toán ${DinhDang.gia(goiChon.phi)}"
            b.btnHuy.setOnClickListener { bang.dismiss() }
            b.btnThanhToan.setOnClickListener {
                lifecycleScope.launch {
                    repo.dayTin(tin.id, goiChon.soNgay, goiChon.phi)
                    bang.dismiss()
                    thongBao("Đã đẩy tin ${goiChon.soNgay} ngày lên mục Nổi bật")
                    taiDuLieu()
                }
            }
            bang.show()
        }
    }

    override fun giaHan(tin: TinDang) {
        lifecycleScope.launch {
            try {
                repo.giaHan(tin.id)
                thongBao("Đã gia hạn thêm ${HangSo.SO_NGAY_HIEN_TIN} ngày")
                binding.nhomTab.findViewWithTag<Chip>(HangSo.TIN_DANG_BAN)?.let { binding.nhomTab.check(it.id) }
                taiDuLieu()
            } catch (e: IllegalStateException) {
                thongBao(e.message ?: "Không gia hạn được")
            }
        }
    }

    override fun xoa(tin: TinDang) {
        AlertDialog.Builder(this)
            .setTitle("Xoá tin?")
            .setMessage("“${tin.tieuDe}” sẽ không còn hiển thị với người mua.")
            .setPositiveButton("Xoá") { _, _ ->
                lifecycleScope.launch {
                    repo.xoaTin(tin.id)
                    thongBao("Đã xoá tin")
                    taiDuLieu()
                }
            }
            .setNegativeButton("Huỷ", null)
            .show()
    }

    private fun thongBao(chu: String) = Toast.makeText(this, chu, Toast.LENGTH_SHORT).show()
}

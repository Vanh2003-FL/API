package com.unimarket.man_hinh.quan_tri

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import com.unimarket.R
import com.unimarket.databinding.ActivityQuanTriBinding
import com.unimarket.databinding.ChipLocBinding
import com.unimarket.databinding.HangMenuBinding
import com.unimarket.databinding.ItemKpiBinding
import com.unimarket.du_lieu.repository.QuanTriRepository
import com.unimarket.tien_ich.CanhLe
import com.unimarket.tien_ich.DinhDang
import com.unimarket.tien_ich.HangSo
import com.unimarket.xu_ly_nghiep_vu.ThongKe
import kotlinx.coroutines.launch

/**
 * Màn Quản trị (SCR-24, UC-54): thẻ KPI theo kỳ 7 ngày / 30 ngày / tất cả, biểu đồ tin đăng–tin bán theo ngày,
 * phân bổ theo danh mục; lối vào kiểm duyệt, người dùng, danh mục, xem dữ liệu. Có nút xuất bảng KPI ra CSV.
 */
class QuanTriActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuanTriBinding
    private lateinit var repo: QuanTriRepository
    private var khoang = ThongKe.Khoang.BAY_NGAY
    private var csvGanNhat = ""

    /** Hộp chọn nơi lưu file của Android (không cần quyền bộ nhớ). */
    private val luuCsv = registerForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        uri ?: return@registerForActivityResult
        lifecycleScope.launch {
            contentResolver.openOutputStream(uri)?.let { repo.ghiChuoi(csvGanNhat, it) }
            Toast.makeText(this@QuanTriActivity, "Đã xuất bảng KPI", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuanTriBinding.inflate(layoutInflater)
        setContentView(binding.root)
        CanhLe.chuaVienHeThong(binding.root)
        repo = QuanTriRepository(this)

        binding.thanhTieuDe.setNavigationOnClickListener { finish() }
        binding.thanhTieuDe.menu.add("Xuất CSV").apply {
            setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            setOnMenuItemClickListener {
                luuCsv.launch("kpi_unimarket_${DinhDang.boDau(khoang.ten).replace(" ", "_")}.csv")
                true
            }
        }

        for (k in ThongKe.Khoang.entries) {
            val chip = ChipLocBinding.inflate(layoutInflater, binding.nhomKy, false).root
            chip.id = View.generateViewId()
            chip.text = k.ten
            chip.tag = k
            binding.nhomKy.addView(chip)
            if (k == khoang) binding.nhomKy.check(chip.id)
        }
        binding.nhomKy.setOnCheckedStateChangeListener { g, ids ->
            khoang = ids.firstOrNull()?.let { g.findViewById<Chip>(it).tag as ThongKe.Khoang } ?: return@setOnCheckedStateChangeListener
            taiDuLieu()
        }

        datHang(binding.hangNguoiDung, R.drawable.ic_user, "Người dùng", "Xem, khoá / mở khoá tài khoản") {
            startActivity(Intent(this, QuanLyNguoiDungActivity::class.java))
        }
        datHang(binding.hangDanhMuc, R.drawable.ic_grid, "Danh mục", "Thêm, sửa, ẩn danh mục con · tỷ lệ khấu hao") {
            startActivity(Intent(this, QuanLyDanhMucActivity::class.java))
        }
        datHang(binding.hangDuLieu, R.drawable.ic_list, "Xem dữ liệu & xuất file", "Xem 9 bảng SQLite, xuất CSV / file .db") {
            startActivity(Intent(this, XemDuLieuActivity::class.java))
        }

        QuyenQuanTri.kiemTra(this) { taiDuLieu() }
    }

    override fun onRestart() {
        super.onRestart()
        taiDuLieu() // quay lại sau khi kiểm duyệt / sửa danh mục → số liệu mới
    }

    private fun taiDuLieu() {
        lifecycleScope.launch {
            val dl = repo.layDuLieuThongKe()
            val bayGio = System.currentTimeMillis()
            val tu = ThongKe.tuNgay(khoang, bayGio)
            val kq = ThongKe.tinh(dl.dsTin, dl.dsNguoiDung, dl.dsLich, dl.dsBaoCao, dl.dsHoatDong, tu)
            val soNgayBieuDo = khoang.soNgay ?: 30
            val dsNgay = ThongKe.theoNgay(dl.dsTin, soNgayBieuDo, bayGio)
            csvGanNhat = ThongKe.taoCsv(khoang, kq, dsNgay)

            binding.tvGiaoDich.text = kq.giaoDichThanhCong.toString()
            binding.tvGiaoDichPhu.text = "giao dịch thành công (${khoang.ten.lowercase()})\n" +
                "Sinh viên tiết kiệm ${DinhDang.gia(kq.tienTietKiem).replace("Tặng miễn phí", "0đ")} so với mua mới (KPI-09)"
            veKpi(kq)

            // Biểu đồ cột: xanh = tin đăng, cam = tin bán
            val xanh = ContextCompat.getColor(this@QuanTriActivity, R.color.chinh)
            val cam = ContextCompat.getColor(this@QuanTriActivity, R.color.gia)
            binding.tvChuThich.text = "■ Tin đăng   ■ Tin bán · $soNgayBieuDo ngày gần nhất"
            binding.bieuDoNgay.datDuLieu(
                dsNgay.map { BieuDoCotView.Cot(it.nhan, listOf(it.dang, it.ban)) }, listOf(xanh, cam),
                "Biểu đồ: ${dsNgay.sumOf { it.dang }} tin đăng, ${dsNgay.sumOf { it.ban }} tin bán trong $soNgayBieuDo ngày"
            )
            veChuThichMau(xanh, cam)

            // Biểu đồ thanh: màu theo danh mục cha
            val phanBo = ThongKe.theoDanhMuc(dl.dsTin, tu)
            binding.tvKhongCoDanhMuc.visibility = if (phanBo.isEmpty()) View.VISIBLE else View.GONE
            binding.bieuDoDanhMuc.datDuLieu(
                phanBo.map { BieuDoNgangView.Thanh(it.ten, it.so, ContextCompat.getColor(this@QuanTriActivity, HangSo.timDanhMuc(it.maCha)!!.mauIcon)) },
                phanBo.joinToString(", ") { "${it.ten} ${it.so} tin" }
            )

            val choXuLy = repo.demTinChoXuLy()
            datHang(binding.hangKiemDuyet, R.drawable.ic_flag, "Kiểm duyệt tin bị báo cáo",
                if (choXuLy == 0) "Không có tin chờ xử lý" else "$choXuLy tin đang chờ xử lý") {
                startActivity(Intent(this@QuanTriActivity, KiemDuyetActivity::class.java))
            }
            binding.hangKiemDuyet.tvPhu.setTextColor(
                ContextCompat.getColor(this@QuanTriActivity, if (choXuLy > 0) R.color.loi else R.color.chu_phu)
            )
        }
    }

    /** Lưới thẻ KPI, 2 thẻ mỗi hàng. Mỗi thẻ ghi mã KPI để đối chiếu với tài liệu phân tích kinh tế. */
    private fun veKpi(kq: ThongKe.KetQua) {
        val the = listOf(
            Triple(kq.nguoiDungMoi.toString(), "Người dùng mới", "KPI-02"),
            Triple(kq.nguoiDungHoatDong.toString(), "Người dùng hoạt động", "KPI-04 · đăng tin, nhắn tin, đặt mua"),
            Triple(kq.soTinDang.toString(), "Tin đăng mới", "trong kỳ"),
            Triple(QuyenQuanTri.phanTram(kq.tyLeBanDuoc), "Tỷ lệ bán được", "KPI-05 · tin đăng trong kỳ"),
            Triple(QuyenQuanTri.soLe(kq.thoiGianBanTB) + if (kq.thoiGianBanTB != null) " ngày" else "", "Thời gian bán TB", "KPI-06"),
            Triple(QuyenQuanTri.phanTram(kq.tyLeDungGiaGoiY), "Dùng giá gợi ý", "KPI-07 · lệch ≤ 10%"),
            Triple(QuyenQuanTri.phanTram(kq.mape), "Sai số gợi ý (MAPE)", "KPI-08 · ${kq.soMauMape} mẫu · mục tiêu < 25%"),
            Triple(DinhDang.gia(kq.doanhThuMoPhong).replace("Tặng miễn phí", "0đ"), "Doanh thu mô phỏng", "KPI-12 · đẩy tin"),
            Triple(QuyenQuanTri.phanTram(kq.tyLeHenDuocXacNhan), "Yêu cầu mua được chốt", "KPI-13 · ${kq.soYeuCauMua} yêu cầu"),
            Triple(QuyenQuanTri.phanTram(kq.tyLeHenThanhGiaoDich), "Hẹn thành giao dịch", "KPI-14"),
            Triple(QuyenQuanTri.phanTram(kq.tyLeTinBiBaoCao), "Tin bị báo cáo", "KPI-11"),
            Triple(DinhDang.gia(kq.tienTietKiem).replace("Tặng miễn phí", "0đ"), "SV tiết kiệm", "KPI-09 · so với giá mua mới")
        )
        binding.luoiKpi.removeAllViews()
        val dp = resources.displayMetrics.density
        the.chunked(2).forEach { cap ->
            val hang = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    .apply { topMargin = (10 * dp).toInt() }
            }
            cap.forEachIndexed { i, (giaTri, ten, ghiChu) ->
                val b = ItemKpiBinding.inflate(LayoutInflater.from(this), hang, false)
                b.tvGiaTri.text = giaTri
                b.tvTen.text = ten
                b.tvGhiChu.text = ghiChu
                if (i == 1) (b.root.layoutParams as LinearLayout.LayoutParams).marginStart = (10 * dp).toInt()
                hang.addView(b.root)
            }
            binding.luoiKpi.addView(hang)
        }
    }

    private fun veChuThichMau(xanh: Int, cam: Int) {
        val sb = android.text.SpannableStringBuilder(binding.tvChuThich.text)
        sb.setSpan(android.text.style.ForegroundColorSpan(xanh), 0, 1, 0)
        val viTri = sb.indexOf("■", 1)
        if (viTri > 0) sb.setSpan(android.text.style.ForegroundColorSpan(cam), viTri, viTri + 1, 0)
        binding.tvChuThich.text = sb
    }

    private fun datHang(hang: HangMenuBinding, icon: Int, tieuDe: String, phu: String, khiBam: () -> Unit) {
        hang.iconMenu.setImageResource(icon)
        hang.tvTieuDe.text = tieuDe
        hang.tvPhu.text = phu
        hang.root.setOnClickListener { khiBam() }
    }
}

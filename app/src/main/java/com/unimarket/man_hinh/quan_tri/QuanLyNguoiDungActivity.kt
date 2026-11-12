package com.unimarket.man_hinh.quan_tri

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.unimarket.R
import com.unimarket.databinding.ActivityDanhSachQuanTriBinding
import com.unimarket.databinding.ItemNguoiDungQuanTriBinding
import com.unimarket.du_lieu.repository.QuanTriRepository
import com.unimarket.du_lieu.repository.QuanTriRepository.DongNguoiDung
import com.unimarket.tien_ich.CanhLe
import com.unimarket.tien_ich.DinhDang
import com.unimarket.tien_ich.HangSo
import kotlinx.coroutines.launch

/** Danh sách người dùng (UC-52): tìm kiếm, xem số liệu, khoá / mở khoá tài khoản. */
class QuanLyNguoiDungActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDanhSachQuanTriBinding
    private lateinit var repo: QuanTriRepository
    private var tatCa: List<DongNguoiDung> = emptyList()
    private var ds: List<DongNguoiDung> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDanhSachQuanTriBinding.inflate(layoutInflater)
        setContentView(binding.root)
        CanhLe.chuaVienHeThong(binding.root)
        repo = QuanTriRepository(this)
        binding.thanhTieuDe.title = "Người dùng"
        binding.thanhTieuDe.setNavigationOnClickListener { finish() }
        binding.oTim.visibility = View.VISIBLE
        binding.nhapTim.doAfterTextChanged { loc() }
        binding.ds.layoutManager = LinearLayoutManager(this)
        binding.ds.adapter = adapter
        QuyenQuanTri.kiemTra(this) { taiDuLieu() }
    }

    private fun taiDuLieu() {
        lifecycleScope.launch {
            tatCa = repo.layDsNguoiDung()
            binding.thanhTieuDe.subtitle = "${tatCa.size} tài khoản · ${tatCa.count { it.nguoiDung.biKhoa }} bị khoá"
            loc()
        }
    }

    /** Lọc theo ô tìm (không dấu, không phân biệt hoa thường). */
    private fun loc() {
        val tu = DinhDang.boDau(binding.nhapTim.text.toString().trim())
        ds = if (tu.isEmpty()) tatCa else tatCa.filter {
            DinhDang.boDau("${it.nguoiDung.hoTen} ${it.nguoiDung.email} ${it.nguoiDung.maTruong}").contains(tu)
        }
        adapter.notifyDataSetChanged()
        binding.trong.root.visibility = if (ds.isEmpty()) View.VISIBLE else View.GONE
        binding.trong.iconTrong.setImageResource(R.drawable.ic_user)
        binding.trong.tvTieuDeTrong.text = "Không tìm thấy người dùng"
        binding.trong.tvPhuTrong.text = ""
        binding.trong.btnTrong1.visibility = View.GONE
        binding.trong.btnTrong2.visibility = View.GONE
    }

    private fun doiKhoa(d: DongNguoiDung) {
        val khoa = !d.nguoiDung.biKhoa
        AlertDialog.Builder(this)
            .setTitle(if (khoa) "Khoá ${d.nguoiDung.hoTen}?" else "Mở khoá ${d.nguoiDung.hoTen}?")
            .setMessage(
                if (khoa) "Người này sẽ bị đăng xuất, không đăng nhập lại được, và mọi tin đang hiện bị gỡ."
                else "Người này đăng nhập lại được. Các tin đã bị gỡ không tự hiện lại."
            )
            .setPositiveButton(if (khoa) "Khoá" else "Mở khoá") { _, _ ->
                lifecycleScope.launch {
                    try {
                        repo.doiKhoa(d.nguoiDung.id, khoa)
                        Toast.makeText(this@QuanLyNguoiDungActivity, if (khoa) "Đã khoá tài khoản" else "Đã mở khoá", Toast.LENGTH_SHORT).show()
                    } catch (e: IllegalStateException) {
                        Toast.makeText(this@QuanLyNguoiDungActivity, e.message, Toast.LENGTH_LONG).show()
                    }
                    taiDuLieu()
                }
            }
            .setNegativeButton("Huỷ", null)
            .show()
    }

    private val adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun getItemCount() = ds.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            object : RecyclerView.ViewHolder(ItemNguoiDungQuanTriBinding.inflate(LayoutInflater.from(parent.context), parent, false).root) {}

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val b = ItemNguoiDungQuanTriBinding.bind(holder.itemView)
            val d = ds[position]
            val nd = d.nguoiDung
            val ctx = this@QuanLyNguoiDungActivity
            b.tvVietTat.text = DinhDang.vietTat(nd.hoTen)
            b.tvTen.text = nd.hoTen
            b.tvPhu.text = nd.email + (if (nd.maTruong.isNotBlank()) " · ${nd.maTruong}" else "")
            b.tvSoLieu.text = "${d.soTin} tin · ${d.soDaBan} đã bán" + if (d.soBiBaoCao > 0) " · ${d.soBiBaoCao} tin bị báo cáo" else ""
            val laQuanTri = nd.vaiTro == HangSo.VAI_TRO_QUAN_TRI
            val (nhan, mauChu, mauNen) = when {
                laQuanTri -> Triple("Quản trị viên", R.color.thong_tin, R.color.nen_thong_tin)
                nd.biKhoa -> Triple("Đã bị khoá", R.color.loi, R.color.nen_loi)
                else -> Triple("Đang hoạt động", R.color.tren_chinh_nhat, R.color.chinh_nhat)
            }
            b.tvNhan.text = nhan
            b.tvNhan.setTextColor(ContextCompat.getColor(ctx, mauChu))
            b.tvNhan.backgroundTintList = ContextCompat.getColorStateList(ctx, mauNen)
            b.btnKhoa.visibility = if (laQuanTri) View.GONE else View.VISIBLE
            b.btnKhoa.text = if (nd.biKhoa) "Mở khoá" else "Khoá"
            b.btnKhoa.setTextColor(ContextCompat.getColor(ctx, if (nd.biKhoa) R.color.chinh else R.color.loi))
            b.btnKhoa.setOnClickListener { doiKhoa(d) }
        }
    }
}

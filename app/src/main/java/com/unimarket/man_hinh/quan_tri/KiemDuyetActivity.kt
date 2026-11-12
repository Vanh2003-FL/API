package com.unimarket.man_hinh.quan_tri

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.unimarket.R
import com.unimarket.databinding.ActivityDanhSachQuanTriBinding
import com.unimarket.databinding.ItemKiemDuyetBinding
import com.unimarket.du_lieu.repository.QuanTriRepository
import com.unimarket.du_lieu.repository.QuanTriRepository.TinBiBaoCao
import com.unimarket.man_hinh.chi_tiet_san_pham.ChiTietSanPhamActivity
import com.unimarket.tien_ich.CanhLe
import com.unimarket.tien_ich.DinhDang
import com.unimarket.tien_ich.HangSo
import kotlinx.coroutines.launch

/**
 * Kiểm duyệt tin bị báo cáo (SCR-26, UC-51): mỗi thẻ là một tin kèm các lý do bị báo cáo.
 * - Giữ tin: bỏ qua báo cáo, tin đang tạm ẩn được hiện lại.
 * - Gỡ tin: ẩn vĩnh viễn, huỷ lịch hẹn đang có, báo người bán.
 * - Gỡ + khoá người bán (UC-52): thêm khoá tài khoản, gỡ mọi tin còn hiện của người đó.
 */
class KiemDuyetActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDanhSachQuanTriBinding
    private lateinit var repo: QuanTriRepository
    private var ds: List<TinBiBaoCao> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDanhSachQuanTriBinding.inflate(layoutInflater)
        setContentView(binding.root)
        CanhLe.chuaVienHeThong(binding.root)
        repo = QuanTriRepository(this)
        binding.thanhTieuDe.title = "Kiểm duyệt"
        binding.thanhTieuDe.setNavigationOnClickListener { finish() }
        binding.ds.layoutManager = LinearLayoutManager(this)
        binding.ds.adapter = adapter
        QuyenQuanTri.kiemTra(this) { taiDuLieu() }
    }

    private fun taiDuLieu() {
        lifecycleScope.launch {
            ds = repo.layTinBiBaoCao()
            adapter.notifyDataSetChanged()
            binding.thanhTieuDe.subtitle = if (ds.isEmpty()) null else "${ds.size} tin chờ xử lý"
            val t = binding.trong
            t.root.visibility = if (ds.isEmpty()) View.VISIBLE else View.GONE
            t.iconTrong.setImageResource(R.drawable.ic_shield)
            t.tvTieuDeTrong.text = "Không còn tin chờ xử lý"
            t.tvPhuTrong.text = "Tin bị báo cáo sẽ hiện ở đây. Đủ ${HangSo.NGUONG_BAO_CAO_TU_AN} báo cáo thì tin tự tạm ẩn."
            t.btnTrong1.visibility = View.GONE
            t.btnTrong2.visibility = View.GONE
        }
    }

    /** Hỏi lại trước khi làm, rồi chạy thao tác và tải lại danh sách. */
    private fun xacNhan(tieuDe: String, noiDung: String, nut: String, xong: String, viec: suspend () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle(tieuDe)
            .setMessage(noiDung)
            .setPositiveButton(nut) { _, _ ->
                lifecycleScope.launch {
                    try {
                        viec()
                        Toast.makeText(this@KiemDuyetActivity, xong, Toast.LENGTH_SHORT).show()
                    } catch (e: IllegalStateException) {
                        Toast.makeText(this@KiemDuyetActivity, e.message, Toast.LENGTH_LONG).show()
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
            object : RecyclerView.ViewHolder(ItemKiemDuyetBinding.inflate(LayoutInflater.from(parent.context), parent, false).root) {}

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val b = ItemKiemDuyetBinding.bind(holder.itemView)
            val muc = ds[position]
            val tin = muc.tin
            val dangAn = tin.trangThai == HangSo.TIN_BI_AN
            b.tvTrangThai.text = if (dangAn) "Đang tạm ẩn · ${muc.dsBaoCao.size} báo cáo" else "Đang hiển thị · ${muc.dsBaoCao.size} báo cáo"
            b.tvTrangThai.setTextColor(ContextCompat.getColor(this@KiemDuyetActivity, if (dangAn) R.color.loi else R.color.canh_bao))
            b.tvTrangThai.backgroundTintList = ContextCompat.getColorStateList(this@KiemDuyetActivity, if (dangAn) R.color.nen_loi else R.color.nen_canh_bao)
            b.tvTieuDe.text = "${tin.tieuDe} · ${DinhDang.gia(tin.gia)}"
            b.tvNguoiBan.text = "Người bán: ${muc.nguoiBan?.hoTen} (${muc.nguoiBan?.email})" +
                if (muc.nguoiBan?.biKhoa == true) " · đã bị khoá" else ""
            // Gom lý do: "Hàng cấm ×2", kèm ghi chú nếu có
            b.tvLyDo.text = muc.dsBaoCao.groupBy { it.lyDo }.entries.joinToString("\n") { (lyDo, dsBc) ->
                "• $lyDo ×${dsBc.size}" + dsBc.mapNotNull { it.ghiChu.ifBlank { null } }.joinToString("") { " – “$it”" }
            }
            b.btnXem.setOnClickListener { ChiTietSanPhamActivity.mo(this@KiemDuyetActivity, tin.id) }
            b.btnGiu.setOnClickListener {
                xacNhan("Giữ tin?", "Bỏ qua ${muc.dsBaoCao.size} báo cáo" + if (dangAn) " và hiển thị lại tin." else ".", "Giữ tin", "Đã giữ tin") {
                    repo.giuTin(tin.id)
                }
            }
            b.btnGo.setOnClickListener {
                xacNhan("Gỡ tin?", "Tin bị ẩn vĩnh viễn, lịch hẹn đang có sẽ bị huỷ và người bán nhận thông báo.", "Gỡ tin", "Đã gỡ tin") {
                    repo.goTin(tin.id, khoaNguoiBan = false)
                }
            }
            b.btnGoKhoa.setOnClickListener {
                xacNhan(
                    "Gỡ tin và khoá người bán?",
                    "${muc.nguoiBan?.hoTen} sẽ không đăng nhập được nữa và mọi tin đang hiện của người này đều bị gỡ.",
                    "Gỡ + khoá", "Đã gỡ tin và khoá tài khoản"
                ) { repo.goTin(tin.id, khoaNguoiBan = true) }
            }
        }
    }
}

package com.unimarket.man_hinh.quan_tri

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.unimarket.R
import com.unimarket.databinding.ActivityQuanLyDanhMucBinding
import com.unimarket.databinding.BottomSheetDanhMucBinding
import com.unimarket.databinding.ChipLocBinding
import com.unimarket.databinding.HangMenuBinding
import com.unimarket.du_lieu.repository.DanhMucRepository
import com.unimarket.mo_hinh.DanhMuc
import com.unimarket.tien_ich.CanhLe
import com.unimarket.tien_ich.HangSo
import kotlinx.coroutines.launch

/**
 * Quản lý danh mục (UC-53, FR-53). 3 danh mục cha cố định; quản trị viên thêm, sửa tên / icon / tỷ lệ khấu hao,
 * ẩn hoặc hiện danh mục con. Không xoá để tin cũ vẫn hiện đúng tên danh mục.
 */
class QuanLyDanhMucActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuanLyDanhMucBinding
    private lateinit var repo: DanhMucRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuanLyDanhMucBinding.inflate(layoutInflater)
        setContentView(binding.root)
        CanhLe.chuaVienHeThong(binding.root)
        repo = DanhMucRepository(this)
        binding.thanhTieuDe.setNavigationOnClickListener { finish() }
        QuyenQuanTri.kiemTra(this) { taiDuLieu() }
    }

    private fun taiDuLieu() {
        lifecycleScope.launch {
            val ds = repo.layTatCa()
            val soTin = repo.demTinTheoCon()
            val dp = resources.displayMetrics.density
            binding.dsNhom.removeAllViews()
            for (cha in HangSo.DANH_MUC_MAC_DINH) {
                val dsCon = ds.filter { it.maCha == cha.ma }
                // Tiêu đề nhóm cha
                binding.dsNhom.addView(TextView(this@QuanLyDanhMucActivity).apply {
                    setTextAppearance(R.style.NhanMuc)
                    text = "${cha.ten} · ${dsCon.count { !it.an }}/${dsCon.size} đang hiện"
                    setPadding(0, (18 * dp).toInt(), 0, (6 * dp).toInt())
                })
                for (con in dsCon) {
                    val h = HangMenuBinding.inflate(layoutInflater, binding.dsNhom, true)
                    h.root.setBackgroundResource(R.drawable.bg_the)
                    (h.root.layoutParams as android.widget.LinearLayout.LayoutParams).bottomMargin = (6 * dp).toInt()
                    h.iconMenu.setImageResource(HangSo.iconTheoTen(con.tenIcon))
                    h.iconMenu.imageTintList = ContextCompat.getColorStateList(this@QuanLyDanhMucActivity, if (con.an) R.color.chu_phu else cha.mauIcon)
                    h.tvTieuDe.text = con.ten
                    h.tvTieuDe.alpha = if (con.an) 0.5f else 1f
                    val khauHao = if (cha.ma == HangSo.SACH) "Ước tính theo giá bìa" else "Khấu hao ${Math.round(con.khauHaoNam * 100)}%/năm"
                    h.tvPhu.text = "$khauHao · ${soTin[con.ma] ?: 0} tin" + if (con.an) " · Đang ẩn" else ""
                    h.root.setOnClickListener { moBang(cha, con) }
                }
                binding.dsNhom.addView(MaterialButton(this@QuanLyDanhMucActivity, null, com.google.android.material.R.attr.materialButtonOutlinedStyle).apply {
                    text = "Thêm danh mục con vào ${cha.ten}"
                    setIconResource(R.drawable.ic_plus)
                    setOnClickListener { moBang(cha, null) }
                })
            }
        }
    }

    /** Bảng thêm (con = null) hoặc sửa danh mục con. */
    private fun moBang(cha: HangSo.DanhMuc, con: DanhMuc?) {
        val bang = BottomSheetDialog(this)
        val b = BottomSheetDanhMucBinding.inflate(layoutInflater)
        bang.setContentView(b.root)
        bang.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        val laSach = cha.ma == HangSo.SACH

        b.tvTieuDe.text = if (con == null) "Thêm danh mục con" else "Sửa danh mục"
        b.tvCha.text = "Thuộc nhóm: ${cha.ten}" + if (con != null) " · mã ${con.ma}" else ""
        b.nhapTen.setText(con?.ten.orEmpty())
        b.oKhauHao.visibility = if (laSach) View.GONE else View.VISIBLE
        b.nhapKhauHao.setText(Math.round((con?.khauHaoNam ?: 0.2) * 100).toString())
        b.swAn.visibility = if (con == null) View.GONE else View.VISIBLE
        b.swAn.isChecked = con?.an == true

        // Chọn icon: mỗi chip là một icon trong bộ cho phép
        var iconChon = con?.tenIcon ?: HangSo.tenIcon(cha.icon)
        for ((ten, icon) in HangSo.DS_ICON_DANH_MUC) {
            val chip = ChipLocBinding.inflate(layoutInflater, b.nhomIcon, false).root
            chip.id = View.generateViewId()
            chip.tag = ten
            chip.text = ""
            chip.setChipIconResource(icon)
            chip.isChipIconVisible = true
            chip.isCheckedIconVisible = false
            chip.contentDescription = ten
            b.nhomIcon.addView(chip)
            if (ten == iconChon) b.nhomIcon.check(chip.id)
        }
        b.nhomIcon.setOnCheckedStateChangeListener { g, ids ->
            iconChon = ids.firstOrNull()?.let { g.findViewById<Chip>(it).tag as String } ?: return@setOnCheckedStateChangeListener
        }

        b.btnHuy.setOnClickListener { bang.dismiss() }
        b.btnLuu.setOnClickListener {
            val khauHao = (b.nhapKhauHao.text.toString().toIntOrNull() ?: -1) / 100.0
            lifecycleScope.launch {
                try {
                    if (con == null) repo.them(cha.ma, b.nhapTen.text.toString(), iconChon, khauHao)
                    else repo.capNhat(con.ma, b.nhapTen.text.toString(), iconChon, khauHao, b.swAn.isChecked)
                    bang.dismiss()
                    Toast.makeText(this@QuanLyDanhMucActivity, "Đã lưu danh mục", Toast.LENGTH_SHORT).show()
                    taiDuLieu()
                } catch (e: IllegalStateException) {
                    val loiKhauHao = e.message?.contains("khấu hao") == true
                    b.oKhauHao.error = if (loiKhauHao) e.message else null
                    b.oTen.error = if (loiKhauHao) null else e.message
                }
            }
        }
        bang.show()
    }
}

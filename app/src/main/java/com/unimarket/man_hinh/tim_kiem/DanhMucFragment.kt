package com.unimarket.man_hinh.tim_kiem

import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.unimarket.R
import com.unimarket.databinding.FragmentDanhMucBinding
import com.unimarket.databinding.HangMenuBinding
import com.unimarket.du_lieu.repository.TinDangRepository
import com.unimarket.mo_hinh.TinDang
import com.unimarket.tien_ich.HangSo
import com.unimarket.xu_ly_nghiep_vu.LocTinDang.PhamVi
import kotlinx.coroutines.launch

/**
 * Tab Danh mục: Sách / Đồ điện tử / Đồ dùng. Chạm danh mục cha để mở ra danh mục con,
 * chạm danh mục con để xem kết quả (mở màn Tìm kiếm đã lọc sẵn).
 */
class DanhMucFragment : Fragment() {

    private var _binding: FragmentDanhMucBinding? = null
    private val binding get() = _binding!!
    private var dangMo: String? = HangSo.SACH   // danh mục cha đang mở

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDanhMucBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        taiDuLieu()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        if (!hidden) taiDuLieu()
    }

    private fun taiDuLieu() {
        viewLifecycleOwner.lifecycleScope.launch {
            val ds = TinDangRepository(requireContext()).layDangHienThi()
            ve(ds)
        }
    }

    /** Vẽ danh sách: mỗi danh mục cha là một khối bo góc. */
    private fun ve(ds: List<TinDang>) {
        if (_binding == null) return
        val ctx = requireContext()
        // Số tin đếm trên toàn quốc nên kết quả cũng mở ở phạm vi toàn quốc (thu hẹp lại ở màn Tìm kiếm)
        val phamVi = PhamVi.TOAN_QUOC
        binding.dsDanhMuc.removeAllViews()

        for (dm in HangSo.DS_DANH_MUC) {
            val khoi = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundResource(R.drawable.bg_the)
                layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = 24 }
            }
            binding.dsDanhMuc.addView(khoi)
            val moRa = dangMo == dm.ma

            // Dòng danh mục cha
            val dau = HangMenuBinding.inflate(layoutInflater, khoi, true)
            dau.iconMenu.setImageResource(dm.icon)
            dau.iconMenu.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(ctx, dm.mauIcon))
            dau.tvTieuDe.text = dm.ten
            dau.tvTieuDe.setTypeface(null, Typeface.BOLD)
            dau.tvPhu.text = "${dm.dsCon.size} danh mục con · ${ds.count { it.danhMucCha == dm.ma }} tin"
            dau.iconMuiTen.setImageResource(if (moRa) R.drawable.ic_down else R.drawable.ic_chev)
            dau.root.setOnClickListener {
                dangMo = if (moRa) null else dm.ma
                ve(ds)
            }
            if (!moRa) continue

            // Các dòng danh mục con
            val tatCa = HangMenuBinding.inflate(layoutInflater, khoi, true)
            tatCa.iconMenu.setImageResource(R.drawable.ic_grid)
            tatCa.tvTieuDe.text = "Tất cả ${dm.ten.lowercase()}"
            tatCa.tvPhu.visibility = View.GONE
            tatCa.root.setOnClickListener { TimKiemActivity.mo(ctx, danhMucCha = dm.ma, phamVi = phamVi) }

            for (con in dm.dsCon) {
                val hang = HangMenuBinding.inflate(layoutInflater, khoi, true)
                hang.iconMenu.setImageResource(con.icon)
                hang.iconMenu.imageTintList = ContextCompat.getColorStateList(ctx, R.color.chu_phu)
                hang.tvTieuDe.text = con.ten
                hang.tvPhu.text = "${ds.count { it.danhMucCon == con.ma }} tin"
                hang.root.setOnClickListener {
                    TimKiemActivity.mo(ctx, danhMucCha = dm.ma, danhMucCon = con.ma, phamVi = phamVi)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

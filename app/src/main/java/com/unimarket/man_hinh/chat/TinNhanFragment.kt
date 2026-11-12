package com.unimarket.man_hinh.chat

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.unimarket.R
import com.unimarket.databinding.ChipLocBinding
import com.unimarket.databinding.FragmentTinNhanBinding
import com.unimarket.du_lieu.repository.GiaoDichRepository
import com.unimarket.du_lieu.repository.GiaoDichRepository.DongCuoc
import com.unimarket.du_lieu.repository.NguoiDungRepository
import com.unimarket.man_hinh.dang_nhap.DangNhapActivity
import com.unimarket.man_hinh.trang_chu.ManHinhChinhActivity
import com.unimarket.tien_ich.HangSo
import com.unimarket.xu_ly_nghiep_vu.QuyTacLichHen
import kotlinx.coroutines.launch

/**
 * Tab Tin nhắn (SCR-21, UC-38): danh sách cuộc trao đổi, lọc theo trạng thái hẹn.
 * Thay cho màn Lịch hẹn riêng: bộ lọc "Chờ tôi xác nhận" chính là danh sách việc cần làm.
 */
class TinNhanFragment : Fragment() {

    enum class Loc(val ten: String) {
        TAT_CA("Tất cả"),
        CHO_TOI("Chờ tôi xác nhận"),
        DA_CHOT("Đã chốt hẹn"),
        DANG_MUA("Tôi đang mua"),
        DANG_BAN("Tôi đang bán")
    }

    private var _binding: FragmentTinNhanBinding? = null
    private val binding get() = _binding!!
    private val adapter = CuocTroChuyenAdapter { PhongChatActivity.mo(requireContext(), it.cuoc.id) }

    private var loc = Loc.TAT_CA
    private var locTinId: Long? = null       // lọc theo một tin (mở từ "N người đang hỏi mua")
    private var tenTinLoc = ""
    private var tatCa: List<DongCuoc> = emptyList()
    private var toiId = 0L

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTinNhanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.dsCuoc.layoutManager = LinearLayoutManager(requireContext())
        binding.dsCuoc.adapter = adapter
        for (l in Loc.entries) {
            val chip = ChipLocBinding.inflate(layoutInflater, binding.nhomLoc, false).root
            chip.id = View.generateViewId()
            chip.tag = l
            chip.text = l.ten
            binding.nhomLoc.addView(chip)
            if (l == loc) binding.nhomLoc.check(chip.id)
        }
        binding.nhomLoc.setOnCheckedStateChangeListener { g, ids ->
            loc = ids.firstOrNull()?.let { g.findViewById<Chip>(it).tag as Loc } ?: return@setOnCheckedStateChangeListener
            ve()
        }
        binding.chipLocTin.setOnCloseIconClickListener {
            locTinId = null
            ve()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isHidden) taiDuLieu()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        if (!hidden) taiDuLieu()
    }

    /** Gọi từ màn chính khi mở tab bằng dòng "N người đang hỏi mua" ở Tin của tôi. */
    fun locTheoTin(tinId: Long, tieuDe: String) {
        locTinId = tinId
        tenTinLoc = tieuDe
        loc = Loc.TAT_CA
        _binding?.let { b ->
            b.nhomLoc.findViewWithTag<Chip>(Loc.TAT_CA)?.let { b.nhomLoc.check(it.id) }
            taiDuLieu()
        }
    }

    private fun taiDuLieu() {
        viewLifecycleOwner.lifecycleScope.launch {
            val nd = NguoiDungRepository(requireContext()).layNguoiDangNhap()
            if (nd == null) {
                veKhach()
                return@launch
            }
            toiId = nd.id
            tatCa = GiaoDichRepository(requireContext()).layDsCuoc(nd.id)
            ve()
            (activity as? ManHinhChinhActivity)?.capNhatSoChuaDoc()
        }
    }

    private fun thoa(d: DongCuoc, l: Loc): Boolean = when (l) {
        Loc.TAT_CA -> true
        Loc.CHO_TOI -> QuyTacLichHen.choToiXacNhan(d.lich, toiId)
        Loc.DA_CHOT -> d.lich?.trangThai == HangSo.LICH_DA_CHOT
        Loc.DANG_MUA -> d.laNguoiMua
        Loc.DANG_BAN -> !d.laNguoiMua
    }

    private fun ve() {
        val b = _binding ?: return
        b.khungLoc.visibility = View.VISIBLE
        val theoTin = tatCa.filter { locTinId == null || it.tin.id == locTinId }

        // Số đếm trên chip "Chờ tôi xác nhận" và "Đã chốt hẹn"
        for (i in 0 until b.nhomLoc.childCount) {
            val chip = b.nhomLoc.getChildAt(i) as Chip
            val l = chip.tag as Loc
            val dem = theoTin.count { thoa(it, l) }
            chip.text = if (l == Loc.CHO_TOI || l == Loc.DA_CHOT) (if (dem > 0) "${l.ten} ($dem)" else l.ten) else l.ten
        }
        b.chipLocTin.visibility = if (locTinId == null) View.GONE else View.VISIBLE
        b.chipLocTin.text = "Tin: $tenTinLoc"

        val ds = theoTin.filter { thoa(it, loc) }
        adapter.capNhat(ds, toiId)
        b.dsCuoc.visibility = View.VISIBLE
        b.trong.root.visibility = if (ds.isEmpty()) View.VISIBLE else View.GONE
        b.trong.iconTrong.setImageResource(R.drawable.ic_chat)
        b.trong.tvTieuDeTrong.text = when {
            locTinId != null && theoTin.isEmpty() -> "Chưa có ai hỏi mua tin này"
            loc == Loc.CHO_TOI -> "Không có việc cần xác nhận"
            tatCa.isEmpty() -> "Chưa có cuộc trò chuyện nào"
            else -> "Không có cuộc trò chuyện ở mục này"
        }
        b.trong.tvPhuTrong.text = if (tatCa.isEmpty()) "Bấm Chat hoặc Đặt mua ở một tin để bắt đầu trao đổi với người bán." else ""
        b.trong.btnTrong1.visibility = View.GONE
        b.trong.btnTrong2.visibility = View.GONE
    }

    /** Khách chưa đăng nhập: chỉ hiện lời mời đăng nhập. */
    private fun veKhach() {
        val b = _binding ?: return
        adapter.capNhat(emptyList(), 0)
        b.khungLoc.visibility = View.GONE
        b.chipLocTin.visibility = View.GONE
        b.trong.root.visibility = View.VISIBLE
        b.trong.iconTrong.setImageResource(R.drawable.ic_chat)
        b.trong.tvTieuDeTrong.text = "Đăng nhập để nhắn tin"
        b.trong.tvPhuTrong.text = "Trao đổi và hẹn giao dịch với người bán ngay trong app."
        b.trong.btnTrong1.visibility = View.VISIBLE
        b.trong.btnTrong1.text = "Đăng nhập"
        b.trong.btnTrong1.setOnClickListener {
            startActivity(
                Intent(requireContext(), DangNhapActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
        }
        b.trong.btnTrong2.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

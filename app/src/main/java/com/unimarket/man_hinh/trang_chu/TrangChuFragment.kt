package com.unimarket.man_hinh.trang_chu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.unimarket.databinding.FragmentTrangChuBinding
import com.unimarket.du_lieu.repository.NguoiDungRepository
import com.unimarket.du_lieu.repository.TinDangRepository
import com.unimarket.tien_ich.HangSo
import kotlinx.coroutines.launch

/**
 * Tab Trang chủ: lời chào + lưới tin mới nhất đọc từ database.
 * (Sprint 2 sẽ bổ sung: ô tìm kiếm, phạm vi Trường/Thành phố/Toàn quốc, tin nổi bật, mở chi tiết tin.)
 */
class TrangChuFragment : Fragment() {

    private var _binding: FragmentTrangChuBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: TinDangAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTrangChuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = TinDangAdapter { tin ->
            Toast.makeText(requireContext(), "Chi tiết “${tin.tieuDe}” sẽ làm ở Sprint 2", Toast.LENGTH_SHORT).show()
        }
        binding.dsTin.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.dsTin.adapter = adapter
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
            val nd = NguoiDungRepository(requireContext()).layNguoiDangNhap()
            val truong = HangSo.timTruong(nd?.maTruong)
            binding.tvChao.text = when {
                nd == null -> "Bạn đang xem với tư cách khách"
                truong == null -> "Xin chào, ${nd.hoTen}"
                else -> "Xin chào, ${nd.hoTen} · ${truong.ma}, ${truong.thanhPho}"
            }
            val ds = TinDangRepository(requireContext()).layTinMoi()
            binding.tvSoTin.text = "${ds.size} tin"
            adapter.capNhat(ds)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

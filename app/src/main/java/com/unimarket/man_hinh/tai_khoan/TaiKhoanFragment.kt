package com.unimarket.man_hinh.tai_khoan

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.unimarket.R
import com.unimarket.databinding.FragmentTaiKhoanBinding
import com.unimarket.databinding.HangMenuBinding
import com.unimarket.du_lieu.repository.NguoiDungRepository
import com.unimarket.du_lieu.repository.TinDangRepository
import com.unimarket.man_hinh.dang_nhap.DangNhapActivity
import com.unimarket.mo_hinh.NguoiDung
import com.unimarket.tien_ich.DinhDang
import com.unimarket.tien_ich.HangSo
import com.unimarket.tien_ich.LuuAnh
import com.unimarket.xu_ly_nghiep_vu.KiemTraHoSo
import kotlinx.coroutines.launch

/** Tab Tài khoản: thông tin cá nhân, trạng thái hồ sơ, số liệu và menu chức năng. */
class TaiKhoanFragment : Fragment() {

    private var _binding: FragmentTaiKhoanBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTaiKhoanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnDangNhap.setOnClickListener { veManDangNhap() }

        // Các mục chưa làm tới sẽ báo sprint thực hiện
        datHang(binding.hangTinNhan, R.drawable.ic_chat, "Tin nhắn & giao dịch", "Chat và lịch hẹn") { baoSprint("Tin nhắn", 4) }
        datHang(binding.hangThongBao, R.drawable.ic_bell, "Thông báo", "Có người mua, chốt hẹn, nhắc hẹn") { baoSprint("Thông báo", 5) }
        datHang(binding.hangTinCuaToi, R.drawable.ic_list, "Tin của tôi", "Đang bán, đã bán, hết hạn") { baoSprint("Tin của tôi", 3) }
        datHang(binding.hangDaLuu, R.drawable.ic_heart, "Đã lưu", "0 tin") {
            startActivity(Intent(requireContext(), DaLuuActivity::class.java))
        }
        datHang(binding.hangHoSo, R.drawable.ic_edit, "Hồ sơ", "Họ tên, SĐT, trường, điểm giao dịch") {
            startActivity(Intent(requireContext(), HoSoActivity::class.java))
        }
        datHang(binding.hangQuanTri, R.drawable.ic_chart, "Quản trị", "Thống kê, biểu đồ, kiểm duyệt, xem dữ liệu") { baoSprint("Quản trị", 6) }
        datHang(binding.hangDangXuat, R.drawable.ic_logout, "Đăng xuất", "") { hoiDangXuat() }
        val mauDo = ContextCompat.getColor(requireContext(), R.color.loi)
        binding.hangDangXuat.tvTieuDe.setTextColor(mauDo)
        binding.hangDangXuat.iconMenu.imageTintList = ColorStateList.valueOf(mauDo)
        binding.hangDangXuat.iconMuiTen.visibility = View.GONE
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
            binding.khungKhach.visibility = if (nd == null) View.VISIBLE else View.GONE
            binding.khungTaiKhoan.visibility = if (nd == null) View.GONE else View.VISIBLE
            if (nd == null) return@launch

            hienThongTin(nd)
            val repoTin = TinDangRepository(requireContext())
            val (dangBan, daBan) = repoTin.demDangBanVaDaBan(nd.id)
            val daLuu = repoTin.demDaLuu(nd.id)
            binding.tvSoDangBan.text = oSoLieu(dangBan, "Đang bán")
            binding.tvSoDaBan.text = oSoLieu(daBan, "Đã bán")
            binding.tvSoDaLuu.text = oSoLieu(daLuu, "Đã lưu")
            binding.hangDaLuu.tvPhu.text = "$daLuu tin"
        }
    }

    private fun hienThongTin(nd: NguoiDung) {
        binding.tvHoTen.text = nd.hoTen
        val truong = HangSo.timTruong(nd.maTruong)
        binding.tvTruong.text = if (truong == null) "Chưa chọn trường" else "${truong.ten} · ${truong.thanhPho}"

        // Ảnh đại diện: có ảnh thì hiện ảnh, không thì hiện chữ viết tắt
        binding.tvVietTat.text = DinhDang.vietTat(nd.hoTen)
        val coAnh = LuuAnh.hienAnh(binding.anhDaiDien, nd.anhDaiDien, 200)
        binding.anhDaiDien.visibility = if (coAnh) View.VISIBLE else View.GONE

        // Nhãn trạng thái hồ sơ
        val thieu = KiemTraHoSo.mucConThieu(nd)
        val ctx = requireContext()
        if (thieu.isEmpty()) {
            binding.tvTrangThaiHoSo.text = "Hồ sơ đầy đủ"
            binding.tvTrangThaiHoSo.setTextColor(ContextCompat.getColor(ctx, R.color.tren_chinh_nhat))
            binding.tvTrangThaiHoSo.backgroundTintList = ContextCompat.getColorStateList(ctx, R.color.chinh_nhat)
            binding.hangHoSo.tvPhu.text = "Họ tên, SĐT, trường, điểm giao dịch"
        } else {
            binding.tvTrangThaiHoSo.text = "Hồ sơ thiếu ${thieu.size} mục"
            binding.tvTrangThaiHoSo.setTextColor(ContextCompat.getColor(ctx, R.color.loi))
            binding.tvTrangThaiHoSo.backgroundTintList = ContextCompat.getColorStateList(ctx, R.color.nen_loi)
            binding.hangHoSo.tvPhu.text = "Thiếu: ${thieu.joinToString(", ")}"
        }

        binding.khungQuanTri.visibility =
            if (nd.vaiTro == HangSo.VAI_TRO_QUAN_TRI) View.VISIBLE else View.GONE
    }

    /** Ô số liệu: số to đậm ở trên, nhãn nhỏ ở dưới. */
    private fun oSoLieu(so: Int, nhan: String): CharSequence {
        val sb = SpannableStringBuilder(so.toString())
        sb.setSpan(StyleSpan(Typeface.BOLD), 0, sb.length, 0)
        sb.setSpan(RelativeSizeSpan(1.4f), 0, sb.length, 0)
        sb.append("\n").append(nhan)
        return sb
    }

    private fun datHang(hang: HangMenuBinding, icon: Int, tieuDe: String, phu: String, khiBam: () -> Unit) {
        hang.iconMenu.setImageResource(icon)
        hang.tvTieuDe.text = tieuDe
        hang.tvPhu.text = phu
        hang.tvPhu.visibility = if (phu.isEmpty()) View.GONE else View.VISIBLE
        hang.root.setOnClickListener { khiBam() }
    }

    private fun baoSprint(chucNang: String, sprint: Int) {
        Toast.makeText(requireContext(), "$chucNang sẽ làm ở Sprint $sprint", Toast.LENGTH_SHORT).show()
    }

    private fun hoiDangXuat() {
        AlertDialog.Builder(requireContext())
            .setTitle("Đăng xuất?")
            .setMessage("Bạn có thể đăng nhập lại bằng tài khoản khác.")
            .setPositiveButton("Đăng xuất") { _, _ ->
                NguoiDungRepository(requireContext()).dangXuat()
                veManDangNhap()
            }
            .setNegativeButton("Huỷ", null)
            .show()
    }

    private fun veManDangNhap() {
        startActivity(
            Intent(requireContext(), DangNhapActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

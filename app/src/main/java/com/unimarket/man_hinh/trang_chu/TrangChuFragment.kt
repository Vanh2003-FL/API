package com.unimarket.man_hinh.trang_chu

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.unimarket.R
import com.unimarket.databinding.FragmentTrangChuBinding
import com.unimarket.databinding.ODanhMucBinding
import com.unimarket.du_lieu.repository.NguoiDungRepository
import com.unimarket.du_lieu.repository.TinDangRepository
import com.unimarket.man_hinh.chi_tiet_san_pham.ChiTietSanPhamActivity
import com.unimarket.man_hinh.chi_tiet_san_pham.LuuTin
import com.unimarket.man_hinh.tai_khoan.BoSungHoSo
import com.unimarket.man_hinh.tai_khoan.DaLuuActivity
import com.unimarket.man_hinh.tim_kiem.TimKiemActivity
import com.unimarket.mo_hinh.NguoiDung
import com.unimarket.mo_hinh.TinDang
import com.unimarket.tien_ich.HangSo
import com.unimarket.xu_ly_nghiep_vu.LocTinDang
import com.unimarket.xu_ly_nghiep_vu.LocTinDang.PhamVi
import kotlinx.coroutines.launch

/**
 * Tab Trang chủ: ô tìm kiếm, phạm vi xem (Trường tôi / Thành phố / Toàn quốc), 3 danh mục,
 * mục Nổi bật (tin được đẩy), Sách cho sinh viên trường tôi, và lưới Tin mới.
 */
class TrangChuFragment : Fragment() {

    companion object {
        /** Phạm vi đang chọn, dùng chung với tab Danh mục. null = chưa chọn (lấy mặc định theo tài khoản). */
        var phamVi: PhamVi? = null
    }

    private var _binding: FragmentTrangChuBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapterNoiBat: TinDangAdapter
    private lateinit var adapterSach: TinDangAdapter
    private lateinit var adapterTinMoi: TinDangAdapter

    private var nguoiDung: NguoiDung? = null
    private var tatCa: List<TinDang> = emptyList()
    private var daLuu: Set<Long> = emptySet()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTrangChuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val act = requireActivity() as AppCompatActivity
        binding.trong.root.visibility = View.GONE

        binding.btnDaLuu.setOnClickListener {
            BoSungHoSo.canDangNhap(act) { startActivity(Intent(act, DaLuuActivity::class.java)) }
        }
        binding.oTimKiem.setOnClickListener { TimKiemActivity.mo(act, phamVi = phamViHienTai()) }
        binding.btnXemTatCaSach.setOnClickListener {
            TimKiemActivity.mo(act, danhMucCha = HangSo.SACH, phamVi = PhamVi.TRUONG)
        }

        // 3 ô danh mục
        for (dm in HangSo.DS_DANH_MUC) {
            val o = ODanhMucBinding.inflate(layoutInflater, binding.hangDanhMuc, true)
            o.iconDanhMuc.setImageResource(dm.icon)
            o.iconDanhMuc.backgroundTintList = ContextCompat.getColorStateList(act, dm.mauNen)
            o.iconDanhMuc.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(act, dm.mauIcon))
            o.tvTenDanhMuc.text = dm.ten
            o.root.setOnClickListener { TimKiemActivity.mo(act, danhMucCha = dm.ma, phamVi = phamViHienTai()) }
        }

        // 3 danh sách tin; chạm thẻ → chi tiết, chạm trái tim → lưu
        val moChiTiet: (TinDang) -> Unit = { ChiTietSanPhamActivity.mo(act, it.id) }
        val bamTim: (TinDang) -> Unit = { tin -> LuuTin.doi(act, tin.id) { taiDuLieu() } }
        adapterNoiBat = TinDangAdapter(ngang = true, khiBamTim = bamTim, khiBam = moChiTiet)
        adapterSach = TinDangAdapter(ngang = true, khiBamTim = bamTim, khiBam = moChiTiet)
        adapterTinMoi = TinDangAdapter(khiBamTim = bamTim, khiBam = moChiTiet)
        caiDanhSach(binding.dsNoiBat, adapterNoiBat, ngang = true)
        caiDanhSach(binding.dsSach, adapterSach, ngang = true)
        caiDanhSach(binding.dsTin, adapterTinMoi, ngang = false)

        binding.nhomPhamVi.setOnCheckedStateChangeListener { _, ids ->
            phamVi = when (ids.firstOrNull()) {
                R.id.chipTruong -> PhamVi.TRUONG
                R.id.chipThanhPho -> PhamVi.THANH_PHO
                else -> PhamVi.TOAN_QUOC
            }
            veLai()
        }
    }

    private fun caiDanhSach(rv: RecyclerView, adapter: TinDangAdapter, ngang: Boolean) {
        rv.layoutManager = if (ngang) LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        else GridLayoutManager(requireContext(), 2)
        rv.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        taiDuLieu()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        if (!hidden) taiDuLieu()
    }

    /** Đọc lại người dùng, toàn bộ tin đang hiển thị và danh sách đã lưu từ database. */
    private fun taiDuLieu() {
        viewLifecycleOwner.lifecycleScope.launch {
            val nd = NguoiDungRepository(requireContext()).layNguoiDangNhap()
            val repo = TinDangRepository(requireContext())
            nguoiDung = nd
            tatCa = repo.layDangHienThi()
            daLuu = if (nd == null) emptySet() else repo.layIdDaLuu(nd.id)
            datChipPhamVi(nd)
            veLai()
        }
    }

    /** Khách hoặc chưa chọn trường thì chỉ xem được Toàn quốc. */
    private fun datChipPhamVi(nd: NguoiDung?) {
        val truong = HangSo.timTruong(nd?.maTruong)
        binding.chipTruong.visibility = if (truong == null) View.GONE else View.VISIBLE
        binding.chipThanhPho.visibility = if (truong == null) View.GONE else View.VISIBLE
        binding.chipThanhPho.text = truong?.thanhPho ?: ""
        binding.tvChao.text = when {
            nd == null -> "Bạn đang xem với tư cách khách"
            truong == null -> "Xin chào, ${nd.hoTen}"
            else -> "Xin chào, ${nd.hoTen} · ${truong.ma}, ${truong.thanhPho}"
        }
        val pv = phamViHienTai()
        val chip = when (pv) {
            PhamVi.TRUONG -> R.id.chipTruong
            PhamVi.THANH_PHO -> R.id.chipThanhPho
            PhamVi.TOAN_QUOC -> R.id.chipToanQuoc
        }
        if (binding.nhomPhamVi.checkedChipId != chip) binding.nhomPhamVi.check(chip)
    }

    /** Phạm vi đang dùng: mặc định là thành phố của người dùng (khách thì toàn quốc). */
    private fun phamViHienTai(): PhamVi {
        val coTruong = HangSo.timTruong(nguoiDung?.maTruong) != null
        val pv = phamVi ?: if (coTruong) PhamVi.THANH_PHO else PhamVi.TOAN_QUOC
        return if (!coTruong) PhamVi.TOAN_QUOC else pv
    }

    /** Lọc lại theo phạm vi và vẽ 3 mục. */
    private fun veLai() {
        if (_binding == null) return
        val nd = nguoiDung
        val pv = phamViHienTai()
        val maTruong = nd?.maTruong ?: ""
        val thanhPho = nd?.thanhPho ?: ""
        val trongPhamVi = LocTinDang.loc(tatCa, LocTinDang.DieuKien(phamVi = pv, maTruong = maTruong, thanhPho = thanhPho))
        val noiDiaDanh = when (pv) {
            PhamVi.TRUONG -> "ở $maTruong"
            PhamVi.THANH_PHO -> "ở $thanhPho"
            PhamVi.TOAN_QUOC -> "toàn quốc"
        }

        // Nổi bật: tin đang được đẩy trong phạm vi
        val noiBat = trongPhamVi.filter { LocTinDang.dangNoiBat(it) }
        binding.mucNoiBat.visibility = if (noiBat.isEmpty()) View.GONE else View.VISIBLE
        adapterNoiBat.capNhat(noiBat, daLuu)

        // Sách cho sinh viên trường tôi
        val sach = tatCa.filter { it.danhMucCha == HangSo.SACH && maTruong.isNotEmpty() && it.maTruong == maTruong }
        binding.mucSach.visibility = if (sach.isEmpty()) View.GONE else View.VISIBLE
        binding.tvTieuDeSach.text = "Sách cho sinh viên $maTruong"
        adapterSach.capNhat(sach, daLuu)

        // Tin mới
        binding.tvTieuDeTinMoi.text = "Tin mới $noiDiaDanh"
        binding.tvSoTin.text = "${trongPhamVi.size} tin"
        adapterTinMoi.capNhat(trongPhamVi, daLuu)
        val trong = binding.trong
        trong.root.visibility = if (trongPhamVi.isEmpty()) View.VISIBLE else View.GONE
        trong.tvTieuDeTrong.text = "Chưa có tin nào $noiDiaDanh"
        trong.tvPhuTrong.text = "Hãy là người đầu tiên đăng tin!"
        trong.btnTrong1.visibility = if (pv != PhamVi.TOAN_QUOC) View.VISIBLE else View.GONE
        trong.btnTrong1.text = "Xem toàn quốc"
        trong.btnTrong1.setOnClickListener { binding.nhomPhamVi.check(R.id.chipToanQuoc) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

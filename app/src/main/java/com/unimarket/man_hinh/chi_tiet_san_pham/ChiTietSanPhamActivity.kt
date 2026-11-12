package com.unimarket.man_hinh.chi_tiet_san_pham

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.unimarket.R
import com.unimarket.databinding.ActivityChiTietSanPhamBinding
import com.unimarket.databinding.BottomSheetBaoCaoBinding
import com.unimarket.databinding.HangMenuBinding
import com.unimarket.databinding.HangThuocTinhBinding
import com.unimarket.du_lieu.repository.NguoiDungRepository
import com.unimarket.du_lieu.repository.TinDangRepository
import com.unimarket.du_lieu.repository.TinDangRepository.KetQuaBaoCao
import com.unimarket.man_hinh.tai_khoan.BoSungHoSo
import com.unimarket.man_hinh.trang_chu.TinDangAdapter
import com.unimarket.mo_hinh.NguoiDung
import com.unimarket.mo_hinh.TinDang
import com.unimarket.tien_ich.CanhLe
import com.unimarket.tien_ich.DinhDang
import com.unimarket.tien_ich.HangSo
import com.unimarket.tien_ich.LuuAnh
import kotlinx.coroutines.launch

/**
 * Màn Chi tiết tin đăng.
 * - Mỗi lần người KHÁC (không phải người bán) mở tin thì lượt xem +1.
 * - Lưu tin, chia sẻ, báo cáo vi phạm, gọi / Zalo cho người bán.
 * - Nút Chat và Đặt mua cần hồ sơ đầy đủ (phòng chat + lịch hẹn làm ở Sprint 4).
 */
class ChiTietSanPhamActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_ID = "tin_dang_id"

        fun mo(context: Context, tinDangId: Long) {
            context.startActivity(Intent(context, ChiTietSanPhamActivity::class.java).putExtra(EXTRA_ID, tinDangId))
        }
    }

    private lateinit var binding: ActivityChiTietSanPhamBinding
    private lateinit var repo: TinDangRepository
    private lateinit var adapterTuongTu: TinDangAdapter

    private var tin: TinDang? = null
    private var nguoiBan: NguoiDung? = null
    private var nguoiXem: NguoiDung? = null
    private var daLuu = false
    private var daDemLuotXem = false
    private var dsAnh: List<String> = emptyList()
    private var viTriAnh = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChiTietSanPhamBinding.inflate(layoutInflater)
        setContentView(binding.root)
        CanhLe.chuaVienHeThong(binding.root)
        repo = TinDangRepository(this)

        adapterTuongTu = TinDangAdapter(ngang = true, khiBam = { mo(this, it.id) })
        binding.dsTuongTu.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.dsTuongTu.adapter = adapterTuongTu

        binding.btnQuayLai.setOnClickListener { finish() }
        binding.btnChiaSe.setOnClickListener { chiaSe() }
        binding.btnTim.setOnClickListener {
            val t = tin ?: return@setOnClickListener
            LuuTin.doi(this, t.id) { daLuu = it; veNutTim() }
        }
        binding.btnBaoCao.setOnClickListener { BoSungHoSo.canDangNhap(this) { moBaoCao() } }
        binding.btnAnhTruoc.setOnClickListener { doiAnh(-1) }
        binding.btnAnhSau.setOnClickListener { doiAnh(+1) }
        binding.btnGoi.setOnClickListener { lienHe(zalo = false) }
        binding.btnZalo.setOnClickListener { lienHe(zalo = true) }

        // Chat và Đặt mua: cần hồ sơ đầy đủ (cổng chặn), chức năng thật làm ở Sprint 4
        binding.btnChat.setOnClickListener {
            BoSungHoSo.kiemTra(this) { thongBao("Phòng chat sẽ làm ở Sprint 4") }
        }
        binding.btnDatMua.setOnClickListener {
            BoSungHoSo.kiemTra(this) { thongBao("Đặt mua + lịch hẹn sẽ làm ở Sprint 4") }
        }
    }

    override fun onResume() {
        super.onResume()
        taiDuLieu()
    }

    private fun taiDuLieu() {
        val id = intent.getLongExtra(EXTRA_ID, -1)
        lifecycleScope.launch {
            var t = repo.layTheoId(id)
            if (t == null) {
                thongBao("Tin không còn tồn tại")
                finish()
                return@launch
            }
            val nx = NguoiDungRepository(this@ChiTietSanPhamActivity).layNguoiDangNhap()
            // Đếm lượt xem: chỉ một lần mỗi lần mở màn, và không đếm khi người bán tự xem tin mình
            if (!daDemLuotXem && nx?.id != t.nguoiBanId) {
                repo.tangLuotXem(t.id)
                t = t.copy(luotXem = t.luotXem + 1)
            }
            daDemLuotXem = true
            tin = t
            nguoiXem = nx
            daLuu = nx != null && t.id in repo.layIdDaLuu(nx.id)
            val ttNguoiBan = repo.thongTinNguoiBan(t.nguoiBanId)
            nguoiBan = ttNguoiBan.nguoiDung

            hienTin(t)
            hienNguoiBan(t, ttNguoiBan)
            veNutTim()

            val tuongTu = repo.layTinTuongTu(t)
            binding.tvNhanTuongTu.visibility = if (tuongTu.isEmpty()) View.GONE else View.VISIBLE
            adapterTuongTu.capNhat(tuongTu)
        }
    }

    // ------------------------------------------------------------------
    private fun hienTin(t: TinDang) {
        val cha = HangSo.timChaCua(t.danhMucCon)
        binding.khungAnh.setBackgroundColor(ContextCompat.getColor(this, cha.mauNen))
        binding.iconLon.setImageResource(HangSo.timDanhMucCon(t.danhMucCon)?.icon ?: cha.icon)
        binding.iconLon.imageTintList = ContextCompat.getColorStateList(this, cha.mauIcon)
        dsAnh = t.anh.split(";").filter { it.isNotBlank() }
        viTriAnh = viTriAnh.coerceIn(0, maxOf(dsAnh.size - 1, 0))
        hienAnh()

        // Dải thông báo theo trạng thái tin
        val dai = when (t.trangThai) {
            HangSo.TIN_DANG_GIAO_DICH -> "Đã có người hẹn mua · bạn vẫn có thể xếp hàng"
            HangSo.TIN_DA_BAN -> "Tin này đã bán"
            HangSo.TIN_HET_HAN -> "Tin đã hết hạn"
            HangSo.TIN_BI_AN -> "Tin đang tạm ẩn, chờ quản trị viên kiểm duyệt"
            else -> null
        }
        binding.tvDaiThongBao.visibility = if (dai == null) View.GONE else View.VISIBLE
        binding.tvDaiThongBao.text = dai

        binding.tvGia.text = DinhDang.gia(t.gia)
        binding.tvTieuDe.text = t.tieuDe
        val tt = HangSo.timTinhTrang(t.tinhTrang)
        binding.tvTinhTrang.text = tt.ten
        binding.tvTinhTrang.backgroundTintList = ContextCompat.getColorStateList(this, tt.mau)
        binding.tvDiaDiem.text = "${t.diemGiaoDich} · ${t.maTruong}"

        val truong = HangSo.timTruong(t.maTruong)
        val phu = StringBuilder("${DinhDang.thoiGianTruoc(t.ngayTao)} · ${t.luotXem} lượt xem")
        if (truong != null) phu.append("\n${truong.ten}, ${truong.thanhPho}")
        binding.tvThongTinPhu.text = phu

        // Bảng thông tin chi tiết: mỗi dòng "Tên: giá trị" trong cột thuocTinh
        binding.bangThuocTinh.removeAllViews()
        val dong = t.thuocTinh.lines().filter { it.contains(":") }.map { it.substringBefore(":").trim() to it.substringAfter(":").trim() }
            .toMutableList()
        dong.add(0, "Danh mục" to "${cha.ten} › ${HangSo.timDanhMucCon(t.danhMucCon)?.ten ?: ""}")
        if (t.ghiChuDiaDiem.isNotBlank()) dong.add("Ghi chú hẹn" to t.ghiChuDiaDiem)
        if (t.coGiaoHang) dong.add("Giao hàng" to "Có thể giao tận nơi")
        for ((ten, giaTri) in dong) {
            val h = HangThuocTinhBinding.inflate(layoutInflater, binding.bangThuocTinh, true)
            h.tvTen.text = ten
            h.tvGiaTri.text = giaTri
        }
        binding.tvMoTa.text = t.moTa.ifBlank { "Người bán chưa viết mô tả." }

        // Tin của chính mình: không báo cáo, không đặt mua
        val cuaToi = nguoiXem?.id == t.nguoiBanId
        val conBan = t.trangThai == HangSo.TIN_DANG_BAN || t.trangThai == HangSo.TIN_DANG_GIAO_DICH
        binding.btnBaoCao.visibility = if (cuaToi) View.GONE else View.VISIBLE
        binding.btnChat.visibility = if (cuaToi) View.GONE else View.VISIBLE
        binding.btnDatMua.visibility = if (cuaToi) View.GONE else View.VISIBLE
        binding.tvTinCuaBan.visibility = if (cuaToi) View.VISIBLE else View.GONE
        binding.btnChat.isEnabled = conBan
        binding.btnDatMua.isEnabled = conBan
    }

    private fun hienNguoiBan(t: TinDang, tt: TinDangRepository.ThongTinNguoiBan) {
        val ten = tt.nguoiDung?.hoTen ?: "Người bán"
        binding.tvVietTatNguoiBan.text = DinhDang.vietTat(ten)
        binding.tvTenNguoiBan.text = ten
        val danhGia = tt.diemTrungBinh?.let { "★ %.1f (%d đánh giá)".format(it, tt.soDanhGia).replace('.', ',') } ?: "Chưa có đánh giá"
        binding.tvThongTinNguoiBan.text = "${t.maTruong} · $danhGia · ${tt.soLanBan} lần bán"
    }

    private fun hienAnh() {
        val coAnh = dsAnh.isNotEmpty() && LuuAnh.hienAnh(binding.anhLon, dsAnh[viTriAnh], 1080)
        binding.anhLon.visibility = if (coAnh) View.VISIBLE else View.GONE
        binding.iconLon.visibility = if (coAnh) View.GONE else View.VISIBLE
        val nhieuAnh = dsAnh.size > 1
        binding.btnAnhTruoc.visibility = if (nhieuAnh) View.VISIBLE else View.GONE
        binding.btnAnhSau.visibility = if (nhieuAnh) View.VISIBLE else View.GONE
        binding.tvSoAnh.visibility = if (nhieuAnh) View.VISIBLE else View.GONE
        binding.tvSoAnh.text = "${viTriAnh + 1}/${dsAnh.size}"
    }

    private fun doiAnh(buoc: Int) {
        if (dsAnh.isEmpty()) return
        viTriAnh = (viTriAnh + buoc + dsAnh.size) % dsAnh.size
        hienAnh()
    }

    private fun veNutTim() {
        binding.btnTim.setImageResource(if (daLuu) R.drawable.ic_heart_filled else R.drawable.ic_heart)
        binding.btnTim.imageTintList = ContextCompat.getColorStateList(this, if (daLuu) R.color.loi else R.color.chu)
        binding.btnTim.contentDescription = if (daLuu) "Bỏ lưu" else "Lưu tin"
    }

    // ------------------------------------------------------------------
    private fun chiaSe() {
        val t = tin ?: return
        val noiDung = "${t.tieuDe} – ${DinhDang.gia(t.gia)}\n${t.diemGiaoDich}, ${t.maTruong}\nXem trên UniMarket – Chợ đồ cũ sinh viên"
        val gui = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, noiDung)
        startActivity(Intent.createChooser(gui, "Chia sẻ tin"))
    }

    /** Gọi điện hoặc mở Zalo người bán. Cần hồ sơ đầy đủ để tránh quấy rối ẩn danh. */
    private fun lienHe(zalo: Boolean) {
        val sdt = nguoiBan?.soDienThoai.orEmpty()
        if (sdt.isBlank()) {
            thongBao("Người bán chưa cập nhật số điện thoại")
            return
        }
        BoSungHoSo.kiemTra(this) {
            val intent = if (zalo) Intent(Intent.ACTION_VIEW, Uri.parse("https://zalo.me/$sdt"))
            else Intent(Intent.ACTION_DIAL, Uri.parse("tel:$sdt"))
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                thongBao("Máy chưa có ứng dụng phù hợp. Số người bán: $sdt")
            }
        }
    }

    /** Bảng chọn lý do báo cáo → ghi vào bảng bao_cao (đủ 3 báo cáo thì tin tự ẩn). */
    private fun moBaoCao() {
        val t = tin ?: return
        val nd = nguoiXem ?: return
        val bang = BottomSheetDialog(this)
        val b = BottomSheetBaoCaoBinding.inflate(layoutInflater)
        bang.setContentView(b.root)
        for (lyDo in HangSo.LY_DO_BAO_CAO) {
            val h = HangMenuBinding.inflate(layoutInflater, b.dsLyDo, true)
            h.iconMenu.setImageResource(R.drawable.ic_flag)
            h.tvTieuDe.text = lyDo
            h.tvPhu.visibility = View.GONE
            h.iconMuiTen.visibility = View.GONE
            h.root.setOnClickListener {
                bang.dismiss()
                lifecycleScope.launch {
                    val ketQua = repo.baoCao(t.id, nd.id, lyDo)
                    thongBao(
                        when (ketQua) {
                            KetQuaBaoCao.DA_GHI_NHAN -> "Cảm ơn bạn đã báo cáo. Chúng mình sẽ xem xét sớm."
                            KetQuaBaoCao.TIN_TU_AN -> "Đã đủ 3 báo cáo – tin đã tạm ẩn chờ kiểm duyệt."
                            KetQuaBaoCao.DA_BAO_CAO_TRUOC -> "Bạn đã báo cáo tin này rồi."
                            KetQuaBaoCao.TIN_CUA_MINH -> "Bạn không thể báo cáo tin của mình."
                        }
                    )
                    taiDuLieu()
                }
            }
        }
        b.btnHuy.setOnClickListener { bang.dismiss() }
        bang.show()
    }

    private fun thongBao(chu: String) = Toast.makeText(this, chu, Toast.LENGTH_SHORT).show()
}

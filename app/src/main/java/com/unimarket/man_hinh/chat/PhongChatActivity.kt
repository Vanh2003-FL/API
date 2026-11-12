package com.unimarket.man_hinh.chat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.unimarket.databinding.ActivityPhongChatBinding
import com.unimarket.du_lieu.repository.GiaoDichRepository
import com.unimarket.du_lieu.repository.GiaoDichRepository.PhongChat
import com.unimarket.du_lieu.repository.NguoiDungRepository
import com.unimarket.man_hinh.chi_tiet_san_pham.ChiTietSanPhamActivity
import com.unimarket.mo_hinh.NguoiDung
import com.unimarket.tien_ich.CanhLe
import com.unimarket.tien_ich.DinhDang
import com.unimarket.tien_ich.HangSo
import com.unimarket.tien_ich.LuuAnh
import com.unimarket.xu_ly_nghiep_vu.QuyTacLichHen
import com.unimarket.xu_ly_nghiep_vu.QuyTacLichHen.Nut
import kotlinx.coroutines.launch

/**
 * Phòng chat gắn lịch hẹn (SCR-22, UC-31). Mỗi cặp tin–người mua có đúng một phòng (BR-40).
 * Mọi thao tác đặt mua, xác nhận, đổi, huỷ, đã bán, đánh giá đều làm ngay trên THANH GHIM;
 * mỗi thao tác thêm một thẻ sự kiện vào cuộc trò chuyện và gửi thông báo cho bên kia.
 */
class PhongChatActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_CUOC = "cuoc_id"

        fun mo(context: Context, cuocId: Long) {
            context.startActivity(Intent(context, PhongChatActivity::class.java).putExtra(EXTRA_CUOC, cuocId))
        }
    }

    private lateinit var binding: ActivityPhongChatBinding
    private lateinit var repo: GiaoDichRepository
    private val adapter = TinNhanAdapter()
    private var cuocId = 0L
    private var toi: NguoiDung? = null
    private var phong: PhongChat? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhongChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        CanhLe.chuaVienHeThong(binding.root)
        repo = GiaoDichRepository(this)
        cuocId = intent.getLongExtra(EXTRA_CUOC, -1)

        binding.thanhTieuDe.setNavigationOnClickListener { finish() }
        binding.dsTinNhan.layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }
        binding.dsTinNhan.adapter = adapter
        binding.thanhTin.setOnClickListener { phong?.let { ChiTietSanPhamActivity.mo(this, it.tin.id) } }
        binding.btnGui.setOnClickListener { gui(binding.nhapTinNhan.text.toString()) }
    }

    override fun onResume() {
        super.onResume()
        taiDuLieu()
    }

    private fun taiDuLieu() {
        lifecycleScope.launch {
            val nd = NguoiDungRepository(this@PhongChatActivity).layNguoiDangNhap()
            val p = nd?.let { repo.layPhong(cuocId, it.id) }
            if (nd == null || p == null) {
                Toast.makeText(this@PhongChatActivity, "Không mở được cuộc trò chuyện", Toast.LENGTH_SHORT).show()
                finish()
                return@launch
            }
            repo.danhDauDaDoc(cuocId, nd.id)
            toi = nd
            phong = p
            ve(p, nd)
        }
    }

    private fun ve(p: PhongChat, nd: NguoiDung) {
        val doiPhuong = p.doiPhuong
        binding.thanhTieuDe.title = doiPhuong.hoTen
        binding.thanhTieuDe.subtitle = (if (p.laNguoiMua) "Người bán" else "Người mua") +
            (if (doiPhuong.maTruong.isNotBlank()) " · ${doiPhuong.maTruong}" else "")

        // Thanh tin đăng
        val t = p.tin
        val cha = HangSo.timChaCua(t.danhMucCon)
        binding.khungAnh.setBackgroundColor(ContextCompat.getColor(this, cha.mauNen))
        val coAnh = LuuAnh.hienAnh(binding.anhTin, t.anh.split(";").firstOrNull { it.isNotBlank() }, 120)
        binding.anhTin.visibility = if (coAnh) View.VISIBLE else View.GONE
        binding.iconTin.visibility = if (coAnh) View.GONE else View.VISIBLE
        binding.iconTin.setImageResource(HangSo.timDanhMucCon(t.danhMucCon)?.icon ?: cha.icon)
        binding.iconTin.imageTintList = ContextCompat.getColorStateList(this, cha.mauIcon)
        binding.tvTieuDeTin.text = t.tieuDe
        binding.tvGiaTin.text = DinhDang.gia(t.gia)

        veThanhGhim(p)
        adapter.capNhat(p.dsTinNhan, nd.id, doiPhuong.hoTen)
        if (p.dsTinNhan.isNotEmpty()) binding.dsTinNhan.scrollToPosition(p.dsTinNhan.size - 1)
        veTraLoiNhanh(p)
    }

    /** Thanh ghim theo bảng 3.3.1: nút chính tô màu, nút đổi viền, nút huỷ/từ chối chữ đỏ. */
    private fun veThanhGhim(p: PhongChat) {
        val g = QuyTacLichHen.thanhGhim(p.lich, p.tin, p.laNguoiMua, p.doiPhuong.hoTen, System.currentTimeMillis())
        val mau = MauLichHen.cua(g.kieu)
        binding.thanhGhim.setBackgroundColor(ContextCompat.getColor(this, mau.nen))
        binding.iconGhim.setImageResource(mau.icon)
        binding.iconGhim.imageTintList = ContextCompat.getColorStateList(this, mau.chu)
        binding.tvTieuDeGhim.text = g.tieuDe
        binding.tvMoTaGhim.text = g.moTa
        binding.tvMoTaGhim.visibility = if (g.moTa.isBlank()) View.GONE else View.VISIBLE

        val nutChinh = g.dsNut.firstOrNull { it in listOf(Nut.DAT_MUA, Nut.DAT_MUA_LAI, Nut.MOI_DAT_MUA, Nut.XAC_NHAN, Nut.DONG_Y, Nut.DA_BAN, Nut.DANH_GIA) }
        val nutDoi = g.dsNut.firstOrNull { it == Nut.DOI || it == Nut.DE_XUAT_KHAC }
        val nutHuy = g.dsNut.firstOrNull { it in listOf(Nut.TU_CHOI, Nut.HUY_YEU_CAU, Nut.HUY_HEN) }
        datNut(binding.btnGhim1, nutChinh)
        datNut(binding.btnGhim2, nutDoi)
        datNut(binding.btnGhim3, nutHuy)
        binding.btnGhim3.setTextColor(ContextCompat.getColor(this, com.unimarket.R.color.loi))
        binding.khungNutGhim.visibility = if (g.dsNut.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun datNut(nut: MaterialButton, loai: Nut?) {
        nut.visibility = if (loai == null) View.GONE else View.VISIBLE
        if (loai == null) return
        nut.text = loai.chu
        nut.setOnClickListener { xuLyNut(loai) }
    }

    private fun veTraLoiNhanh(p: PhongChat) {
        binding.nhomTraLoiNhanh.removeAllViews()
        for (cau in QuyTacLichHen.cauTraLoiNhanh(p.lich, p.laNguoiMua)) {
            val chip = Chip(this).apply {
                text = cau
                setOnClickListener { gui(cau) }
            }
            binding.nhomTraLoiNhanh.addView(chip)
        }
    }

    private fun gui(noiDung: String) {
        val nd = toi ?: return
        if (noiDung.isBlank()) return
        lifecycleScope.launch {
            repo.guiTinNhan(cuocId, nd.id, noiDung)
            binding.nhapTinNhan.text.clear()
            taiDuLieu()
        }
    }

    // ===================== CÁC NÚT TRÊN THANH GHIM =====================
    private fun xuLyNut(nut: Nut) {
        val p = phong ?: return
        val nd = toi ?: return
        val lich = p.lich
        when (nut) {
            Nut.DAT_MUA, Nut.DAT_MUA_LAI -> {
                val canhBao = if (p.tin.trangThai == HangSo.TIN_DANG_GIAO_DICH)
                    "Người bán đã hẹn với người khác, yêu cầu của bạn sẽ xếp hàng." else null
                BangGiaoDich.moBangHen(this, p.tin, null, canhBao) { kq ->
                    chay("Đã gửi yêu cầu. Chờ người bán xác nhận địa điểm.") {
                        repo.datMua(p.tin.id, nd.id, kq.diaDiem, kq.thoiGian, kq.loiNhan)
                    }
                }
            }
            Nut.MOI_DAT_MUA -> chay("Đã gửi lời mời đặt mua") { repo.moiDatMua(cuocId, nd.id) }
            Nut.XAC_NHAN, Nut.DONG_Y -> {
                if (lich == null) return
                chay("Đã chốt hẹn: ${lich.diaDiem}, ${DinhDang.gioHen(lich.thoiGianHen)}") { repo.xacNhan(lich.id, nd.id) }
            }
            Nut.DOI, Nut.DE_XUAT_KHAC -> {
                if (lich == null) return
                BangGiaoDich.moBangHen(this, p.tin, lich, null) { kq ->
                    chay("Đã gửi đề xuất mới, chờ bên kia đồng ý") { repo.doiHen(lich.id, nd.id, kq.diaDiem, kq.thoiGian) }
                }
            }
            Nut.TU_CHOI -> {
                if (lich == null) return
                BangGiaoDich.hoiLyDo(this, "Từ chối yêu cầu mua", HangSo.LY_DO_TU_CHOI) { lyDo ->
                    chay("Đã từ chối yêu cầu") { repo.huyHen(lich.id, nd.id, lyDo, tuChoi = true) }
                }
            }
            Nut.HUY_YEU_CAU, Nut.HUY_HEN -> {
                if (lich == null) return
                val tieuDe = if (nut == Nut.HUY_HEN) "Huỷ lịch hẹn đã chốt?" else "Huỷ yêu cầu?"
                BangGiaoDich.hoiLyDo(this, tieuDe, HangSo.LY_DO_HUY_HEN) { lyDo ->
                    chay("Đã huỷ lịch hẹn") { repo.huyHen(lich.id, nd.id, lyDo) }
                }
            }
            Nut.DA_BAN -> lifecycleScope.launch {
                val ungVien = repo.layUngVienNguoiMua(p.tin.id)
                BangGiaoDich.moBangDaBan(this@PhongChatActivity, p.tin, ungVien) { gia, nguoiMuaId ->
                    chay("Đã chuyển sang Đã bán với giá ${DinhDang.gia(gia)}") { repo.daBan(p.tin.id, nd.id, gia, nguoiMuaId) }
                }
            }
            Nut.DANH_GIA -> {
                if (lich == null) return
                BangGiaoDich.moBangDanhGia(this, p.nguoiBan.hoTen, p.tin.tieuDe) { soSao, nhanXet ->
                    chay("Cảm ơn bạn đã đánh giá!") { repo.danhGia(lich.id, nd.id, soSao, nhanXet) }
                }
            }
        }
    }

    /** Chạy một thao tác ghi database; thành công thì báo và vẽ lại, lỗi nghiệp vụ thì hiện hộp thoại. */
    private fun chay(thanhCong: String, viec: suspend () -> Unit) {
        lifecycleScope.launch {
            try {
                viec()
                Toast.makeText(this@PhongChatActivity, thanhCong, Toast.LENGTH_SHORT).show()
            } catch (e: IllegalStateException) {
                BangGiaoDich.baoLoi(this@PhongChatActivity, e.message)
            }
            taiDuLieu()
        }
    }
}

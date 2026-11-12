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
import androidx.core.content.IntentCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.unimarket.R
import com.unimarket.databinding.ActivityChiTietSanPhamBinding
import com.unimarket.databinding.BottomSheetBaoCaoBinding
import com.unimarket.databinding.HangMenuBinding
import com.unimarket.databinding.HangThuocTinhBinding
import com.unimarket.du_lieu.repository.DangTinRepository
import com.unimarket.du_lieu.repository.GiaoDichRepository
import com.unimarket.du_lieu.repository.NguoiDungRepository
import com.unimarket.du_lieu.repository.TinDangRepository
import com.unimarket.du_lieu.repository.TinDangRepository.KetQuaBaoCao
import com.unimarket.man_hinh.chat.BangGiaoDich
import com.unimarket.man_hinh.chat.PhongChatActivity
import com.unimarket.man_hinh.dang_tin.TinCuaToiActivity
import com.unimarket.man_hinh.tai_khoan.BoSungHoSo
import com.unimarket.man_hinh.trang_chu.TinDangAdapter
import com.unimarket.mo_hinh.LichHen
import com.unimarket.mo_hinh.NguoiDung
import com.unimarket.mo_hinh.TinDang
import com.unimarket.tien_ich.CanhLe
import com.unimarket.tien_ich.DinhDang
import com.unimarket.tien_ich.HangSo
import com.unimarket.tien_ich.LuuAnh
import com.unimarket.xu_ly_nghiep_vu.GoiYGia
import com.unimarket.xu_ly_nghiep_vu.QuyTacLichHen
import kotlinx.coroutines.launch

/**
 * Màn Chi tiết tin đăng.
 * - Mỗi lần người KHÁC (không phải người bán) mở tin thì lượt xem +1.
 * - Lưu tin, chia sẻ, báo cáo vi phạm, gọi / Zalo cho người bán.
 * - Nút Chat và Đặt mua cần hồ sơ đầy đủ. Đặt mua xong mở thẳng phòng chat;
 *   quay lại thì nút chính đổi thành "Xem trao đổi" (bảng 3.3.2).
 */
class ChiTietSanPhamActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_ID = "tin_dang_id"
        private const val EXTRA_XEM_TRUOC = "ban_nhap"

        fun mo(context: Context, tinDangId: Long) {
            context.startActivity(Intent(context, ChiTietSanPhamActivity::class.java).putExtra(EXTRA_ID, tinDangId))
        }

        /** Mở ở chế độ XEM TRƯỚC một tin chưa lưu (từ màn Đăng tin). Bấm "Đăng tin" sẽ trả về RESULT_OK. */
        fun intentXemTruoc(context: Context, banNhap: TinDang): Intent =
            Intent(context, ChiTietSanPhamActivity::class.java).putExtra(EXTRA_XEM_TRUOC, banNhap)
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
    private var banNhap: TinDang? = null   // khác null = đang ở chế độ xem trước
    private var cuocId: Long? = null       // phòng chat của người xem với tin này (nếu đã có)
    private var lichCuaToi: LichHen? = null

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

        binding.tvTinCuaBan.setOnClickListener { TinCuaToiActivity.mo(this, tin?.trangThai ?: HangSo.TIN_DANG_BAN) }

        banNhap = IntentCompat.getSerializableExtra(intent, EXTRA_XEM_TRUOC, TinDang::class.java)
        if (banNhap != null) {
            // Chế độ xem trước: "Sửa lại" quay về form, "Đăng tin" báo cho màn Đăng tin
            binding.btnChat.text = "Sửa lại"
            binding.btnChat.setIconResource(R.drawable.ic_edit)
            binding.btnChat.setOnClickListener { finish() }
            binding.btnDatMua.text = "Đăng tin"
            binding.btnDatMua.setIconResource(R.drawable.ic_check)
            binding.btnDatMua.setOnClickListener {
                setResult(RESULT_OK)
                finish()
            }
            return
        }

        // Chat và Đặt mua: cần hồ sơ đầy đủ (cổng chặn BR-13)
        binding.btnChat.setOnClickListener { BoSungHoSo.kiemTra(this) { moPhongChat() } }
        binding.btnDatMua.setOnClickListener {
            val phong = cuocId
            if (phong != null && QuyTacLichHen.dangHoatDong(lichCuaToi)) PhongChatActivity.mo(this, phong)
            else BoSungHoSo.kiemTra(this) { datMua() }
        }
    }

    override fun onResume() {
        super.onResume()
        taiDuLieu()
    }

    private fun taiDuLieu() {
        banNhap?.let {
            hienXemTruoc(it)
            return
        }
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
            if (nx != null && nx.id != t.nguoiBanId) {
                val (phong, lich) = GiaoDichRepository(this@ChiTietSanPhamActivity).lichCuaNguoiMua(t.id, nx.id)
                cuocId = phong
                lichCuaToi = lich
            }
            val ttNguoiBan = repo.thongTinNguoiBan(t.nguoiBanId)
            nguoiBan = ttNguoiBan.nguoiDung

            hienTin(t)
            hienNguoiBan(t, ttNguoiBan)
            veNutTim()

            val tuongTu = repo.layTinTuongTu(t)
            binding.tvNhanTuongTu.visibility = if (tuongTu.isEmpty()) View.GONE else View.VISIBLE
            adapterTuongTu.capNhat(tuongTu)

            // Giá thị trường: chạy thuật toán gợi ý giá cho chính món này (không so với chính nó)
            val kq = DangTinRepository(this@ChiTietSanPhamActivity)
                .goiYGia(t.tieuDe, t.danhMucCon, t.tinhTrang, t.thanhPho, t.giaGoc, t.namMua, boQuaId = t.id)
            hienGiaThiTruong(t.gia, if (kq.tuDuLieuThat) kq else null)
        }
    }

    /** Chế độ xem trước: hiện bản nháp như một tin thật, ẩn các nút chỉ dành cho người mua. */
    private fun hienXemTruoc(t: TinDang) {
        lifecycleScope.launch {
            val nx = NguoiDungRepository(this@ChiTietSanPhamActivity).layNguoiDangNhap()
            nguoiXem = nx
            tin = t
            hienTin(t)
            hienNguoiBan(t, repo.thongTinNguoiBan(t.nguoiBanId))
            binding.btnTim.visibility = View.GONE
            binding.btnChiaSe.visibility = View.GONE
            binding.btnBaoCao.visibility = View.GONE
            binding.btnGoi.visibility = View.GONE
            binding.btnZalo.visibility = View.GONE
            binding.tvTinCuaBan.visibility = View.GONE
            binding.btnChat.visibility = View.VISIBLE
            binding.btnDatMua.visibility = View.VISIBLE
            binding.btnChat.isEnabled = true
            binding.btnDatMua.isEnabled = true
            binding.tvNhanTuongTu.visibility = View.GONE
            binding.tvDaiThongBao.visibility = View.VISIBLE
            binding.tvDaiThongBao.text = "Xem trước · tin chưa được đăng"
            binding.tvThongTinPhu.text = "Vừa xong · 0 lượt xem\n${HangSo.timTruong(t.maTruong)?.ten}, ${t.thanhPho}"
            // Khoảng giá thị trường lấy từ kết quả gợi ý lúc soạn (chỉ khi dựa trên tin thật)
            val tuDuLieu = t.doTinCay in listOf("CAO", "TRUNG_BINH", "THAP")
            hienGiaThiTruong(
                t.gia,
                if (tuDuLieu) GoiYGia.KetQua(
                    giaThap = t.goiYThap ?: 0, giaCao = t.goiYCao ?: 0, soMau = t.soMau ?: 0,
                    doTinCay = GoiYGia.DoTinCay.valueOf(t.doTinCay!!)
                ) else null
            )
        }
    }

    /** Khung "Giá thị trường": khoảng giá của các tin tương tự và nhận xét giá đang rao. */
    private fun hienGiaThiTruong(giaRao: Long, kq: GoiYGia.KetQua?) {
        if (kq == null || kq.giaCao <= 0) {
            binding.khungGiaThiTruong.visibility = View.GONE
            return
        }
        binding.khungGiaThiTruong.visibility = View.VISIBLE
        binding.tvGiaThiTruong.text = "Giá thị trường: ${DinhDang.gia(kq.giaThap)} – ${DinhDang.gia(kq.giaCao)}"
        val nhanXet = when {
            giaRao == 0L -> "tin tặng miễn phí"
            giaRao < kq.giaThap -> "giá này rẻ hơn thị trường"
            giaRao > kq.giaCao -> "giá này cao hơn thị trường"
            else -> "giá này nằm trong khoảng hợp lý"
        }
        binding.tvNguonGiaThiTruong.text = "Từ ${kq.soMau} tin tương tự trong ${GoiYGia.SO_NGAY_LAY_MAU} ngày · $nhanXet"
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
        // Đã có lịch hẹn đang hoạt động → nút chính thành "Xem trao đổi"; tin hết bán vẫn xem lại được phòng chat cũ
        val dangHen = QuyTacLichHen.dangHoatDong(lichCuaToi)
        if (banNhap != null) return // chế độ xem trước giữ nhãn "Sửa lại" / "Đăng tin"
        binding.btnDatMua.text = if (dangHen) "Xem trao đổi" else "Đặt mua"
        binding.btnDatMua.setIconResource(if (dangHen) R.drawable.ic_chat else R.drawable.ic_cal)
        binding.btnChat.isEnabled = conBan || cuocId != null
        binding.btnDatMua.isEnabled = conBan || dangHen
    }

    // ===================== CHAT / ĐẶT MUA =====================
    private fun moPhongChat() {
        val t = tin ?: return
        lifecycleScope.launch {
            val nd = NguoiDungRepository(this@ChiTietSanPhamActivity).layNguoiDangNhap() ?: return@launch
            try {
                PhongChatActivity.mo(this@ChiTietSanPhamActivity, GiaoDichRepository(this@ChiTietSanPhamActivity).moPhong(t.id, nd.id))
            } catch (e: IllegalStateException) {
                BangGiaoDich.baoLoi(this@ChiTietSanPhamActivity, e.message)
            }
        }
    }

    /** Đặt mua (UC-34): chọn địa điểm + giờ → tạo lịch hẹn → mở thẳng phòng chat. */
    private fun datMua() {
        val t = tin ?: return
        val canhBao = if (t.trangThai == HangSo.TIN_DANG_GIAO_DICH)
            "Đã có người hẹn mua trước, yêu cầu của bạn sẽ xếp hàng." else null
        BangGiaoDich.moBangHen(this, t, null, canhBao) { kq ->
            lifecycleScope.launch {
                val nd = NguoiDungRepository(this@ChiTietSanPhamActivity).layNguoiDangNhap() ?: return@launch
                try {
                    val phong = GiaoDichRepository(this@ChiTietSanPhamActivity)
                        .datMua(t.id, nd.id, kq.diaDiem, kq.thoiGian, kq.loiNhan)
                    thongBao("Đã gửi yêu cầu. Chờ người bán xác nhận địa điểm.")
                    PhongChatActivity.mo(this@ChiTietSanPhamActivity, phong)
                } catch (e: IllegalStateException) {
                    BangGiaoDich.baoLoi(this@ChiTietSanPhamActivity, e.message)
                }
            }
        }
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

package com.unimarket.man_hinh.dang_tin

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.unimarket.R
import com.unimarket.databinding.ActivityDangTinBinding
import com.unimarket.databinding.BottomSheetXacNhanDiaDiemBinding
import com.unimarket.databinding.ChipLocBinding
import com.unimarket.databinding.HangMenuBinding
import com.unimarket.databinding.OAnhBinding
import com.unimarket.du_lieu.repository.DangTinRepository
import com.unimarket.du_lieu.repository.NguoiDungRepository
import com.unimarket.man_hinh.chi_tiet_san_pham.ChiTietSanPhamActivity
import com.unimarket.mo_hinh.NguoiDung
import com.unimarket.mo_hinh.TinDang
import com.unimarket.tien_ich.CanhLe
import com.unimarket.tien_ich.DinhDang
import com.unimarket.tien_ich.HangSo
import com.unimarket.tien_ich.LuuAnh
import com.unimarket.xu_ly_nghiep_vu.GoiYGia
import com.unimarket.xu_ly_nghiep_vu.KiemTraTinDang
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

/**
 * Màn Đăng tin 3 bước (cũng dùng để SỬA tin):
 *   Bước 1 – Ảnh (1–6 ảnh, ảnh đầu là ảnh bìa)
 *   Bước 2 – Danh mục cha › danh mục con
 *   Bước 3 – Tiêu đề, tình trạng, thông tin riêng, GỢI Ý GIÁ, giá bán, mô tả, nơi giao dịch
 * → Xem trước (màn Chi tiết ở chế độ xem trước) → Xác nhận địa điểm → lưu vào bảng tin_dang.
 */
class DangTinActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_SUA_ID = "sua_tin_id"

        fun moMoi(context: Context) = context.startActivity(Intent(context, DangTinActivity::class.java))

        fun moSua(context: Context, tinId: Long) =
            context.startActivity(Intent(context, DangTinActivity::class.java).putExtra(EXTRA_SUA_ID, tinId))
    }

    private lateinit var binding: ActivityDangTinBinding
    private lateinit var repo: DangTinRepository
    private var nguoiDung: NguoiDung? = null
    private var tinSua: TinDang? = null            // khác null = đang sửa tin cũ

    // Trạng thái bản nháp
    private val dsAnh = mutableListOf<String>()
    private var maCon: String? = null
    private var chaDangMo: String? = null
    private var tinhTrang = "TOT"
    private var maTruong = ""
    private var diem = HangSo.DIEM_GIAO_DICH.first()
    private var buoc = 1
    private var ketQuaGoiY = GoiYGia.KetQua()
    private var viecGoiY: Job? = null

    // Chọn nhiều ảnh một lúc (tối đa 6), không cần xin quyền bộ nhớ
    private val chonAnh = registerForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(KiemTraTinDang.SO_ANH_TOI_DA)
    ) { dsUri ->
        if (dsUri.isEmpty()) return@registerForActivityResult
        lifecycleScope.launch {
            val conCho = KiemTraTinDang.SO_ANH_TOI_DA - dsAnh.size
            dsUri.take(conCho).forEachIndexed { i, uri ->
                try {
                    dsAnh += withContext(Dispatchers.IO) {
                        LuuAnh.chepVaoApp(this@DangTinActivity, uri, "tin_${System.currentTimeMillis()}_$i.jpg")
                    }
                } catch (e: Exception) {
                    thongBao("Không đọc được một ảnh")
                }
            }
            if (dsUri.size > conCho) thongBao("Chỉ thêm được tối đa ${KiemTraTinDang.SO_ANH_TOI_DA} ảnh")
            veLuoiAnh()
        }
    }

    // Màn Xem trước trả về OK khi người dùng bấm "Đăng tin"
    private val xemTruoc = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) moXacNhanDiaDiem()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDangTinBinding.inflate(layoutInflater)
        setContentView(binding.root)
        CanhLe.chuaVienHeThong(binding.root)
        repo = DangTinRepository(this)

        binding.thanhTieuDe.setNavigationOnClickListener { hoiThoat() }
        onBackPressedDispatcher.addCallback(this) { if (buoc > 1) hienBuoc(buoc - 1) else hoiThoat() }
        binding.btnTiep.setOnClickListener { tiep() }
        binding.btnDoiDanhMuc.setOnClickListener { hienBuoc(2) }
        binding.btnGiaiThich.setOnClickListener { giaiThichTinhTrang() }
        caiOBuoc3()

        lifecycleScope.launch {
            val nd = NguoiDungRepository(this@DangTinActivity).layNguoiDangNhap()
            if (nd == null) {
                finish()
                return@launch
            }
            nguoiDung = nd
            val idSua = intent.getLongExtra(EXTRA_SUA_ID, 0)
            val tin = if (idSua > 0) repo.layTheoId(idSua) else null
            if (tin != null && tin.nguoiBanId == nd.id) {
                tinSua = tin
                binding.thanhTieuDe.title = "Sửa tin"
                dienTinSua(tin)
                hienBuoc(3)
            } else {
                // Mặc định nơi giao dịch = trường và điểm quen thuộc trong hồ sơ
                chonTruong(nd.maTruong)
                diem = if (nd.diemGiaoDich in HangSo.DIEM_GIAO_DICH) nd.diemGiaoDich else HangSo.DIEM_GIAO_DICH.first()
                if (nd.diemGiaoDich == HangSo.DIEM_KHAC) binding.nhapGhiChu.setText(nd.ghiChuDiem)
                taoChip(binding.nhomDiem, HangSo.DIEM_GIAO_DICH.map { it to it }, diem) { diem = it }
                hienBuoc(1)
            }
        }
    }

    // ===================== CHUYỂN BƯỚC =====================
    private fun hienBuoc(n: Int) {
        buoc = n
        binding.buoc1.visibility = if (n == 1) View.VISIBLE else View.GONE
        binding.buoc2.visibility = if (n == 2) View.VISIBLE else View.GONE
        binding.buoc3.visibility = if (n == 3) View.VISIBLE else View.GONE
        binding.tvBuoc.text = "Bước $n/3"
        binding.tvTenBuoc.text = listOf("Ảnh", "Danh mục", "Thông tin & giá")[n - 1]
        binding.thanhTienDo.setProgressCompat(n, true)
        binding.btnTiep.text = if (n < 3) "Tiếp" else "Xem trước"
        when (n) {
            1 -> veLuoiAnh()
            2 -> veDanhMuc()
            3 -> veBuoc3()
        }
    }

    private fun tiep() {
        when (buoc) {
            1 -> if (dsAnh.isEmpty() && tinSua == null) binding.tvLoiAnh.visibility = View.VISIBLE else hienBuoc(2)
            2 -> if (maCon == null) thongBao("Chọn danh mục cho món đồ") else hienBuoc(3)
            3 -> if (kiemTraBuoc3()) moXemTruoc()
        }
    }

    private fun hoiThoat() {
        val dangSoan = dsAnh.isNotEmpty() || binding.nhapTieuDe.text?.isNotBlank() == true
        if (!dangSoan) {
            finish()
            return
        }
        AlertDialog.Builder(this)
            .setTitle(if (tinSua == null) "Bỏ tin đang soạn?" else "Bỏ các thay đổi?")
            .setMessage("Những gì bạn đã nhập sẽ không được lưu.")
            .setPositiveButton("Bỏ") { _, _ -> finish() }
            .setNegativeButton("Tiếp tục soạn", null)
            .show()
    }

    // ===================== BƯỚC 1: ẢNH =====================
    private fun veLuoiAnh() {
        binding.luoiAnh.removeAllViews()
        binding.tvSoAnh.text = "Thêm ảnh sản phẩm (${dsAnh.size}/${KiemTraTinDang.SO_ANH_TOI_DA})"
        dsAnh.forEachIndexed { i, duongDan ->
            val o = OAnhBinding.inflate(layoutInflater, binding.luoiAnh, false)
            LuuAnh.hienAnh(o.anh, duongDan, 300)
            o.tvAnhBia.visibility = if (i == 0) View.VISIBLE else View.GONE
            o.btnXoaAnh.setOnClickListener {
                dsAnh.removeAt(i)
                veLuoiAnh()
            }
            binding.luoiAnh.addView(o.root)
        }
        if (dsAnh.size < KiemTraTinDang.SO_ANH_TOI_DA) {
            val o = OAnhBinding.inflate(layoutInflater, binding.luoiAnh, false)
            o.tvThemAnh.visibility = View.VISIBLE
            o.btnXoaAnh.visibility = View.GONE
            o.root.setOnClickListener {
                chonAnh.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
            binding.luoiAnh.addView(o.root)
        }
        binding.tvLoiAnh.visibility = if (dsAnh.isEmpty() && tinSua == null) View.VISIBLE else View.GONE
    }

    // ===================== BƯỚC 2: DANH MỤC =====================
    private fun veDanhMuc() {
        binding.dsDanhMuc.removeAllViews()
        if (chaDangMo == null) chaDangMo = maCon?.let { HangSo.timChaCua(it).ma }
        for (dm in HangSo.DS_DANH_MUC) {
            val khoi = android.widget.LinearLayout(this).apply {
                orientation = android.widget.LinearLayout.VERTICAL
                setBackgroundResource(R.drawable.bg_the)
                layoutParams = android.widget.LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = 24 }
            }
            binding.dsDanhMuc.addView(khoi)
            val moRa = chaDangMo == dm.ma
            val dau = HangMenuBinding.inflate(layoutInflater, khoi, true)
            dau.iconMenu.setImageResource(dm.icon)
            dau.iconMenu.imageTintList = ContextCompat.getColorStateList(this, dm.mauIcon)
            dau.tvTieuDe.text = dm.ten
            dau.tvTieuDe.setTypeface(null, Typeface.BOLD)
            dau.tvPhu.visibility = View.GONE
            dau.iconMuiTen.setImageResource(if (moRa) R.drawable.ic_down else R.drawable.ic_chev)
            dau.root.setOnClickListener {
                chaDangMo = if (moRa) null else dm.ma
                veDanhMuc()
            }
            if (!moRa) continue
            for (con in dm.dsCon) {
                val hang = HangMenuBinding.inflate(layoutInflater, khoi, true)
                hang.iconMenu.setImageResource(con.icon)
                hang.iconMenu.imageTintList = ContextCompat.getColorStateList(this, R.color.chu_phu)
                hang.tvTieuDe.text = con.ten
                hang.tvPhu.visibility = View.GONE
                val dangChon = maCon == con.ma
                hang.iconMuiTen.setImageResource(R.drawable.ic_check)
                hang.iconMuiTen.imageTintList = ContextCompat.getColorStateList(this, R.color.chinh)
                hang.iconMuiTen.visibility = if (dangChon) View.VISIBLE else View.INVISIBLE
                hang.root.setOnClickListener {
                    maCon = con.ma
                    hienBuoc(3)
                }
            }
        }
    }

    // ===================== BƯỚC 3: THÔNG TIN & GIÁ =====================
    private fun caiOBuoc3() {
        taoChip(binding.nhomTinhTrang, HangSo.DS_TINH_TRANG.map { it.ma to it.ten }, tinhTrang) {
            tinhTrang = it
            capNhatGoiY()
        }
        val dsTenTruong = HangSo.DS_TRUONG.map { "${it.ten} (${it.ma})" }
        binding.chonTruong.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, dsTenTruong))
        binding.chonTruong.setOnItemClickListener { _, _, viTri, _ ->
            maTruong = HangSo.DS_TRUONG[viTri].ma
            capNhatGoiY()
        }

        // Thay đổi các ô ảnh hưởng tới giá → tính lại gợi ý
        binding.nhapTieuDe.doAfterTextChanged { binding.oTieuDe.error = null; capNhatGoiY() }
        binding.nhapGiaBia.doAfterTextChanged { capNhatGoiY() }
        binding.nhapGiaGoc.doAfterTextChanged { capNhatGoiY() }
        binding.nhapNamMua.doAfterTextChanged { binding.oNamMua.error = null; capNhatGoiY() }
        binding.nhapMonHoc.doAfterTextChanged { binding.oMonHoc.error = null }
        binding.nhapMoTa.doAfterTextChanged { binding.oMoTa.error = null }
        binding.nhapGia.doAfterTextChanged { binding.oGia.error = null; capNhatCanhBao() }
        binding.cbMienPhi.setOnCheckedChangeListener { _, mienPhi ->
            binding.nhapGia.isEnabled = !mienPhi
            if (mienPhi) binding.nhapGia.setText("0")
            binding.oGia.error = null
            capNhatCanhBao()
        }
        binding.goiY.btnDungGia.setOnClickListener {
            binding.cbMienPhi.isChecked = false
            binding.nhapGia.setText(ketQuaGoiY.giaGoiY.toString())
            thongBao("Đã điền giá ${DinhDang.gia(ketQuaGoiY.giaGoiY)}")
        }
    }

    private fun laSach() = maCon?.let { HangSo.timChaCua(it).ma == HangSo.SACH } ?: false

    private fun veBuoc3() {
        val con = HangSo.timDanhMucCon(maCon)
        binding.tvDanhMucDaChon.text = if (con == null) "Chưa chọn danh mục" else "${HangSo.timChaCua(maCon).ten} › ${con.ten}"
        binding.khungSach.visibility = if (laSach()) View.VISIBLE else View.GONE
        binding.khungDoKhac.visibility = if (laSach()) View.GONE else View.VISIBLE
        binding.oMonHoc.hint = if (maCon == "GIAO_TRINH") "Môn học *" else "Môn học / chủ đề"
        capNhatGoiY()
    }

    private fun so(chu: CharSequence?): Long? = chu?.toString()?.filter { it.isDigit() }?.toLongOrNull()
    private fun giaGoc(): Long? = if (laSach()) so(binding.nhapGiaBia.text) else so(binding.nhapGiaGoc.text)
    private fun namMua(): Int? = if (laSach()) null else so(binding.nhapNamMua.text)?.toInt()

    /** Tính lại gợi ý giá sau khi người dùng ngừng gõ 0,3 giây (tránh tính liên tục từng ký tự). */
    private fun capNhatGoiY() {
        val con = maCon ?: return
        viecGoiY?.cancel()
        viecGoiY = lifecycleScope.launch {
            delay(300)
            ketQuaGoiY = repo.goiYGia(
                tieuDe = binding.nhapTieuDe.text.toString(),
                danhMucCon = con,
                tinhTrang = tinhTrang,
                thanhPho = HangSo.timTruong(maTruong)?.thanhPho ?: "",
                giaGoc = giaGoc(),
                namMua = namMua(),
                boQuaId = tinSua?.id ?: 0
            )
            veGoiY(ketQuaGoiY)
            capNhatCanhBao()
        }
    }

    private fun veGoiY(kq: GoiYGia.KetQua) {
        val g = binding.goiY
        val co = kq.coGoiY
        g.tvDoTinCay.visibility = if (co) View.VISIBLE else View.GONE
        g.tvGiaGoiY.visibility = if (co) View.VISIBLE else View.GONE
        g.tvKhoangGia.visibility = if (co) View.VISIBLE else View.GONE
        g.btnDungGia.visibility = if (co) View.VISIBLE else View.GONE
        g.tvNguon.text = kq.giaiThich
        if (!co) return
        g.tvDoTinCay.text = kq.doTinCay.ten
        val mau = when (kq.doTinCay) {
            GoiYGia.DoTinCay.CAO -> R.color.tt_nhu_moi
            GoiYGia.DoTinCay.TRUNG_BINH -> R.color.tt_kha
            GoiYGia.DoTinCay.THAP -> R.color.gia
            else -> R.color.thong_tin
        }
        g.tvDoTinCay.backgroundTintList = ContextCompat.getColorStateList(this, mau)
        g.tvGiaGoiY.text = DinhDang.gia(kq.giaGoiY)
        g.tvKhoangGia.text = "Khoảng hợp lý: ${DinhDang.gia(kq.giaThap)} – ${DinhDang.gia(kq.giaCao)}"
    }

    private fun capNhatCanhBao() {
        val canhBao = if (binding.cbMienPhi.isChecked) null else GoiYGia.canhBaoLech(so(binding.nhapGia.text) ?: 0, ketQuaGoiY)
        binding.tvCanhBaoGia.visibility = if (canhBao == null) View.GONE else View.VISIBLE
        binding.tvCanhBaoGia.text = canhBao
    }

    /** Kiểm tra toàn bộ bước 3; ô nào sai thì hiện lỗi đỏ ngay dưới ô đó. */
    private fun kiemTraBuoc3(): Boolean {
        val tieuDe = binding.nhapTieuDe.text.toString()
        val moTa = binding.nhapMoTa.text.toString()
        binding.oTieuDe.error = KiemTraTinDang.loiTieuDe(tieuDe)
            ?: KiemTraTinDang.tuKhoaCam(tieuDe, moTa)?.let { "Có nội dung không được phép: “$it”" }
        binding.oMoTa.error = KiemTraTinDang.loiMoTa(moTa)
        binding.oGia.error = KiemTraTinDang.loiGia(so(binding.nhapGia.text), binding.cbMienPhi.isChecked)
        binding.oMonHoc.error = if (laSach()) KiemTraTinDang.loiMonHoc(maCon ?: "", binding.nhapMonHoc.text.toString()) else null
        val nam = namMua()
        val namNay = Calendar.getInstance().get(Calendar.YEAR)
        binding.oNamMua.error = if (nam != null && (nam < 1990 || nam > namNay)) "Năm không hợp lệ" else null
        val loiTruong = HangSo.timTruong(maTruong) == null
        if (loiTruong) thongBao("Chọn trường nơi giao dịch")
        val hopLe = listOf(binding.oTieuDe, binding.oMoTa, binding.oGia, binding.oMonHoc, binding.oNamMua).all { it.error == null } && !loiTruong
        if (!hopLe) thongBao("Vui lòng sửa các ô được đánh dấu đỏ")
        return hopLe
    }

    /** Gộp dữ liệu trên form thành một TinDang (chưa lưu). */
    private fun taoBanNhap(): TinDang {
        val nd = nguoiDung!!
        val con = maCon!!
        val truong = HangSo.timTruong(maTruong)!!
        val thuocTinh = mutableListOf<String>()
        if (laSach()) {
            binding.nhapMonHoc.text.toString().trim().takeIf { it.isNotEmpty() }?.let { thuocTinh += "Môn học: $it" }
            giaGoc()?.let { thuocTinh += "Giá bìa: ${DinhDang.gia(it)}" }
        } else {
            binding.nhapHang.text.toString().trim().takeIf { it.isNotEmpty() }?.let { thuocTinh += "Hãng: $it" }
            namMua()?.let { thuocTinh += "Năm mua: $it" }
            giaGoc()?.let { thuocTinh += "Giá mua gốc: ${DinhDang.gia(it)}" }
        }
        val goc = tinSua ?: TinDang(
            nguoiBanId = nd.id, tieuDe = "", danhMucCha = "", danhMucCon = "", gia = 0,
            tinhTrang = "", maTruong = "", thanhPho = "", diemGiaoDich = ""
        )
        val kq = ketQuaGoiY
        return goc.copy(
            tieuDe = binding.nhapTieuDe.text.toString().trim(),
            moTa = binding.nhapMoTa.text.toString().trim(),
            danhMucCha = HangSo.timChaCua(con).ma,
            danhMucCon = con,
            gia = if (binding.cbMienPhi.isChecked) 0 else so(binding.nhapGia.text) ?: 0,
            giaGoc = giaGoc(),
            namMua = namMua(),
            tinhTrang = tinhTrang,
            thuocTinh = thuocTinh.joinToString("\n"),
            anh = dsAnh.joinToString(";"),
            maTruong = truong.ma,
            thanhPho = truong.thanhPho,
            diemGiaoDich = diem,
            ghiChuDiaDiem = binding.nhapGhiChu.text.toString().trim(),
            coGiaoHang = binding.cbGiaoHang.isChecked,
            // Lưu kết quả gợi ý tại thời điểm đăng để sau này đo độ chính xác (so với giá bán thật)
            giaGoiY = if (kq.coGoiY) kq.giaGoiY else null,
            goiYThap = if (kq.coGoiY) kq.giaThap else null,
            goiYCao = if (kq.coGoiY) kq.giaCao else null,
            doTinCay = if (kq.coGoiY) kq.doTinCay.name else null,
            soMau = if (kq.tuDuLieuThat) kq.soMau else null
        )
    }

    private fun moXemTruoc() {
        xemTruoc.launch(ChiTietSanPhamActivity.intentXemTruoc(this, taoBanNhap()))
    }

    // ===================== XÁC NHẬN ĐỊA ĐIỂM & LƯU =====================
    private fun moXacNhanDiaDiem() {
        val banNhap = taoBanNhap()
        val truong = HangSo.timTruong(banNhap.maTruong)
        val bang = BottomSheetDialog(this)
        val b = BottomSheetXacNhanDiaDiemBinding.inflate(layoutInflater)
        bang.setContentView(b.root)
        b.tvDiem.text = banNhap.diemGiaoDich
        b.tvTruong.text = "${truong?.ten}, ${truong?.thanhPho}"
        b.tvGhiChu.text = "Ghi chú: ${banNhap.ghiChuDiaDiem}"
        b.tvGhiChu.visibility = if (banNhap.ghiChuDiaDiem.isBlank()) View.GONE else View.VISIBLE
        b.btnXacNhan.text = if (tinSua == null) "Xác nhận & đăng tin" else "Xác nhận & lưu thay đổi"
        b.cbXacNhan.setOnCheckedChangeListener { _, daTich -> b.btnXacNhan.isEnabled = daTich }
        b.btnDoiDiaDiem.setOnClickListener {
            bang.dismiss()
            hienBuoc(3)
            binding.buoc3.post { binding.buoc3.smoothScrollTo(0, binding.tvNhanNoiGiaoDich.top) }
        }
        b.btnXacNhan.setOnClickListener {
            b.btnXacNhan.isEnabled = false
            lifecycleScope.launch {
                try {
                    if (tinSua == null) {
                        repo.dangTin(banNhap)
                        thongBao("Đã đăng tin")
                    } else {
                        repo.capNhatTin(banNhap)
                        thongBao("Đã lưu thay đổi")
                    }
                    bang.dismiss()
                    TinCuaToiActivity.mo(this@DangTinActivity, HangSo.TIN_DANG_BAN)
                    finish()
                } catch (e: IllegalStateException) {
                    bang.dismiss()
                    AlertDialog.Builder(this@DangTinActivity)
                        .setTitle("Chưa đăng được tin")
                        .setMessage(e.message)
                        .setPositiveButton("Đã hiểu", null)
                        .show()
                }
            }
        }
        bang.show()
    }

    // ===================== SỬA TIN: ĐIỀN LẠI FORM =====================
    private fun dienTinSua(t: TinDang) {
        maCon = t.danhMucCon
        tinhTrang = t.tinhTrang
        dsAnh.clear()
        dsAnh += t.anh.split(";").filter { it.isNotBlank() }
        taoChip(binding.nhomTinhTrang, HangSo.DS_TINH_TRANG.map { it.ma to it.ten }, tinhTrang) {
            tinhTrang = it
            capNhatGoiY()
        }
        diem = if (t.diemGiaoDich in HangSo.DIEM_GIAO_DICH) t.diemGiaoDich else HangSo.DIEM_GIAO_DICH.first()
        taoChip(binding.nhomDiem, HangSo.DIEM_GIAO_DICH.map { it to it }, diem) { diem = it }
        chonTruong(t.maTruong)

        // Thuộc tính lưu dạng "Tên: giá trị" → lấy lại Môn học / Hãng
        val thuocTinh = t.thuocTinh.lines().associate { it.substringBefore(":").trim() to it.substringAfter(":").trim() }
        binding.nhapTieuDe.setText(t.tieuDe)
        binding.nhapMoTa.setText(t.moTa)
        binding.nhapMonHoc.setText(thuocTinh["Môn học"] ?: "")
        binding.nhapHang.setText(thuocTinh["Hãng"] ?: "")
        val laSachTin = HangSo.timChaCua(t.danhMucCon).ma == HangSo.SACH
        t.giaGoc?.let { (if (laSachTin) binding.nhapGiaBia else binding.nhapGiaGoc).setText(it.toString()) }
        t.namMua?.let { binding.nhapNamMua.setText(it.toString()) }
        if (t.gia == 0L) binding.cbMienPhi.isChecked = true else binding.nhapGia.setText(t.gia.toString())
        binding.nhapGhiChu.setText(t.ghiChuDiaDiem)
        binding.cbGiaoHang.isChecked = t.coGiaoHang
    }

    // ===================== TIỆN ÍCH =====================
    private fun chonTruong(ma: String) {
        val truong = HangSo.timTruong(ma) ?: return
        maTruong = truong.ma
        binding.chonTruong.setText("${truong.ten} (${truong.ma})", false)
    }

    /** Tạo nhóm chip chọn một (tình trạng, điểm giao dịch). luaChon: danh sách (mã, chữ hiển thị). */
    private fun taoChip(nhom: ChipGroup, luaChon: List<Pair<String, String>>, dangChon: String, khiChon: (String) -> Unit) {
        nhom.setOnCheckedStateChangeListener(null)
        nhom.removeAllViews()
        for ((ma, chu) in luaChon) {
            val chip = ChipLocBinding.inflate(layoutInflater, nhom, false).root
            chip.id = View.generateViewId()
            chip.text = chu
            chip.tag = ma
            nhom.addView(chip)
            if (ma == dangChon) nhom.check(chip.id)
        }
        nhom.setOnCheckedStateChangeListener { g, ids ->
            val chip = ids.firstOrNull()?.let { g.findViewById<Chip>(it) } ?: return@setOnCheckedStateChangeListener
            khiChon(chip.tag as String)
        }
    }

    private fun giaiThichTinhTrang() {
        val noiDung = HangSo.DS_TINH_TRANG.joinToString("\n\n") {
            "• ${it.ten} (hệ số giá ${String.format("%.2f", it.heSo).replace('.', ',')})\n${it.moTa}"
        }
        AlertDialog.Builder(this)
            .setTitle("Chọn tình trạng thế nào?")
            .setMessage(noiDung)
            .setPositiveButton("Đã hiểu", null)
            .show()
    }

    private fun thongBao(chu: String) = Toast.makeText(this, chu, Toast.LENGTH_SHORT).show()
}

package com.unimarket.man_hinh.tai_khoan

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import com.unimarket.tien_ich.CanhLe
import com.unimarket.R
import com.unimarket.databinding.ActivityHoSoBinding
import com.unimarket.databinding.HangLuaChonBinding
import com.unimarket.du_lieu.repository.NguoiDungRepository
import com.unimarket.mo_hinh.NguoiDung
import com.unimarket.tien_ich.DinhDang
import com.unimarket.tien_ich.HangSo
import com.unimarket.tien_ich.LuuAnh
import com.unimarket.xu_ly_nghiep_vu.KiemTraHoSo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Màn Hồ sơ: sửa họ tên, số điện thoại, trường, điểm giao dịch quen thuộc, ảnh đại diện.
 * Nếu mở từ bảng "Bổ sung thông tin để tiếp tục" thì các mục thiếu được tô đỏ,
 * lưu đủ xong sẽ tự quay lại làm tiếp việc đang dở (đăng tin, đặt mua...).
 */
class HoSoActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_LAN_DAU = "lan_dau"            // mở ngay sau khi đăng ký
        const val EXTRA_TU_CONG_CHAN = "tu_cong_chan"  // mở từ bảng nhắc bổ sung hồ sơ
    }

    private lateinit var binding: ActivityHoSoBinding
    private lateinit var repo: NguoiDungRepository
    private var nguoiDung: NguoiDung? = null
    private var maTruongDaChon = ""
    private var anhMoi: String? = null
    private var tuCongChan = false
    private var daHoanTat = false

    // Trình chọn ảnh của Android (không cần xin quyền đọc bộ nhớ)
    private val chonAnh = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        val nd = nguoiDung ?: return@registerForActivityResult
        if (uri == null) return@registerForActivityResult
        lifecycleScope.launch {
            try {
                val duongDan = withContext(Dispatchers.IO) {
                    LuuAnh.chepVaoApp(this@HoSoActivity, uri, "dai_dien_${nd.id}_${System.currentTimeMillis()}.jpg")
                }
                anhMoi = duongDan
                hienAnhDaiDien(duongDan)
            } catch (e: Exception) {
                Toast.makeText(this@HoSoActivity, "Không đọc được ảnh", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHoSoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        CanhLe.chuaVienHeThong(binding.root)
        repo = NguoiDungRepository(this)
        tuCongChan = intent.getBooleanExtra(EXTRA_TU_CONG_CHAN, false)

        binding.tvCanhBao.visibility = if (tuCongChan) View.VISIBLE else View.GONE
        binding.tvGioiThieu.visibility = if (tuCongChan) View.GONE else View.VISIBLE
        binding.thanhTieuDe.setNavigationOnClickListener { finish() }
        binding.btnDeSau.setOnClickListener { finish() }
        binding.btnLuu.setOnClickListener { luu() }
        binding.hangAnh.setOnClickListener {
            chonAnh.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        taoChipDiemGiaoDich()
        binding.nhapTimTruong.doAfterTextChanged { veDanhSachTruong() }
        // Người dùng gõ lại thì xoá dòng báo lỗi đỏ của ô đó
        binding.nhapHoTen.doAfterTextChanged { binding.oHoTen.error = null }
        binding.nhapSoDienThoai.doAfterTextChanged { binding.oSoDienThoai.error = null }
        binding.nhapGhiChuDiem.doAfterTextChanged { binding.oGhiChuDiem.error = null }

        lifecycleScope.launch {
            val nd = repo.layNguoiDangNhap()
            if (nd == null) {
                finish()
                return@launch
            }
            nguoiDung = nd
            dienForm(nd)
            if (tuCongChan) danhDauMucThieu(KiemTraHoSo.mucConThieu(nd))
        }
    }

    /** Đóng màn mà chưa lưu đủ → bỏ luôn việc đang chờ (ví dụ huỷ đăng tin). */
    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing && tuCongChan && !daHoanTat) BoSungHoSo.boQua()
    }

    // ------------------------------------------------------------------
    private fun dienForm(nd: NguoiDung) {
        binding.nhapHoTen.setText(nd.hoTen)
        binding.nhapSoDienThoai.setText(nd.soDienThoai)
        binding.cbZalo.isChecked = nd.zaloCungSo
        chonTruong(HangSo.timTruong(nd.maTruong))
        for (i in 0 until binding.nhomDiem.childCount) {
            val chip = binding.nhomDiem.getChildAt(i) as Chip
            chip.isChecked = chip.text == nd.diemGiaoDich
        }
        binding.nhapGhiChuDiem.setText(nd.ghiChuDiem)
        if (!nd.anhDaiDien.isNullOrBlank()) hienAnhDaiDien(nd.anhDaiDien)
    }

    private fun taoChipDiemGiaoDich() {
        for (diem in HangSo.DIEM_GIAO_DICH + HangSo.DIEM_KHAC) {
            val chip = Chip(this).apply {
                id = View.generateViewId()
                text = diem
                isCheckable = true
            }
            binding.nhomDiem.addView(chip)
        }
        binding.nhomDiem.setOnCheckedStateChangeListener { _, _ ->
            val khac = diemDangChon() == HangSo.DIEM_KHAC
            binding.oGhiChuDiem.visibility = if (khac) View.VISIBLE else View.GONE
            binding.tvNhanDiem.setTextColor(ContextCompat.getColor(this, R.color.chu))
            binding.tvNhanDiem.text = "Điểm giao dịch quen thuộc *"
        }
    }

    private fun diemDangChon(): String {
        val chip = binding.nhomDiem.findViewById<Chip>(binding.nhomDiem.checkedChipId)
        return chip?.text?.toString() ?: ""
    }

    /** Vẽ lại danh sách trường theo chữ đang gõ (tìm không dấu: "bach khoa" vẫn ra "Bách khoa"). */
    private fun veDanhSachTruong() {
        val tuKhoa = DinhDang.boDau(binding.nhapTimTruong.text.toString().trim())
        val ketQua = HangSo.DS_TRUONG.filter { DinhDang.boDau(it.ten + " " + it.ma).contains(tuKhoa) }
        binding.dsTruong.removeAllViews()
        if (ketQua.isEmpty()) {
            val hang = HangLuaChonBinding.inflate(layoutInflater, binding.dsTruong, true)
            hang.tvTieuDe.text = "Không tìm thấy trường"
            hang.tvPhu.text = "Thử gõ tên viết tắt, ví dụ NEU, HUST, FTU"
            hang.iconChon.visibility = View.GONE
            return
        }
        for (truong in ketQua) {
            val hang = HangLuaChonBinding.inflate(layoutInflater, binding.dsTruong, true)
            hang.tvTieuDe.text = truong.ten
            hang.tvPhu.text = "${truong.ma} · ${truong.thanhPho}"
            hang.iconChon.visibility = if (truong.ma == maTruongDaChon) View.VISIBLE else View.INVISIBLE
            hang.root.setOnClickListener { chonTruong(truong) }
        }
    }

    private fun chonTruong(truong: HangSo.Truong?) {
        maTruongDaChon = truong?.ma ?: ""
        binding.nhapThanhPho.setText(truong?.thanhPho ?: "")
        binding.oTimTruong.error = null
        binding.nhapTimTruong.setText(truong?.ten ?: "") // đổi chữ ô tìm → danh sách tự vẽ lại
    }

    private fun hienAnhDaiDien(duongDan: String) {
        if (LuuAnh.hienAnh(binding.anhDaiDien, duongDan, 300)) {
            binding.anhDaiDien.setContentPadding(0, 0, 0, 0)
        }
    }

    // ------------------------------------------------------------------
    private fun luu() {
        val goc = nguoiDung ?: return
        binding.oHoTen.error = null
        binding.oSoDienThoai.error = null
        binding.oGhiChuDiem.error = null

        val hoTen = binding.nhapHoTen.text.toString().trim()
        val sdt = binding.nhapSoDienThoai.text.toString().replace(" ", "")
        val diem = diemDangChon()
        val ghiChu = binding.nhapGhiChuDiem.text.toString().trim()

        // Kiểm tra những gì người dùng ĐÃ nhập có đúng định dạng không
        if (hoTen.length < 2) {
            binding.oHoTen.error = "Họ và tên cần ít nhất 2 ký tự"
            return
        }
        if (sdt.isNotEmpty() && !KiemTraHoSo.soDienThoaiHopLe(sdt)) {
            binding.oSoDienThoai.error = "Số điện thoại gồm 10 chữ số, bắt đầu bằng 0"
            return
        }
        if (diem == HangSo.DIEM_KHAC && ghiChu.length < 10) {
            binding.oGhiChuDiem.error = "Mô tả nơi gặp tối thiểu 10 ký tự"
            return
        }

        val moi = goc.copy(
            hoTen = hoTen,
            soDienThoai = sdt,
            zaloCungSo = binding.cbZalo.isChecked,
            maTruong = maTruongDaChon,
            thanhPho = HangSo.timTruong(maTruongDaChon)?.thanhPho ?: "",
            diemGiaoDich = diem,
            ghiChuDiem = if (diem == HangSo.DIEM_KHAC) ghiChu else "",
            anhDaiDien = anhMoi ?: goc.anhDaiDien
        )

        lifecycleScope.launch {
            repo.luuHoSo(moi)
            nguoiDung = moi
            val thieu = KiemTraHoSo.mucConThieu(moi)
            when {
                thieu.isEmpty() && tuCongChan -> {
                    // Đủ hồ sơ → đóng màn này và làm tiếp việc đang dở
                    daHoanTat = true
                    Toast.makeText(this@HoSoActivity, "Đã lưu hồ sơ", Toast.LENGTH_SHORT).show()
                    finish()
                    BoSungHoSo.chayViecDangCho()
                }
                thieu.isEmpty() -> {
                    Toast.makeText(this@HoSoActivity, "Đã lưu hồ sơ", Toast.LENGTH_SHORT).show()
                    finish()
                }
                tuCongChan -> {
                    danhDauMucThieu(thieu)
                    Toast.makeText(this@HoSoActivity, "Còn thiếu: ${thieu.joinToString(", ")}", Toast.LENGTH_LONG).show()
                }
                else -> {
                    Toast.makeText(this@HoSoActivity, "Đã lưu. Còn thiếu: ${thieu.joinToString(", ")}", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }
    }

    /** Tô đỏ các mục còn thiếu. */
    private fun danhDauMucThieu(thieu: List<String>) {
        if (KiemTraHoSo.MUC_HO_TEN in thieu) binding.oHoTen.error = "Cần nhập họ và tên"
        if (KiemTraHoSo.MUC_SO_DIEN_THOAI in thieu) binding.oSoDienThoai.error = "Cần số điện thoại 10 số, bắt đầu bằng 0"
        if (KiemTraHoSo.MUC_TRUONG in thieu) binding.oTimTruong.error = "Chọn trường của bạn trong danh sách"
        if (KiemTraHoSo.MUC_DIEM_GIAO_DICH in thieu) {
            binding.tvNhanDiem.setTextColor(ContextCompat.getColor(this, R.color.loi))
            binding.tvNhanDiem.text = "Điểm giao dịch quen thuộc * (chưa chọn)"
        }
    }
}

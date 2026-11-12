package com.unimarket.man_hinh.tim_kiem

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.ChipGroup
import com.unimarket.R
import com.unimarket.databinding.ActivityTimKiemBinding
import com.unimarket.databinding.BottomSheetBoLocBinding
import com.unimarket.databinding.ChipLocBinding
import com.unimarket.du_lieu.repository.NguoiDungRepository
import com.unimarket.du_lieu.repository.TinDangRepository
import com.unimarket.man_hinh.chi_tiet_san_pham.ChiTietSanPhamActivity
import com.unimarket.man_hinh.chi_tiet_san_pham.LuuTin
import com.unimarket.man_hinh.trang_chu.TinDangAdapter
import com.unimarket.mo_hinh.NguoiDung
import com.unimarket.mo_hinh.TinDang
import com.unimarket.tien_ich.CanhLe
import com.unimarket.tien_ich.DinhDang
import com.unimarket.tien_ich.HangSo
import com.unimarket.xu_ly_nghiep_vu.LocTinDang
import com.unimarket.xu_ly_nghiep_vu.LocTinDang.PhamVi
import kotlinx.coroutines.launch

/**
 * Màn Tìm kiếm + kết quả. Gõ đến đâu lọc đến đó (tìm không dấu).
 * Mở từ ô tìm kiếm, từ ô danh mục ở Trang chủ, hoặc từ tab Danh mục (kèm sẵn bộ lọc danh mục).
 */
class TimKiemActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_CHA = "danh_muc_cha"
        private const val EXTRA_CON = "danh_muc_con"
        private const val EXTRA_PHAM_VI = "pham_vi"

        fun mo(context: Context, danhMucCha: String? = null, danhMucCon: String? = null, phamVi: PhamVi = PhamVi.TOAN_QUOC) {
            context.startActivity(
                Intent(context, TimKiemActivity::class.java)
                    .putExtra(EXTRA_CHA, danhMucCha)
                    .putExtra(EXTRA_CON, danhMucCon)
                    .putExtra(EXTRA_PHAM_VI, phamVi.name)
            )
        }
    }

    private lateinit var binding: ActivityTimKiemBinding
    private lateinit var adapter: TinDangAdapter
    private var nguoiDung: NguoiDung? = null
    private var tatCa: List<TinDang> = emptyList()
    private var daLuu: Set<Long> = emptySet()
    private var dk = LocTinDang.DieuKien()
    private var ketQua: List<TinDang> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimKiemBinding.inflate(layoutInflater)
        setContentView(binding.root)
        CanhLe.chuaVienHeThong(binding.root)

        dk = LocTinDang.DieuKien(
            danhMucCha = intent.getStringExtra(EXTRA_CHA),
            danhMucCon = intent.getStringExtra(EXTRA_CON),
            phamVi = PhamVi.valueOf(intent.getStringExtra(EXTRA_PHAM_VI) ?: PhamVi.TOAN_QUOC.name)
        )

        adapter = TinDangAdapter(
            khiBamTim = { tin ->
                LuuTin.doi(this, tin.id) { luu ->
                    daLuu = if (luu) daLuu + tin.id else daLuu - tin.id
                    adapter.capNhat(ketQua, daLuu)
                }
            },
            khiBam = { ChiTietSanPhamActivity.mo(this, it.id) }
        )
        binding.dsKetQua.layoutManager = GridLayoutManager(this, 2)
        binding.dsKetQua.adapter = adapter

        binding.btnQuayLai.setOnClickListener { finish() }
        binding.nhapTuKhoa.doAfterTextChanged {
            dk = dk.copy(tuKhoa = it?.toString() ?: "")
            capNhatKetQua()
        }
        binding.chipPhamVi.setOnClickListener { chonPhamVi() }
        binding.chipBoLoc.setOnClickListener { moBoLoc() }
        binding.chipSapXep.setOnClickListener { chonSapXep() }

        // Mở từ ô tìm kiếm (không kèm danh mục) → bật bàn phím luôn
        if (dk.danhMucCha == null) {
            binding.nhapTuKhoa.requestFocus()
            binding.nhapTuKhoa.post {
                getSystemService(InputMethodManager::class.java).showSoftInput(binding.nhapTuKhoa, 0)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            val nd = NguoiDungRepository(this@TimKiemActivity).layNguoiDangNhap()
            val repo = TinDangRepository(this@TimKiemActivity)
            nguoiDung = nd
            tatCa = repo.layDangHienThi()
            daLuu = if (nd == null) emptySet() else repo.layIdDaLuu(nd.id)
            // Chưa có trường thì chỉ tìm được toàn quốc
            val coTruong = HangSo.timTruong(nd?.maTruong) != null
            dk = dk.copy(
                maTruong = nd?.maTruong ?: "",
                thanhPho = nd?.thanhPho ?: "",
                phamVi = if (coTruong) dk.phamVi else PhamVi.TOAN_QUOC
            )
            capNhatKetQua()
        }
    }

    // ------------------------------------------------------------------
    private fun capNhatKetQua() {
        ketQua = LocTinDang.loc(tatCa, dk)
        adapter.capNhat(ketQua, daLuu)

        binding.chipPhamVi.text = tenPhamVi(dk.phamVi)
        val soLoc = dk.soBoLoc()
        binding.chipBoLoc.text = if (soLoc > 0) "Bộ lọc · $soLoc" else "Bộ lọc"
        binding.chipBoLoc.chipBackgroundColor =
            ContextCompat.getColorStateList(this, if (soLoc > 0) R.color.chinh_nhat else R.color.be_mat)
        binding.chipSapXep.text = dk.sapXep.ten

        // Dòng tóm tắt: "5 kết quả · Hà Nội · Sách › Giáo trình · Dưới 50.000đ"
        val nhan = mutableListOf("${ketQua.size} kết quả", tenPhamVi(dk.phamVi))
        dk.danhMucCha?.let { cha ->
            val tenCha = HangSo.timDanhMuc(cha)?.ten ?: ""
            val tenCon = HangSo.timDanhMucCon(dk.danhMucCon)?.ten
            nhan.add(if (tenCon == null) tenCha else "$tenCha › $tenCon")
        }
        dk.giaToiDa?.let { nhan.add("Đến ${DinhDang.gia(it)}") }
        if (dk.tinhTrang.isNotEmpty()) nhan.add(dk.tinhTrang.joinToString(", ") { HangSo.timTinhTrang(it).ten })
        binding.tvKetQua.text = nhan.joinToString(" · ")

        // Trạng thái trống: gợi ý mở rộng phạm vi hoặc xoá bộ lọc
        val trong = binding.trong
        trong.root.visibility = if (ketQua.isEmpty()) View.VISIBLE else View.GONE
        trong.tvTieuDeTrong.text = "Không tìm thấy kết quả"
        trong.tvPhuTrong.text = if (dk.tuKhoa.isBlank()) "Thử đổi bộ lọc hoặc phạm vi" else "cho “${dk.tuKhoa.trim()}”"
        trong.btnTrong1.visibility = if (dk.phamVi != PhamVi.TOAN_QUOC) View.VISIBLE else View.GONE
        trong.btnTrong1.text = "Mở rộng ra toàn quốc"
        trong.btnTrong1.setOnClickListener { dk = dk.copy(phamVi = PhamVi.TOAN_QUOC); capNhatKetQua() }
        trong.btnTrong2.visibility = if (soLoc > 0) View.VISIBLE else View.GONE
        trong.btnTrong2.text = "Xoá bộ lọc"
        trong.btnTrong2.setOnClickListener { xoaBoLoc(); capNhatKetQua() }
    }

    private fun tenPhamVi(pv: PhamVi): String = when (pv) {
        PhamVi.TRUONG -> "Trường tôi"
        PhamVi.THANH_PHO -> dk.thanhPho.ifEmpty { "Thành phố" }
        PhamVi.TOAN_QUOC -> "Toàn quốc"
    }

    private fun xoaBoLoc() {
        dk = dk.copy(danhMucCha = null, danhMucCon = null, giaToiDa = null, tinhTrang = emptySet())
    }

    private fun chonPhamVi() {
        val coTruong = HangSo.timTruong(nguoiDung?.maTruong) != null
        val dsPhamVi = if (coTruong) PhamVi.entries else listOf(PhamVi.TOAN_QUOC)
        val menu = PopupMenu(this, binding.chipPhamVi)
        dsPhamVi.forEachIndexed { i, pv -> menu.menu.add(0, i, i, tenPhamVi(pv)) }
        menu.setOnMenuItemClickListener {
            dk = dk.copy(phamVi = dsPhamVi[it.itemId])
            capNhatKetQua()
            true
        }
        menu.show()
    }

    private fun chonSapXep() {
        val menu = PopupMenu(this, binding.chipSapXep)
        LocTinDang.SapXep.entries.forEachIndexed { i, sx -> menu.menu.add(0, i, i, sx.ten) }
        menu.setOnMenuItemClickListener {
            dk = dk.copy(sapXep = LocTinDang.SapXep.entries[it.itemId])
            capNhatKetQua()
            true
        }
        menu.show()
    }

    // ------------------------------------------------------------------
    /** Bảng Bộ lọc: mỗi lần chạm chip thì áp dụng ngay và cập nhật số kết quả trên nút. */
    private fun moBoLoc() {
        val bang = BottomSheetDialog(this)
        val b = BottomSheetBoLocBinding.inflate(layoutInflater)
        bang.setContentView(b.root)

        fun veBang() {
            // Danh mục cha
            b.nhomDanhMuc.removeAllViews()
            themChip(b.nhomDanhMuc, "Tất cả", dk.danhMucCha == null) {
                dk = dk.copy(danhMucCha = null, danhMucCon = null)
            }
            for (dm in HangSo.DS_DANH_MUC) {
                themChip(b.nhomDanhMuc, dm.ten, dk.danhMucCha == dm.ma) {
                    dk = dk.copy(danhMucCha = dm.ma, danhMucCon = null)
                }
            }
            // Danh mục con (chỉ hiện khi đã chọn danh mục cha)
            val cha = HangSo.timDanhMuc(dk.danhMucCha)
            b.tvNhanDanhMucCon.visibility = if (cha == null) View.GONE else View.VISIBLE
            b.nhomDanhMucCon.visibility = if (cha == null) View.GONE else View.VISIBLE
            b.nhomDanhMucCon.removeAllViews()
            cha?.let { HangSo.conDangHien(it) }?.forEach { con ->
                themChip(b.nhomDanhMucCon, con.ten, dk.danhMucCon == con.ma) {
                    dk = dk.copy(danhMucCon = if (dk.danhMucCon == con.ma) null else con.ma)
                }
            }
            // Giá tối đa
            b.nhomGia.removeAllViews()
            for ((gia, ten) in LocTinDang.MUC_GIA) {
                themChip(b.nhomGia, ten, dk.giaToiDa == gia) { dk = dk.copy(giaToiDa = gia) }
            }
            // Tình trạng (chọn nhiều)
            b.nhomTinhTrang.removeAllViews()
            for (tt in HangSo.DS_TINH_TRANG) {
                themChip(b.nhomTinhTrang, tt.ten, tt.ma in dk.tinhTrang) {
                    dk = dk.copy(tinhTrang = if (tt.ma in dk.tinhTrang) dk.tinhTrang - tt.ma else dk.tinhTrang + tt.ma)
                }
            }
            b.btnXemKetQua.text = "Xem ${ketQua.size} kết quả"
        }

        // Gán sau để themChip gọi lại được veBang()
        veLaiBang = { capNhatKetQua(); veBang() }
        b.btnXoaTatCa.setOnClickListener { xoaBoLoc(); veLaiBang() }
        b.btnXemKetQua.setOnClickListener { bang.dismiss() }
        veBang()
        bang.show()
    }

    private var veLaiBang: () -> Unit = {}

    private fun themChip(nhom: ChipGroup, chu: String, dangChon: Boolean, khiChon: () -> Unit) {
        val chip = ChipLocBinding.inflate(layoutInflater, nhom, false).root
        chip.text = chu
        chip.isChecked = dangChon
        chip.setOnClickListener {
            khiChon()
            veLaiBang()
        }
        nhom.addView(chip)
    }
}

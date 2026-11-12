package com.unimarket.man_hinh.trang_chu

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.unimarket.R
import com.unimarket.databinding.ActivityManHinhChinhBinding
import com.unimarket.du_lieu.repository.GiaoDichRepository
import com.unimarket.du_lieu.repository.NguoiDungRepository
import com.unimarket.man_hinh.chat.TinNhanFragment
import com.unimarket.man_hinh.dang_tin.DangTinActivity
import com.unimarket.man_hinh.tai_khoan.BoSungHoSo
import com.unimarket.man_hinh.tai_khoan.TaiKhoanFragment
import com.unimarket.man_hinh.tim_kiem.DanhMucFragment
import com.unimarket.tien_ich.CanhLe
import kotlinx.coroutines.launch

/**
 * Màn hình chính chứa 5 tab: Trang chủ, Danh mục, Đăng tin, Tin nhắn, Tài khoản.
 * Mỗi tab là một Fragment; đổi tab thì ẩn/hiện Fragment (không tạo lại → giữ vị trí cuộn).
 */
class ManHinhChinhActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_LOC_TIN_ID = "loc_tin_id"
        private const val EXTRA_LOC_TIN_TEN = "loc_tin_ten"

        /** Mở tab Tin nhắn, lọc sẵn các cuộc trao đổi về một tin (từ "N người đang hỏi mua"). */
        fun moTinNhanTheoTin(context: Context, tinId: Long, tieuDe: String) {
            context.startActivity(
                Intent(context, ManHinhChinhActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    .putExtra(EXTRA_LOC_TIN_ID, tinId)
                    .putExtra(EXTRA_LOC_TIN_TEN, tieuDe)
            )
        }
    }

    private lateinit var binding: ActivityManHinhChinhBinding
    private var locTinCho: Pair<Long, String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManHinhChinhBinding.inflate(layoutInflater)
        setContentView(binding.root)
        CanhLe.chuaVienHeThong(binding.root)

        if (savedInstanceState == null) hienTab(R.id.tab_trang_chu)

        binding.thanhDieuHuong.setOnItemSelectedListener { muc ->
            if (muc.itemId == R.id.tab_dang_tin) {
                moDangTin()
                false // nút Đăng tin mở màn riêng, không đổi tab đang chọn
            } else {
                hienTab(muc.itemId)
                true
            }
        }
        xuLyIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        xuLyIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        capNhatSoChuaDoc()
        // Mở tab Tin nhắn có lọc ở onResume (lúc này mới được đổi Fragment an toàn)
        locTinCho?.let { (tinId, ten) ->
            locTinCho = null
            chonTab(R.id.tab_tin_nhan)
            (supportFragmentManager.findFragmentByTag("tab_${R.id.tab_tin_nhan}") as? TinNhanFragment)?.locTheoTin(tinId, ten)
        }
    }

    /** Chuyển sang tab khác bằng code (ví dụ từ tab Tài khoản bấm "Tin nhắn & giao dịch"). */
    fun chonTab(maTab: Int) {
        binding.thanhDieuHuong.selectedItemId = maTab
    }

    /** Số tin nhắn chưa đọc hiện thành chấm đỏ trên tab Tin nhắn. */
    fun capNhatSoChuaDoc() {
        lifecycleScope.launch {
            val nd = NguoiDungRepository(this@ManHinhChinhActivity).layNguoiDangNhap()
            val so = if (nd == null) 0 else GiaoDichRepository(this@ManHinhChinhActivity).tongChuaDoc(nd.id)
            val huyHieu = binding.thanhDieuHuong.getOrCreateBadge(R.id.tab_tin_nhan)
            huyHieu.isVisible = so > 0
            huyHieu.number = so
        }
    }

    /** Ghi nhớ yêu cầu "lọc tin nhắn theo tin"; thực hiện ở onResume. */
    private fun xuLyIntent(intent: Intent) {
        val tinId = intent.getLongExtra(EXTRA_LOC_TIN_ID, -1)
        if (tinId <= 0) return
        intent.removeExtra(EXTRA_LOC_TIN_ID) // xoay màn hình không lọc lại
        locTinCho = tinId to intent.getStringExtra(EXTRA_LOC_TIN_TEN).orEmpty()
    }

    /** Hiện Fragment của tab được chọn, ẩn các tab khác. */
    private fun hienTab(maTab: Int) {
        val ten = "tab_$maTab"
        val giaoDich = supportFragmentManager.beginTransaction()
        supportFragmentManager.fragments.forEach { giaoDich.hide(it) }
        val daCo = supportFragmentManager.findFragmentByTag(ten)
        if (daCo == null) giaoDich.add(R.id.khungNoiDung, taoFragment(maTab), ten) else giaoDich.show(daCo)
        giaoDich.commitNow() // chạy ngay để có thể gọi hàm của tab vừa mở (ví dụ lọc tin nhắn)
    }

    private fun taoFragment(maTab: Int): Fragment = when (maTab) {
        R.id.tab_trang_chu -> TrangChuFragment()
        R.id.tab_danh_muc -> DanhMucFragment()
        R.id.tab_tin_nhan -> TinNhanFragment()
        else -> TaiKhoanFragment()
    }

    /** Bấm "Đăng tin": phải đăng nhập và có hồ sơ đầy đủ mới được vào. */
    private fun moDangTin() {
        BoSungHoSo.kiemTra(this) { DangTinActivity.moMoi(this) }
    }
}

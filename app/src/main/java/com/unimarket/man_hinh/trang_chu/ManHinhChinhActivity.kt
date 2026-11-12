package com.unimarket.man_hinh.trang_chu

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.unimarket.tien_ich.CanhLe
import com.unimarket.R
import com.unimarket.databinding.ActivityManHinhChinhBinding
import com.unimarket.man_hinh.tai_khoan.BoSungHoSo
import com.unimarket.man_hinh.tai_khoan.TaiKhoanFragment
import com.unimarket.man_hinh.tim_kiem.DanhMucFragment

/**
 * Màn hình chính chứa 5 tab: Trang chủ, Danh mục, Đăng tin, Tin nhắn, Tài khoản.
 * Mỗi tab là một Fragment; đổi tab thì ẩn/hiện Fragment (không tạo lại → giữ vị trí cuộn).
 */
class ManHinhChinhActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManHinhChinhBinding

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
    }

    /** Hiện Fragment của tab được chọn, ẩn các tab khác. */
    private fun hienTab(maTab: Int) {
        val ten = "tab_$maTab"
        val giaoDich = supportFragmentManager.beginTransaction()
        supportFragmentManager.fragments.forEach { giaoDich.hide(it) }
        val daCo = supportFragmentManager.findFragmentByTag(ten)
        if (daCo == null) giaoDich.add(R.id.khungNoiDung, taoFragment(maTab), ten) else giaoDich.show(daCo)
        giaoDich.commit()
    }

    private fun taoFragment(maTab: Int): Fragment = when (maTab) {
        R.id.tab_trang_chu -> TrangChuFragment()
        R.id.tab_danh_muc -> DanhMucFragment()
        R.id.tab_tin_nhan -> SapCoFragment.tao(
            R.drawable.ic_chat, "Tin nhắn",
            "Phòng chat kèm lịch hẹn giao dịch (Sprint 4)."
        )
        else -> TaiKhoanFragment()
    }

    /** Bấm "Đăng tin": phải đăng nhập và có hồ sơ đầy đủ mới được vào. */
    private fun moDangTin() {
        BoSungHoSo.kiemTra(this) {
            AlertDialog.Builder(this)
                .setTitle("Đăng tin")
                .setMessage("Hồ sơ của bạn đã đầy đủ. Màn đăng tin 3 bước kèm gợi ý giá sẽ làm ở Sprint 3.")
                .setPositiveButton("Đã hiểu", null)
                .show()
        }
    }
}

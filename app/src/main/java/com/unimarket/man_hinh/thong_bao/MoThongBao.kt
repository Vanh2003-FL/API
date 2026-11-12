package com.unimarket.man_hinh.thong_bao

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.unimarket.R
import com.unimarket.du_lieu.repository.ThongBaoRepository
import com.unimarket.man_hinh.chat.PhongChatActivity
import com.unimarket.man_hinh.chi_tiet_san_pham.ChiTietSanPhamActivity
import com.unimarket.man_hinh.dang_tin.TinCuaToiActivity
import com.unimarket.man_hinh.quan_tri.KiemDuyetActivity
import com.unimarket.mo_hinh.ThongBao
import kotlinx.coroutines.launch

/** Dùng chung cho trung tâm thông báo và banner: icon theo loại, và mở đúng màn khi bấm vào thông báo. */
object MoThongBao {

    /** Icon, màu icon, màu nền vòng tròn theo loại thông báo. */
    data class Kieu(val icon: Int, val mauIcon: Int, val mauNen: Int)

    fun kieuCua(loai: String): Kieu = when (loai) {
        "DAT_MUA", "MOI_DAT_MUA" -> Kieu(R.drawable.ic_cal, R.color.thong_tin, R.color.nen_thong_tin)
        "DOI_HEN" -> Kieu(R.drawable.ic_swap, R.color.canh_bao, R.color.nen_canh_bao)
        "CHOT_HEN" -> Kieu(R.drawable.ic_check, R.color.chinh, R.color.chinh_nhat)
        "NHAC_HEN" -> Kieu(R.drawable.ic_clock, R.color.canh_bao, R.color.nen_canh_bao)
        "HUY_HEN" -> Kieu(R.drawable.ic_close, R.color.loi, R.color.nen_loi)
        "DA_DANG" -> Kieu(R.drawable.ic_pin, R.color.chinh, R.color.chinh_nhat)
        "SAP_HET_HAN" -> Kieu(R.drawable.ic_info, R.color.gia, R.color.nen_gia)
        "MOI_DANH_GIA", "DANH_GIA" -> Kieu(R.drawable.ic_star, R.color.gia, R.color.nen_gia)
        "KIEM_DUYET" -> Kieu(R.drawable.ic_shield, R.color.thong_tin, R.color.nen_thong_tin)
        "BAO_CAO" -> Kieu(R.drawable.ic_flag, R.color.loi, R.color.nen_loi)
        "TIN_BI_AN" -> Kieu(R.drawable.ic_shield, R.color.canh_bao, R.color.nen_canh_bao)
        else -> Kieu(R.drawable.ic_bell, R.color.chu_phu, R.color.nen_lom)
    }

    /**
     * Đánh dấu đã đọc rồi mở nội dung liên quan:
     * thông báo giao dịch → phòng chat (cuộn tới thẻ sự kiện), thông báo về tin → Tin của tôi,
     * thông báo báo cáo vi phạm (quản trị viên) → màn Kiểm duyệt.
     */
    fun mo(activity: Activity, tb: ThongBao) {
        (activity as? LifecycleOwner)?.lifecycleScope?.launch { ThongBaoRepository(activity).danhDauDaDoc(tb.id) }
        val id = tb.lienKetId ?: return
        when (tb.loaiLienKet) {
            "CHAT" -> PhongChatActivity.mo(activity, id, cuonToiLuc = tb.thoiGian)
            "TIN_CUA_TOI" -> TinCuaToiActivity.mo(activity)
            "TIN_DANG" -> ChiTietSanPhamActivity.mo(activity, id)
            "KIEM_DUYET" -> activity.startActivity(Intent(activity, KiemDuyetActivity::class.java))
        }
    }
}

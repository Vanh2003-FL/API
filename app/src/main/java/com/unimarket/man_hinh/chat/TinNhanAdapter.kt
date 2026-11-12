package com.unimarket.man_hinh.chat

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.unimarket.R
import com.unimarket.databinding.ItemTheSuKienBinding
import com.unimarket.databinding.ItemTinNhanBinding
import com.unimarket.mo_hinh.TinNhan
import com.unimarket.tien_ich.DinhDang
import com.unimarket.tien_ich.HangSo

/**
 * Danh sách trong phòng chat, gồm 2 kiểu dòng:
 * - Tin nhắn chữ: của mình căn phải nền xanh, của bên kia căn trái nền trắng.
 * - Thẻ sự kiện lịch hẹn: căn giữa, icon và màu theo loại sự kiện.
 */
class TinNhanAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val KIEU_CHU = 0
        private const val KIEU_SU_KIEN = 1
    }

    private var ds: List<TinNhan> = emptyList()
    private var toiId = 0L
    private var tenDoiPhuong = ""

    fun capNhat(dsMoi: List<TinNhan>, nguoiXemId: Long, tenBenKia: String) {
        ds = dsMoi
        toiId = nguoiXemId
        tenDoiPhuong = tenBenKia
        notifyDataSetChanged()
    }

    class OChu(val b: ItemTinNhanBinding) : RecyclerView.ViewHolder(b.root)
    class OSuKien(val b: ItemTheSuKienBinding) : RecyclerView.ViewHolder(b.root)

    override fun getItemCount() = ds.size

    override fun getItemViewType(position: Int) =
        if (ds[position].loai == HangSo.TIN_NHAN_SU_KIEN) KIEU_SU_KIEN else KIEU_CHU

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == KIEU_SU_KIEN) OSuKien(ItemTheSuKienBinding.inflate(inflater, parent, false))
        else OChu(ItemTinNhanBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val tn = ds[position]
        when (holder) {
            is OChu -> veTinNhanChu(holder.b, tn)
            is OSuKien -> veTheSuKien(holder.b, tn)
        }
    }

    private fun veTinNhanChu(b: ItemTinNhanBinding, tn: TinNhan) {
        val ctx = b.root.context
        val cuaToi = tn.nguoiGuiId == toiId
        b.hang.gravity = if (cuaToi) Gravity.END else Gravity.START
        b.tvNoiDung.text = tn.noiDung
        b.tvNoiDung.setBackgroundResource(if (cuaToi) R.drawable.bg_bong_toi else R.drawable.bg_bong_ban)
        b.tvNoiDung.setTextColor(ContextCompat.getColor(ctx, if (cuaToi) R.color.tren_chinh else R.color.chu))
        b.tvGio.text = DinhDang.gioNgan(tn.thoiGian)
    }

    /**
     * noiDung của thẻ sự kiện là cụm động từ, ví dụ "gửi yêu cầu mua" hoặc "huỷ lịch hẹn · Không tiện giờ".
     * Ghép chủ ngữ: "Bạn" (mình làm), tên bên kia, hoặc không có chủ ngữ nếu là hệ thống (nguoiGuiId = 0).
     */
    private fun veTheSuKien(b: ItemTheSuKienBinding, tn: TinNhan) {
        val ctx = b.root.context
        val phan = tn.noiDung.split(" · ", limit = 2)
        val chuNgu = when (tn.nguoiGuiId) {
            0L -> ""
            toiId -> "Bạn "
            else -> "$tenDoiPhuong "
        }
        b.tvSuKien.text = (chuNgu + phan[0]).replaceFirstChar { it.uppercase() }

        val chiTiet = mutableListOf<String>()
        if (phan.size > 1) chiTiet += phan[1].replaceFirstChar { it.uppercase() }
        if (tn.diaDiem != null && tn.thoiGianHen != null) chiTiet += "${tn.diaDiem} · ${DinhDang.gioHen(tn.thoiGianHen)}"
        b.tvChiTiet.text = chiTiet.joinToString("\n")
        b.tvChiTiet.visibility = if (chiTiet.isEmpty()) View.GONE else View.VISIBLE
        b.tvGio.text = DinhDang.gioNgan(tn.thoiGian)

        val (icon, mauIcon, mauNen) = when (tn.suKien) {
            HangSo.SK_DAT_MUA -> Triple(R.drawable.ic_cal, R.color.thong_tin, R.color.nen_thong_tin)
            HangSo.SK_DOI_HEN -> Triple(R.drawable.ic_swap, R.color.canh_bao, R.color.nen_canh_bao)
            HangSo.SK_CHOT_HEN -> Triple(R.drawable.ic_check, R.color.chinh, R.color.chinh_nhat)
            HangSo.SK_HUY_HEN -> Triple(R.drawable.ic_close, R.color.loi, R.color.nen_loi)
            HangSo.SK_NHAC_HEN -> Triple(R.drawable.ic_clock, R.color.canh_bao, R.color.nen_canh_bao)
            HangSo.SK_DA_BAN -> Triple(R.drawable.ic_tag, R.color.gia, R.color.nen_gia)
            HangSo.SK_DANH_GIA -> Triple(R.drawable.ic_star, R.color.gia, R.color.nen_gia)
            else -> Triple(R.drawable.ic_cal, R.color.chinh, R.color.nen_lom) // lời mời đặt mua
        }
        b.iconSuKien.setImageResource(icon)
        b.iconSuKien.imageTintList = ContextCompat.getColorStateList(ctx, mauIcon)
        b.khungThe.backgroundTintList = ContextCompat.getColorStateList(ctx, mauNen)
    }
}

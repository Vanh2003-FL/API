package com.unimarket.man_hinh.chat

import com.unimarket.R
import com.unimarket.xu_ly_nghiep_vu.QuyTacLichHen.Kieu

/** Màu nền, màu chữ và icon theo nhóm trạng thái lịch hẹn – dùng chung cho thanh ghim và nhãn ở tab Tin nhắn. */
object MauLichHen {

    data class Mau(val nen: Int, val chu: Int, val icon: Int)

    fun cua(kieu: Kieu): Mau = when (kieu) {
        Kieu.TRONG -> Mau(R.color.nen_lom, R.color.chu_phu, R.drawable.ic_cal)
        Kieu.CHO -> Mau(R.color.nen_canh_bao, R.color.canh_bao, R.drawable.ic_clock)
        Kieu.DA_CHOT -> Mau(R.color.chinh_nhat, R.color.tren_chinh_nhat, R.drawable.ic_check)
        Kieu.XONG -> Mau(R.color.nen_thong_tin, R.color.thong_tin, R.drawable.ic_tag)
        Kieu.HUY -> Mau(R.color.nen_loi, R.color.loi, R.drawable.ic_close)
    }
}

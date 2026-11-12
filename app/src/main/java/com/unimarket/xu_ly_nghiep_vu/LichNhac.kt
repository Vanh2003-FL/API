package com.unimarket.xu_ly_nghiep_vu

import com.unimarket.mo_hinh.LichHen
import com.unimarket.mo_hinh.TinDang
import com.unimarket.tien_ich.HangSo

/**
 * Quy tắc cho các TÁC VỤ TỰ ĐỘNG của hệ thống (tác nhân "Hệ thống" trong đặc tả):
 * - UC-62 / BR-37: nhắc cả hai bên trước giờ hẹn 1 giờ (chỉ với lịch đã chốt, mỗi lịch nhắc một lần).
 * - UC-63 / BR-15: yêu cầu chờ xác nhận quá 24 giờ thì tự huỷ.
 * - Báo người bán khi tin còn ≤ 3 ngày là hết hạn (mỗi tin một lần).
 * - Tính thời điểm cần kiểm tra lần tới để đặt báo thức (app đóng vẫn nhắc được).
 * Chỉ gồm hàm thuần, không đọc database → viết unit test được.
 */
object LichNhac {

    const val NHAC_TRUOC = HangSo.MOT_GIO               // nhắc trước giờ hẹn 1 giờ
    const val BAO_HET_HAN_TRUOC = 3 * HangSo.MOT_NGAY   // báo trước khi tin hết hạn 3 ngày

    /** Lịch đã chốt, chưa nhắc, và đã vào khoảng 1 giờ trước giờ hẹn (chưa tới giờ hẹn). */
    fun canNhacHen(lich: LichHen, bayGio: Long): Boolean =
        lich.trangThai == HangSo.LICH_DA_CHOT && lich.daNhacLuc == null &&
            bayGio >= lich.thoiGianHen - NHAC_TRUOC && bayGio < lich.thoiGianHen

    /** Yêu cầu đang chờ xác nhận đã quá hạn phản hồi 24 giờ. */
    fun quaHanPhanHoi(lich: LichHen, bayGio: Long): Boolean =
        lich.trangThai == HangSo.LICH_CHO_XAC_NHAN && bayGio >= lich.hetHanLuc

    /** Tin đang bán, chưa được báo, còn không quá 3 ngày là hết hạn. */
    fun canBaoSapHetHan(tin: TinDang, bayGio: Long): Boolean =
        tin.trangThai == HangSo.TIN_DANG_BAN && !tin.daBaoSapHetHan &&
            tin.ngayHetHan > bayGio && tin.ngayHetHan - bayGio <= BAO_HET_HAN_TRUOC

    /** Còn bao lâu tới giờ hẹn, viết cho người đọc: "1 giờ" (từ 55 phút trở lên) hoặc "40 phút". */
    fun conLai(thoiGianHen: Long, bayGio: Long): String {
        val phut = ((thoiGianHen - bayGio + HangSo.MOT_PHUT - 1) / HangSo.MOT_PHUT).coerceAtLeast(1)
        return if (phut >= 55) "1 giờ" else "$phut phút"
    }

    /** Số ngày còn lại (làm tròn lên) trước khi tin hết hạn. */
    fun soNgayConLai(ngayHetHan: Long, bayGio: Long): Long =
        ((ngayHetHan - bayGio + HangSo.MOT_NGAY - 1) / HangSo.MOT_NGAY).coerceAtLeast(0)

    /**
     * Thời điểm SỚM NHẤT trong tương lai cần chạy lại tác vụ tự động, hoặc null nếu không còn gì phải làm.
     * Dùng để đặt báo thức AlarmManager khi app đóng.
     */
    fun thoiDiemKiemTraTiepTheo(dsLich: List<LichHen>, dsTin: List<TinDang>, bayGio: Long): Long? {
        val cacMoc = mutableListOf<Long>()
        for (l in dsLich) when (l.trangThai) {
            HangSo.LICH_DA_CHOT -> if (l.daNhacLuc == null && l.thoiGianHen > bayGio) cacMoc += l.thoiGianHen - NHAC_TRUOC
            HangSo.LICH_CHO_XAC_NHAN -> cacMoc += l.hetHanLuc
        }
        for (t in dsTin) if (t.trangThai == HangSo.TIN_DANG_BAN && !t.daBaoSapHetHan && t.ngayHetHan > bayGio) {
            cacMoc += t.ngayHetHan - BAO_HET_HAN_TRUOC
        }
        // Mốc đã qua (ví dụ app vừa mở lại) → kiểm tra ngay sau 1 phút
        return cacMoc.minOrNull()?.coerceAtLeast(bayGio + HangSo.MOT_PHUT)
    }
}

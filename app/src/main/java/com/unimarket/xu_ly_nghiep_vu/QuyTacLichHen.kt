package com.unimarket.xu_ly_nghiep_vu

import com.unimarket.mo_hinh.LichHen
import com.unimarket.mo_hinh.TinDang
import com.unimarket.tien_ich.DinhDang
import com.unimarket.tien_ich.HangSo
import java.util.Calendar

/**
 * Quy tắc nghiệp vụ của lịch hẹn giao dịch (BR-33 → BR-41) và nội dung thanh ghim trong phòng chat.
 * Chỉ gồm hàm thuần (không đọc database) để viết unit test.
 *
 * Vòng đời lịch hẹn:
 *   CHO_XAC_NHAN ──(bên kia xác nhận)──► DA_CHOT ──(người bán bấm Đã bán)──► HOAN_TAT
 *        │  ▲                                │
 *        │  └──(một bên đổi địa điểm/giờ)    │
 *        └──────────(huỷ / từ chối)──────────┴──► DA_HUY
 * Người đưa ra đề xuất mới nhất (nguoiDeXuatId) KHÔNG được tự xác nhận; người còn lại mới xác nhận (BR-35).
 */
object QuyTacLichHen {

    // ===================== KIỂM TRA DỮ LIỆU NHẬP =====================

    /** BR-37: giờ hẹn phải sau hiện tại ít nhất 30 phút và không quá 7 ngày. Trả về câu báo lỗi hoặc null. */
    fun loiGioHen(thoiGianHen: Long, bayGio: Long): String? = when {
        thoiGianHen < bayGio + HangSo.PHUT_HEN_SOM_NHAT * HangSo.MOT_PHUT ->
            "Giờ hẹn phải sau bây giờ ít nhất ${HangSo.PHUT_HEN_SOM_NHAT} phút."
        thoiGianHen > bayGio + HangSo.SO_NGAY_HEN_TOI_DA * HangSo.MOT_NGAY ->
            "Chỉ hẹn trong vòng ${HangSo.SO_NGAY_HEN_TOI_DA} ngày tới."
        else -> null
    }

    /** BR-34: địa điểm là một điểm có sẵn của trường, hoặc mô tả tự nhập đủ 10 ký tự. */
    fun loiDiaDiem(diaDiem: String): String? {
        val d = diaDiem.trim()
        return when {
            d in HangSo.DIEM_GIAO_DICH -> null
            d.isEmpty() -> "Chọn hoặc nhập địa điểm hẹn."
            d.length < HangSo.SO_KY_TU_DIA_DIEM_TU_NHAP ->
                "Mô tả địa điểm rõ hơn (ít nhất ${HangSo.SO_KY_TU_DIA_DIEM_TU_NHAP} ký tự), ví dụ \"Ghế đá trước nhà A2\"."
            else -> null
        }
    }

    /**
     * Giờ hẹn gợi ý như bản thiết kế: hôm nay 17:00 (nếu còn kịp), mai 11:30, mai 17:00.
     * Trả về danh sách (nhãn, thời điểm).
     */
    fun goiYGioHen(bayGio: Long): List<Pair<String, Long>> {
        fun luc(themNgay: Int, gio: Int, phut: Int) = Calendar.getInstance().apply {
            timeInMillis = bayGio
            add(Calendar.DAY_OF_YEAR, themNgay)
            set(Calendar.HOUR_OF_DAY, gio); set(Calendar.MINUTE, phut); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val ds = mutableListOf<Pair<String, Long>>()
        val chieuNay = luc(0, 17, 0)
        if (loiGioHen(chieuNay, bayGio) == null) ds += "Hôm nay 17:00" to chieuNay
        ds += "Mai 11:30" to luc(1, 11, 30)
        ds += "Mai 17:00" to luc(1, 17, 0)
        if (ds.size < 3) ds += "Ngày kia 11:30" to luc(2, 11, 30)
        return ds
    }

    // ===================== TRẠNG THÁI =====================

    /** Lịch hẹn còn hiệu lực (chưa huỷ, chưa hoàn tất). BR-40: mỗi phòng chat tối đa 1 lịch như vậy. */
    fun dangHoatDong(lich: LichHen?): Boolean =
        lich != null && (lich.trangThai == HangSo.LICH_CHO_XAC_NHAN || lich.trangThai == HangSo.LICH_DA_CHOT)

    /** Người phải bấm xác nhận = người KHÔNG đưa ra đề xuất mới nhất. */
    fun nguoiPhaiXacNhan(lich: LichHen): Long =
        if (lich.nguoiDeXuatId == lich.nguoiMuaId) lich.nguoiBanId else lich.nguoiMuaId

    /** Lịch đang chờ CHÍNH người này xác nhận (dùng cho bộ lọc "Chờ tôi xác nhận"). */
    fun choToiXacNhan(lich: LichHen?, nguoiDungId: Long): Boolean =
        lich != null && lich.trangThai == HangSo.LICH_CHO_XAC_NHAN && nguoiPhaiXacNhan(lich) == nguoiDungId

    /** Tin còn nhận đặt mua: đang bán, hoặc đang giao dịch (người sau xếp hàng – BR-36). */
    fun tinConNhanDatMua(tin: TinDang): Boolean =
        tin.trangThai == HangSo.TIN_DANG_BAN || tin.trangThai == HangSo.TIN_DANG_GIAO_DICH

    // ===================== THANH GHIM TRONG PHÒNG CHAT (bảng 3.3.1) =====================

    /** Các nút có thể hiện trên thanh ghim. Nút đầu danh sách là nút chính (tô màu). */
    enum class Nut(val chu: String) {
        DAT_MUA("Đặt mua"),
        DAT_MUA_LAI("Đặt mua lại"),
        MOI_DAT_MUA("Gửi lời mời đặt mua"),
        XAC_NHAN("Xác nhận"),
        DONG_Y("Đồng ý"),
        DOI("Đổi"),
        DE_XUAT_KHAC("Đề xuất khác"),
        TU_CHOI("Từ chối"),
        HUY_YEU_CAU("Huỷ yêu cầu"),
        HUY_HEN("Huỷ hẹn"),
        DA_BAN("Đã bán"),
        DANH_GIA("Đánh giá người bán")
    }

    /** Màu của thanh ghim theo nhóm trạng thái. */
    enum class Kieu { TRONG, CHO, DA_CHOT, XONG, HUY }

    data class ThanhGhim(val tieuDe: String, val moTa: String, val kieu: Kieu, val dsNut: List<Nut>)

    /**
     * Nội dung thanh ghim theo trạng thái lịch hẹn và vai trò người đang xem.
     * @param lich lịch hẹn mới nhất của phòng chat (null = chưa đặt mua)
     * @param tenDoiPhuong tên người bên kia (người bán thấy tên người mua)
     */
    fun thanhGhim(lich: LichHen?, tin: TinDang, laNguoiMua: Boolean, tenDoiPhuong: String, bayGio: Long): ThanhGhim {
        val conBan = tinConNhanDatMua(tin)
        val hen = lich?.let { "${it.diaDiem} · ${DinhDang.gioHen(it.thoiGianHen, bayGio)}" } ?: ""

        // Chưa đặt mua, hoặc lịch cũ đã huỷ
        if (lich == null || lich.trangThai == HangSo.LICH_DA_HUY) {
            if (!conBan) return ThanhGhim("Tin không còn bán", "Bạn vẫn xem lại được cuộc trò chuyện", Kieu.TRONG, emptyList())
            if (lich == null) {
                return if (laNguoiMua) ThanhGhim("Chưa có lịch hẹn", "Đặt mua để chốt địa điểm và giờ gặp", Kieu.TRONG, listOf(Nut.DAT_MUA))
                else ThanhGhim("Người mua chưa đặt mua", "Mời người mua đặt lịch để chốt hẹn", Kieu.TRONG, listOf(Nut.MOI_DAT_MUA))
            }
            val lyDo = "Đã huỷ: ${lich.lyDoHuy ?: "không rõ lý do"}"
            return if (laNguoiMua) ThanhGhim(lyDo, "Bạn có thể đặt mua lại nếu vẫn muốn", Kieu.HUY, listOf(Nut.DAT_MUA_LAI))
            else ThanhGhim(lyDo, "Lịch hẹn trước: $hen", Kieu.HUY, listOf(Nut.MOI_DAT_MUA))
        }

        return when (lich.trangThai) {
            HangSo.LICH_CHO_XAC_NHAN -> {
                val nguoiMuaDeXuat = lich.nguoiDeXuatId == lich.nguoiMuaId
                when {
                    // Người bán đã chốt với người khác → người mua này đang xếp hàng (BR-36)
                    laNguoiMua && nguoiMuaDeXuat && tin.trangThai == HangSo.TIN_DANG_GIAO_DICH ->
                        ThanhGhim("Người bán đã hẹn với người khác", "Bạn đang xếp hàng · $hen", Kieu.CHO, listOf(Nut.HUY_YEU_CAU))
                    laNguoiMua && nguoiMuaDeXuat ->
                        ThanhGhim("Chờ người bán xác nhận", hen, Kieu.CHO, listOf(Nut.HUY_YEU_CAU))
                    laNguoiMua ->
                        ThanhGhim("Người bán đề xuất", hen, Kieu.CHO, listOf(Nut.DONG_Y, Nut.DE_XUAT_KHAC, Nut.HUY_YEU_CAU))
                    nguoiMuaDeXuat ->
                        ThanhGhim("$tenDoiPhuong muốn mua", hen + (if (lich.loiNhan.isNotBlank()) "\n“${lich.loiNhan}”" else ""),
                            Kieu.CHO, listOf(Nut.XAC_NHAN, Nut.DOI, Nut.TU_CHOI))
                    else ->
                        ThanhGhim("Chờ người mua đồng ý", hen, Kieu.CHO, listOf(Nut.HUY_YEU_CAU))
                }
            }
            HangSo.LICH_DA_CHOT ->
                if (laNguoiMua) ThanhGhim("Đã chốt hẹn", hen, Kieu.DA_CHOT, listOf(Nut.HUY_HEN))
                else ThanhGhim("Đã chốt hẹn", hen, Kieu.DA_CHOT, listOf(Nut.DA_BAN, Nut.HUY_HEN))
            else -> { // HOAN_TAT
                val gia = DinhDang.gia(tin.giaBan ?: tin.gia)
                when {
                    !laNguoiMua -> ThanhGhim("Đã bán $gia", "Giao dịch hoàn tất", Kieu.XONG, emptyList())
                    lich.soSao == null -> ThanhGhim("Đã mua · $gia", "Đánh giá giúp người khác chọn người bán uy tín", Kieu.XONG, listOf(Nut.DANH_GIA))
                    else -> ThanhGhim("Đã mua · $gia", "Bạn đã đánh giá ${"★".repeat(lich.soSao)}", Kieu.XONG, emptyList())
                }
            }
        }
    }

    /** Nhãn trạng thái ngắn cho từng dòng ở tab Tin nhắn (SCR-21). */
    fun nhanTrangThai(lich: LichHen?, nguoiDungId: Long): Pair<String, Kieu> {
        if (lich == null) return "Chưa đặt mua" to Kieu.TRONG
        val laNguoiMua = lich.nguoiMuaId == nguoiDungId
        return when (lich.trangThai) {
            HangSo.LICH_CHO_XAC_NHAN ->
                if (nguoiPhaiXacNhan(lich) == nguoiDungId) "Chờ bạn xác nhận" to Kieu.CHO
                else (if (laNguoiMua) "Chờ người bán" else "Chờ người mua") to Kieu.CHO
            HangSo.LICH_DA_CHOT -> "Đã chốt hẹn" to Kieu.DA_CHOT
            HangSo.LICH_HOAN_TAT -> (if (laNguoiMua) "Đã mua" else "Đã bán") to Kieu.XONG
            else -> "Đã huỷ" to Kieu.HUY
        }
    }

    /** Câu trả lời nhanh theo ngữ cảnh (SCR-22). */
    fun cauTraLoiNhanh(lich: LichHen?, laNguoiMua: Boolean): List<String> = when {
        lich == null || lich.trangThai == HangSo.LICH_DA_HUY ->
            if (laNguoiMua) listOf("Còn hàng không bạn?", "Bớt được không?", "Hẹn gặp ở đâu?")
            else listOf("Còn bạn nhé", "Bạn bấm Đặt mua để chốt hẹn nhé")
        lich.trangThai == HangSo.LICH_CHO_XAC_NHAN -> {
            // Người phải xác nhận trả lời "Ok"; người đã đề xuất thì nhắc bên kia
            val toiDeXuat = (lich.nguoiDeXuatId == lich.nguoiMuaId) == laNguoiMua
            if (toiDeXuat) listOf("Bạn xác nhận giúp mình nhé", "Giờ đó được không bạn?")
            else listOf("Ok bạn", "Giờ đó mình bận, đổi giờ nhé")
        }
        lich.trangThai == HangSo.LICH_DA_CHOT -> listOf("Mình tới rồi nhé", "Mình đến muộn 5 phút", "Bạn mặc áo màu gì?")
        else -> listOf("Cảm ơn bạn nhé!")
    }
}

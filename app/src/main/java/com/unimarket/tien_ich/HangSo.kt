package com.unimarket.tien_ich

import com.unimarket.R

/**
 * Các hằng số dùng chung toàn app: trường, điểm giao dịch, danh mục, tình trạng, trạng thái tin...
 * Đặt hết ở một chỗ để dễ tìm và dễ sửa khi bảo vệ đồ án.
 */
object HangSo {

    // ===================== TRƯỜNG ĐẠI HỌC =====================
    data class Truong(val ma: String, val ten: String, val thanhPho: String)

    val DS_TRUONG = listOf(
        Truong("NEU", "Đại học Kinh tế Quốc dân", "Hà Nội"),
        Truong("HUST", "Đại học Bách khoa Hà Nội", "Hà Nội"),
        Truong("FTU", "Đại học Ngoại thương", "Hà Nội"),
        Truong("UEH", "Đại học Kinh tế TP.HCM", "TP.HCM"),
        Truong("HCMUT", "Đại học Bách khoa – ĐHQG TP.HCM", "TP.HCM"),
        Truong("DUE", "Đại học Kinh tế – ĐH Đà Nẵng", "Đà Nẵng")
    )

    fun timTruong(ma: String?): Truong? = DS_TRUONG.find { it.ma == ma }

    // ===================== ĐIỂM GIAO DỊCH =====================
    /** Các điểm công cộng trong khuôn viên trường, dùng khi đăng tin và hẹn gặp. */
    val DIEM_GIAO_DICH = listOf("Cổng chính", "Thư viện", "Căng tin", "Sảnh giảng đường")
    const val DIEM_KHAC = "Khác"

    // ===================== DANH MỤC =====================
    /** Danh mục con. khauHaoNam = tỷ lệ mất giá mỗi năm (dùng cho gợi ý giá khi thiếu dữ liệu). */
    data class DanhMucCon(val ma: String, val ten: String, val icon: Int, val khauHaoNam: Double)

    /** Danh mục cha: có màu nền và màu icon riêng như bản thiết kế. */
    data class DanhMuc(
        val ma: String,
        val ten: String,
        val icon: Int,
        val mauNen: Int,
        val mauIcon: Int,
        val dsCon: List<DanhMucCon>
    )

    const val SACH = "SACH"
    const val DIEN_TU = "DIEN_TU"
    const val DO_DUNG = "DO_DUNG"

    val DS_DANH_MUC = listOf(
        DanhMuc(
            SACH, "Sách", R.drawable.ic_book, R.color.nen_sach, R.color.icon_sach, listOf(
                DanhMucCon("GIAO_TRINH", "Giáo trình", R.drawable.ic_book, 0.0),
                DanhMucCon("THAM_KHAO", "Sách tham khảo, ngoại ngữ", R.drawable.ic_book, 0.0),
                DanhMucCon("TRUYEN", "Truyện, tiểu thuyết", R.drawable.ic_book, 0.0)
            )
        ),
        DanhMuc(
            DIEN_TU, "Đồ điện tử", R.drawable.ic_laptop, R.color.nen_dien_tu, R.color.icon_dien_tu, listOf(
                DanhMucCon("DIEN_THOAI", "Điện thoại, máy tính bảng", R.drawable.ic_phone, 0.25),
                DanhMucCon("LAPTOP", "Laptop", R.drawable.ic_laptop, 0.20),
                DanhMucCon("PHU_KIEN", "Tai nghe, loa, phụ kiện", R.drawable.ic_headphones, 0.30),
                DanhMucCon("MAY_TINH_CAM_TAY", "Máy tính cầm tay", R.drawable.ic_calc, 0.10)
            )
        ),
        DanhMuc(
            DO_DUNG, "Đồ dùng", R.drawable.ic_fan, R.color.nen_do_dung, R.color.icon_do_dung, listOf(
                DanhMucCon("GIA_DUNG", "Gia dụng", R.drawable.ic_fan, 0.15),
                DanhMucCon("NOI_THAT", "Nội thất", R.drawable.ic_desk, 0.10),
                DanhMucCon("THOI_TRANG", "Quần áo, giày dép", R.drawable.ic_tag, 0.30),
                DanhMucCon("XE_DAP", "Xe đạp", R.drawable.ic_bike, 0.15),
                DanhMucCon("KHAC", "Dụng cụ học tập, khác", R.drawable.ic_tag, 0.20)
            )
        )
    )

    fun timDanhMuc(ma: String?): DanhMuc? = DS_DANH_MUC.find { it.ma == ma }

    fun timDanhMucCon(ma: String?): DanhMucCon? =
        DS_DANH_MUC.flatMap { it.dsCon }.find { it.ma == ma }

    /** Danh mục cha chứa danh mục con này, ví dụ "LAPTOP" → danh mục "Đồ điện tử". */
    fun timChaCua(maCon: String?): DanhMuc =
        DS_DANH_MUC.find { cha -> cha.dsCon.any { it.ma == maCon } } ?: DS_DANH_MUC.last()

    // ===================== TÌNH TRẠNG =====================
    /** heSo: hệ số giá so với "Như mới". tyLeSach: % giá bìa khi ước tính giá sách. */
    data class TinhTrang(
        val ma: String,
        val ten: String,
        val heSo: Double,
        val tyLeSach: Double,
        val moTa: String,
        val mau: Int
    )

    val DS_TINH_TRANG = listOf(
        TinhTrang("NHU_MOI", "Như mới", 1.00, 0.60, "Dùng rất ít, không trầy xước, còn đủ phụ kiện", R.color.tt_nhu_moi),
        TinhTrang("TOT", "Tốt", 0.85, 0.50, "Có dấu hiệu sử dụng nhẹ, hoạt động hoàn hảo", R.color.tt_tot),
        TinhTrang("KHA", "Khá", 0.70, 0.40, "Trầy xước hoặc ghi chú rõ, vẫn dùng tốt", R.color.tt_kha),
        TinhTrang("CU", "Cũ", 0.50, 0.30, "Hao mòn nhiều hoặc có lỗi nhỏ, ghi rõ trong mô tả", R.color.tt_cu)
    )

    fun timTinhTrang(ma: String?): TinhTrang = DS_TINH_TRANG.find { it.ma == ma } ?: DS_TINH_TRANG[1]

    // ===================== TRẠNG THÁI TIN ĐĂNG =====================
    const val TIN_DANG_BAN = "DANG_BAN"
    const val TIN_DANG_GIAO_DICH = "DANG_GIAO_DICH"
    const val TIN_DA_BAN = "DA_BAN"
    const val TIN_HET_HAN = "HET_HAN"
    const val TIN_BI_AN = "BI_AN"
    const val TIN_DA_XOA = "DA_XOA"

    // ===================== BÁO CÁO VI PHẠM =====================
    val LY_DO_BAO_CAO = listOf(
        "Lừa đảo", "Hàng cấm", "Sai danh mục", "Thông tin sai sự thật", "Bùng hẹn nhiều lần", "Spam"
    )

    // ===================== VAI TRÒ =====================
    const val VAI_TRO_NGUOI_DUNG = "NGUOI_DUNG"
    const val VAI_TRO_QUAN_TRI = "QUAN_TRI"

    // ===================== QUY TẮC NGHIỆP VỤ =====================
    const val SO_NGAY_HIEN_TIN = 30          // tin tự hết hạn sau 30 ngày
    const val SO_LAN_GIA_HAN_TOI_DA = 2      // mỗi tin gia hạn tối đa 2 lần
    const val NGUONG_BAO_CAO_TU_AN = 3       // đủ 3 báo cáo thì tin tự ẩn
    const val PHI_DAY_TIN = 10_000L          // phí đẩy tin 3 ngày (mô phỏng doanh thu)

    /** Các gói đẩy tin (thanh toán mô phỏng, không trừ tiền thật). */
    data class GoiDayTin(val soNgay: Int, val phi: Long, val ten: String)

    val DS_GOI_DAY_TIN = listOf(
        GoiDayTin(3, 10_000L, "Gói 3 ngày"),
        GoiDayTin(7, 20_000L, "Gói 7 ngày mùa tốt nghiệp")
    )

    // ===================== LỊCH HẸN GIAO DỊCH =====================
    // CHO_XAC_NHAN → DA_CHOT → HOAN_TAT, hoặc DA_HUY bất cứ lúc nào trước khi hoàn tất
    const val LICH_CHO_XAC_NHAN = "CHO_XAC_NHAN"
    const val LICH_DA_CHOT = "DA_CHOT"
    const val LICH_HOAN_TAT = "HOAN_TAT"
    const val LICH_DA_HUY = "DA_HUY"

    // Loại thẻ sự kiện trong phòng chat (cột tin_nhan.suKien)
    const val SK_DAT_MUA = "DAT_MUA"
    const val SK_DOI_HEN = "DOI_HEN"
    const val SK_CHOT_HEN = "CHOT_HEN"
    const val SK_HUY_HEN = "HUY_HEN"
    const val SK_NHAC_HEN = "NHAC_HEN"
    const val SK_DA_BAN = "DA_BAN"
    const val SK_MOI_DAT_MUA = "MOI_DAT_MUA"
    const val SK_DANH_GIA = "DANH_GIA"

    const val TIN_NHAN_VAN_BAN = "VAN_BAN"
    const val TIN_NHAN_SU_KIEN = "SU_KIEN"

    val LY_DO_HUY_HEN = listOf("Không tiện giờ hẹn", "Đổi ý, không mua nữa", "Đã mua được món khác", "Lý do khác")
    val LY_DO_TU_CHOI = listOf("Đã bán cho người khác", "Không tiện giờ hẹn", "Địa điểm quá xa", "Lý do khác")

    const val SO_YEU_CAU_CHO_TOI_DA = 5      // BR-33: tối đa 5 yêu cầu mua đang chờ cùng lúc
    const val PHUT_HEN_SOM_NHAT = 30         // BR-37: giờ hẹn sau hiện tại ít nhất 30 phút
    const val SO_NGAY_HEN_TOI_DA = 7         // BR-37: không hẹn quá 7 ngày
    const val SO_KY_TU_DIA_DIEM_TU_NHAP = 10 // BR-34: địa điểm tự nhập dài ít nhất 10 ký tự

    const val MOT_NGAY = 24L * 60 * 60 * 1000
    const val MOT_GIO = 60L * 60 * 1000
    const val MOT_PHUT = 60L * 1000
}

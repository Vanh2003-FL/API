package com.unimarket.mo_hinh

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.Serializable

/**
 * Bảng tin_dang: mỗi dòng là một món đồ được đăng bán.
 * Trạng thái: DANG_BAN → DANG_GIAO_DICH → DA_BAN, hoặc HET_HAN / BI_AN / DA_XOA.
 */
@Entity(
    tableName = "tin_dang",
    foreignKeys = [ForeignKey(
        entity = NguoiDung::class,
        parentColumns = ["id"],
        childColumns = ["nguoiBanId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("nguoiBanId"), Index("trangThai"), Index("danhMucCon")]
)
data class TinDang(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nguoiBanId: Long,
    val tieuDe: String,
    val moTa: String = "",
    val danhMucCha: String,              // SACH / DIEN_TU / DO_DUNG
    val danhMucCon: String,              // GIAO_TRINH, LAPTOP, ...
    val gia: Long,                       // giá rao (đồng), 0 = tặng miễn phí
    val giaGoc: Long? = null,            // giá bìa (sách) hoặc giá mua mới
    val namMua: Int? = null,
    val tinhTrang: String,               // NHU_MOI / TOT / KHA / CU
    val thuocTinh: String = "",          // thông tin riêng, mỗi dòng "Tên: giá trị"
    val anh: String = "",                // đường dẫn ảnh, cách nhau bởi dấu ";" (rỗng = dùng icon)
    val maTruong: String,
    val thanhPho: String,
    val diemGiaoDich: String,
    val ghiChuDiaDiem: String = "",
    val coGiaoHang: Boolean = false,
    val xacNhanDiaDiemLuc: Long? = null, // lúc người bán bấm "Xác nhận địa điểm"
    val trangThai: String = "DANG_BAN",
    // Kết quả gợi ý giá tại thời điểm đăng (để đánh giá độ chính xác thuật toán)
    val giaGoiY: Long? = null,
    val goiYThap: Long? = null,
    val goiYCao: Long? = null,
    val doTinCay: String? = null,
    val soMau: Int? = null,
    // Thông tin khi đã bán
    val giaBan: Long? = null,
    val ngayBan: Long? = null,
    val nguoiMuaId: Long? = null,
    // Số liệu
    val luotXem: Int = 0,
    val soBaoCao: Int = 0,
    val soLanGiaHan: Int = 0,
    val soLanDayTin: Int = 0,            // số lần người bán mua gói đẩy tin
    val doanhThuDayTin: Long = 0,        // tổng tiền các gói đẩy tin (thanh toán mô phỏng)
    val dayTinDen: Long? = null,         // tin nổi bật đến thời điểm này
    val tuKhoa: String = "",             // tiêu đề + mô tả viết không dấu, dùng để tìm kiếm
    val ngayTao: Long = System.currentTimeMillis(),
    val ngayCapNhat: Long = System.currentTimeMillis(),
    val ngayHetHan: Long = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000
) : Serializable // Serializable để gửi bản nháp sang màn Xem trước

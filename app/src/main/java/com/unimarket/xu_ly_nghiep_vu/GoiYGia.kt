package com.unimarket.xu_ly_nghiep_vu

import com.unimarket.mo_hinh.TinDang
import com.unimarket.tien_ich.DinhDang
import com.unimarket.tien_ich.HangSo
import kotlin.math.max
import kotlin.math.roundToLong

/**
 * ⭐ TÍNH NĂNG NÂNG CAO 1 – GỢI Ý GIÁ BÁN (tài liệu thiết kế docs/04, mục 4.6)
 *
 * Ý tưởng kinh tế: giá đồ cũ hợp lý = giá mà thị trường (các tin tương tự gần đây) đang chấp nhận,
 * điều chỉnh theo tình trạng món đồ. Các bước:
 *   1. Lấy mẫu: tin cùng danh mục con, trong 90 ngày, tiêu đề giống ≥ 50%.
 *   2. Chuẩn hoá mọi mẫu về giá "Như mới": g = giá / hệ số tình trạng. Tin ĐÃ BÁN nặng gấp đôi
 *      (giá đã được người mua chấp nhận đáng tin hơn giá rao).
 *   3. Chọn phạm vi: ≥ 5 mẫu cùng thành phố thì chỉ dùng thành phố, không thì dùng toàn quốc.
 *   4. Loại giá bất thường bằng khoảng tứ phân vị (IQR).
 *   5. Trung vị có trọng số × hệ số tình trạng của món đang đăng → giá gợi ý; P25–P75 → khoảng hợp lý.
 *   6. Thiếu dữ liệu: ước tính khấu hao từ giá bìa (sách) hoặc giá mua gốc (đồ khác).
 * Người bán luôn được tự sửa giá (gợi ý chỉ để tham khảo).
 */
object GoiYGia {

    const val SO_NGAY_LAY_MAU = 90
    const val NGUONG_TUONG_DONG = 0.5
    const val SO_MAU_TOI_THIEU_THANH_PHO = 5
    const val SO_MAU_TOI_THIEU = 3
    const val TY_LE_CON_LAI_TOI_THIEU = 0.20

    enum class DoTinCay(val ten: String) {
        CAO("Cao"),
        TRUNG_BINH("Trung bình"),
        THAP("Thấp"),
        UOC_TINH("Ước tính"),
        KHONG_DU("Chưa đủ dữ liệu")
    }

    enum class PhamVi { THANH_PHO, TOAN_QUOC, KHAU_HAO, KHONG_CO }

    /** Một mẫu giá đã lấy từ database. */
    data class Mau(val gia: Long, val tinhTrang: String, val daBan: Boolean, val thanhPho: String)

    data class KetQua(
        val giaGoiY: Long = 0,
        val giaThap: Long = 0,
        val giaCao: Long = 0,
        val soMau: Int = 0,          // số mẫu dùng để tính (sau khi loại bất thường)
        val soDaBan: Int = 0,        // trong đó bao nhiêu tin đã bán
        val soBiLoai: Int = 0,       // số mẫu bị loại vì giá bất thường
        val doTinCay: DoTinCay = DoTinCay.KHONG_DU,
        val phamVi: PhamVi = PhamVi.KHONG_CO,
        val giaiThich: String = ""
    ) {
        val coGoiY: Boolean get() = doTinCay != DoTinCay.KHONG_DU
        val tuDuLieuThat: Boolean get() = phamVi == PhamVi.THANH_PHO || phamVi == PhamVi.TOAN_QUOC
    }

    // ===================== BƯỚC 1: LẤY MẪU TƯƠNG TỰ =====================

    /** Tách tiêu đề thành tập từ đơn không dấu, bỏ ký hiệu và từ 1 ký tự. "Kinh tế vi mô – NEU" → {kinh, te, vi, mo, neu} */
    fun tachTu(tieuDe: String): Set<String> =
        DinhDang.boDau(tieuDe).split(Regex("[^a-z0-9]+")).filter { it.length >= 2 }.toSet()

    /** Độ tương đồng = số từ chung / số từ của tin đang đăng. */
    fun doTuongDong(tieuDeMoi: String, tieuDeMau: String): Double {
        val t = tachTu(tieuDeMoi)
        if (t.isEmpty()) return 0.0
        return t.intersect(tachTu(tieuDeMau)).size.toDouble() / t.size
    }

    /**
     * Lọc mẫu từ danh sách tin (đã cùng danh mục con, trong 90 ngày – do câu truy vấn lo).
     * Bỏ tin đang sửa (boQuaId), tin tặng 0đ, tin tiêu đề quá khác.
     */
    fun chonMau(tieuDe: String, dsTin: List<TinDang>, boQuaId: Long = 0): List<Mau> =
        dsTin.filter { it.id != boQuaId && doTuongDong(tieuDe, it.tieuDe) >= NGUONG_TUONG_DONG }
            .map { tin ->
                val daBan = tin.trangThai == HangSo.TIN_DA_BAN
                Mau(if (daBan) tin.giaBan ?: tin.gia else tin.gia, tin.tinhTrang, daBan, tin.thanhPho)
            }
            .filter { it.gia > 0 }

    // ===================== BƯỚC 2–6: TÍNH GIÁ =====================

    /**
     * @param tinhTrang     tình trạng của món đang đăng (NHU_MOI / TOT / KHA / CU)
     * @param thanhPho      thành phố của người bán
     * @param laSach        món đồ thuộc danh mục Sách?
     * @param maDanhMucCon  để lấy tỷ lệ khấu hao năm
     * @param giaGoc        giá bìa (sách) hoặc giá mua mới (đồ khác), có thể null
     * @param namMua        năm mua, có thể null
     */
    fun tinh(
        dsMau: List<Mau>,
        tinhTrang: String,
        thanhPho: String,
        laSach: Boolean,
        maDanhMucCon: String,
        giaGoc: Long?,
        namMua: Int?,
        namHienTai: Int
    ): KetQua {
        val heSoDich = HangSo.timTinhTrang(tinhTrang).heSo

        // Bước 3 – chọn phạm vi
        val cungThanhPho = dsMau.filter { it.thanhPho == thanhPho }
        val (mauDung, phamVi) = when {
            cungThanhPho.size >= SO_MAU_TOI_THIEU_THANH_PHO -> cungThanhPho to PhamVi.THANH_PHO
            dsMau.size >= SO_MAU_TOI_THIEU -> dsMau to PhamVi.TOAN_QUOC
            else -> return uocTinhKhauHao(tinhTrang, laSach, maDanhMucCon, giaGoc, namMua, namHienTai)
        }

        // Bước 2 – chuẩn hoá về giá "Như mới", gán trọng số
        data class Diem(val g: Double, val trongSo: Int, val daBan: Boolean)
        val diem = mauDung.map { Diem(it.gia / HangSo.timTinhTrang(it.tinhTrang).heSo, if (it.daBan) 2 else 1, it.daBan) }
            .sortedBy { it.g }

        // Bước 4 – loại ngoại lai bằng IQR
        val (q1, q3) = tuPhanVi(diem.map { it.g })
        val iqr = q3 - q1
        val giuLai = diem.filter { it.g >= q1 - 1.5 * iqr && it.g <= q3 + 1.5 * iqr }
        if (giuLai.size < SO_MAU_TOI_THIEU) {
            return uocTinhKhauHao(tinhTrang, laSach, maDanhMucCon, giaGoc, namMua, namHienTai)
        }

        // Bước 5 – trung vị có trọng số: cộng dồn trọng số đến khi đạt một nửa tổng
        val tongTrongSo = giuLai.sumOf { it.trongSo }
        var congDon = 0
        var trungViTrongSo = giuLai.first().g
        for (d in giuLai) {
            congDon += d.trongSo
            if (congDon * 2 >= tongTrongSo) {
                trungViTrongSo = d.g
                break
            }
        }
        val (p25, p75) = tuPhanVi(giuLai.map { it.g })
        val n = giuLai.size
        val soDaBan = giuLai.count { it.daBan }
        val noi = if (phamVi == PhamVi.THANH_PHO) thanhPho else "toàn quốc"
        return KetQua(
            giaGoiY = lamTron(trungViTrongSo * heSoDich),
            giaThap = lamTron(p25 * heSoDich),
            giaCao = lamTron(p75 * heSoDich),
            soMau = n,
            soDaBan = soDaBan,
            soBiLoai = diem.size - n,
            doTinCay = when {
                n >= 10 -> DoTinCay.CAO
                n >= 5 -> DoTinCay.TRUNG_BINH
                else -> DoTinCay.THAP
            },
            phamVi = phamVi,
            giaiThich = "Dựa trên $n tin tương tự ở $noi trong $SO_NGAY_LAY_MAU ngày ($soDaBan đã bán)" +
                if (diem.size > n) " · đã loại ${diem.size - n} giá bất thường" else ""
        )
    }

    /** Bước 6 – ước tính khấu hao khi không đủ tin tương tự. */
    fun uocTinhKhauHao(
        tinhTrang: String,
        laSach: Boolean,
        maDanhMucCon: String,
        giaGoc: Long?,
        namMua: Int?,
        namHienTai: Int
    ): KetQua {
        val tt = HangSo.timTinhTrang(tinhTrang)
        if (giaGoc == null || giaGoc <= 0) {
            return KetQua(giaiThich = "Chưa đủ dữ liệu để gợi ý. Nhập ${if (laSach) "giá bìa" else "giá mua gốc"} để nhận ước tính.")
        }
        if (laSach) {
            // Sách: giá bìa × tỷ lệ theo tình trạng (60/50/40/30%), khoảng ±10 điểm %
            val tyLe = tt.tyLeSach
            return KetQua(
                giaGoiY = lamTron(giaGoc * tyLe),
                giaThap = lamTron(giaGoc * (tyLe - 0.1)),
                giaCao = lamTron(giaGoc * (tyLe + 0.1)),
                doTinCay = DoTinCay.UOC_TINH,
                phamVi = PhamVi.KHAU_HAO,
                giaiThich = "Ước tính từ giá bìa ${DinhDang.gia(giaGoc)} × ${(tyLe * 100).roundToLong()}% " +
                    "(tình trạng ${tt.ten}). Chưa đủ tin tương tự để so sánh."
            )
        }
        // Đồ khác: giá gốc × phần còn lại sau khấu hao × hệ số tình trạng, khoảng ±15%
        val r = HangSo.timDanhMucCon(maDanhMucCon)?.khauHaoNam ?: 0.20
        var soNam = if (namMua == null) 1.0 else (namHienTai - namMua).toDouble()
        if (soNam <= 0) soNam = 0.5
        val conLai = max(1 - r * soNam, TY_LE_CON_LAI_TOI_THIEU)
        val uocTinh = giaGoc * conLai * tt.heSo
        val namChu = if (soNam % 1.0 == 0.0) soNam.toLong().toString() else soNam.toString().replace('.', ',')
        return KetQua(
            giaGoiY = lamTron(uocTinh),
            giaThap = lamTron(uocTinh * 0.85),
            giaCao = lamTron(uocTinh * 1.15),
            doTinCay = DoTinCay.UOC_TINH,
            phamVi = PhamVi.KHAU_HAO,
            giaiThich = "Giá gốc ${DinhDang.gia(giaGoc)}, dùng $namChu năm, khấu hao ${(r * 100).roundToLong()}%/năm, " +
                "tình trạng ${tt.ten}."
        )
    }

    // ===================== HÀM TOÁN HỌC PHỤ =====================

    fun trungVi(ds: List<Double>): Double {
        val n = ds.size
        val giua = n / 2
        return if (n % 2 == 1) ds[giua] else (ds[giua - 1] + ds[giua]) / 2
    }

    /** Tứ phân vị Q1, Q3 (danh sách đã sắp xếp): trung vị của nửa dưới và nửa trên, không tính phần tử giữa. */
    fun tuPhanVi(daSapXep: List<Double>): Pair<Double, Double> {
        val n = daSapXep.size
        if (n == 1) return daSapXep[0] to daSapXep[0]
        val giua = n / 2
        val nuaDuoi = daSapXep.subList(0, giua)
        val nuaTren = daSapXep.subList(if (n % 2 == 1) giua + 1 else giua, n)
        return trungVi(nuaDuoi) to trungVi(nuaTren)
    }

    /** Làm tròn giá: < 100.000đ → bội 1.000đ; đến 1 triệu → bội 5.000đ; trên 1 triệu → bội 50.000đ. */
    fun lamTron(gia: Double): Long {
        val buoc = when {
            gia < 100_000 -> 1_000L
            gia <= 1_000_000 -> 5_000L
            else -> 50_000L
        }
        // + 1e-9 để tránh sai số số thực (ví dụ 42.499,9999 phải thành 43.000 như 42.500)
        return (gia / buoc + 1e-9).roundToLong() * buoc
    }

    /** Giá người bán nhập lệch quá 50% so với gợi ý → nhắc (vẫn cho đăng). */
    fun canhBaoLech(giaNhap: Long, kq: KetQua): String? {
        if (!kq.coGoiY || giaNhap <= 0 || kq.giaGoiY <= 0) return null
        val lech = (giaNhap - kq.giaGoiY).toDouble() / kq.giaGoiY
        if (kotlin.math.abs(lech) <= 0.5) return null
        return "Giá của bạn ${if (lech > 0) "cao" else "thấp"} hơn nhiều so với gợi ý (${DinhDang.gia(kq.giaGoiY)}). Bạn vẫn có thể đăng."
    }
}

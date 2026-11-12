package com.unimarket.du_lieu.dulieu_mau

import androidx.room.withTransaction
import com.unimarket.du_lieu.database.AppDatabase
import com.unimarket.mo_hinh.BaoCao
import com.unimarket.mo_hinh.CuocTroChuyen
import com.unimarket.mo_hinh.LichHen
import com.unimarket.mo_hinh.NguoiDung
import com.unimarket.mo_hinh.ThongBao
import com.unimarket.mo_hinh.TinDang
import com.unimarket.mo_hinh.TinNhan
import com.unimarket.mo_hinh.YeuThich
import com.unimarket.tien_ich.DinhDang
import com.unimarket.tien_ich.HangSo
import com.unimarket.tien_ich.HangSo.MOT_GIO
import com.unimarket.tien_ich.HangSo.MOT_NGAY
import com.unimarket.tien_ich.MaHoa
import com.unimarket.xu_ly_nghiep_vu.QuyTacLichHen
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Dữ liệu mẫu nạp vào database ở LẦN CHẠY ĐẦU TIÊN (khi bảng nguoi_dung còn trống).
 * Nội dung lấy theo bản thiết kế unimarket-ui.html để demo và để thuật toán gợi ý giá, thống kê có số liệu.
 * Tất cả tài khoản mẫu dùng chung mật khẩu 123456.
 */
object DuLieuMau {

    const val MAT_KHAU_MAU = "123456"

    /** Các tài khoản hiện ở mục "Đăng nhập nhanh" trên màn Đăng nhập. */
    data class TaiKhoanNhanh(val nhan: String, val email: String)

    val DS_DANG_NHAP_NHANH = listOf(
        TaiKhoanNhanh("Lan · người bán", "lan@unimarket.vn"),
        TaiKhoanNhanh("Huy · người mua", "huy@unimarket.vn"),
        TaiKhoanNhanh("Vy · hồ sơ thiếu", "vy@unimarket.vn"),
        TaiKhoanNhanh("Quản trị viên", "admin@unimarket.vn")
    )

    private val khoa = Mutex()

    /** Nạp dữ liệu mẫu nếu database còn trống. Gọi nhiều lần cũng chỉ nạp một lần. */
    suspend fun napNeuTrong(db: AppDatabase) = khoa.withLock {
        if (db.nguoiDungDao().dem() > 0) return@withLock
        db.withTransaction { nap(db) }
    }

    // ------------------------------------------------------------------
    private suspend fun nap(db: AppDatabase) {
        val bayGio = System.currentTimeMillis()

        // ---------- 1. Người dùng ----------
        fun nd(email: String, hoTen: String, sdt: String, truong: String, diem: String, vaiTro: String = HangSo.VAI_TRO_NGUOI_DUNG) =
            NguoiDung(
                email = email,
                matKhauMaHoa = MaHoa.bamMatKhau(email, MAT_KHAU_MAU),
                hoTen = hoTen,
                soDienThoai = sdt,
                maTruong = truong,
                thanhPho = HangSo.timTruong(truong)?.thanhPho ?: "",
                diemGiaoDich = diem,
                vaiTro = vaiTro,
                ngayTao = bayGio - 60 * MOT_NGAY
            )

        val dsNguoiDung = listOf(
            nd("lan@unimarket.vn", "Nguyễn Thị Lan", "0912345678", "NEU", "Thư viện"),
            nd("huy@unimarket.vn", "Phạm Đức Huy", "0987654321", "NEU", "Cổng chính"),
            nd("admin@unimarket.vn", "Quản trị viên", "0900000000", "NEU", "Cổng chính", HangSo.VAI_TRO_QUAN_TRI),
            nd("vy@unimarket.vn", "Đinh Khánh Vy", "", "", ""),          // tài khoản mới, hồ sơ còn thiếu
            nd("minhanh@unimarket.vn", "Trần Minh Anh", "0911000001", "NEU", "Cổng chính"),
            nd("quanghuy@unimarket.vn", "Lê Quang Huy", "0911000002", "NEU", "Thư viện"),
            nd("trang@unimarket.vn", "Phạm Thu Trang", "0911000003", "NEU", "Sảnh giảng đường"),
            nd("nam@unimarket.vn", "Đỗ Văn Nam", "0911000004", "HUST", "Căng tin"),
            nd("my@unimarket.vn", "Ngô Hà My", "0911000005", "FTU", "Thư viện"),
            nd("hoa@unimarket.vn", "Vũ Thị Hoa", "0911000006", "NEU", "Căng tin"),
            nd("long@unimarket.vn", "Bùi Đức Long", "0911000007", "HUST", "Cổng chính"),
            nd("bao@unimarket.vn", "Huỳnh Gia Bảo", "0911000008", "UEH", "Cổng chính"),
            nd("linh@unimarket.vn", "Trương Mỹ Linh", "0911000009", "UEH", "Sảnh giảng đường"),
            nd("thanh@unimarket.vn", "Lâm Chí Thanh", "0911000010", "HCMUT", "Cổng chính"),
            nd("khoa@unimarket.vn", "Phan Minh Khoa", "0911000011", "DUE", "Thư viện"),
            nd("tung@unimarket.vn", "Nguyễn Văn Tùng", "0911000012", "NEU", "Căng tin"),
            nd("khai@unimarket.vn", "Hoàng Khải", "0911000013", "HUST", "Cổng chính"),
            nd("thao@unimarket.vn", "Lý Thảo", "0911000014", "FTU", "Căng tin")
        )
        val dsId = db.nguoiDungDao().themNhieu(dsNguoiDung)
        // Tra id theo phần trước @ của email cho dễ đọc: id("lan"), id("huy")...
        val theoEmail = dsNguoiDung.map { it.email.substringBefore('@') }.zip(dsId).toMap()
        fun id(ten: String) = theoEmail.getValue(ten)

        // ---------- 2. Tin đăng ----------
        fun tin(
            ban: String, tieuDe: String, maCon: String, gia: Long, tinhTrang: String, truong: String, diem: String,
            gioTruoc: Double, moTa: String = "", thuocTinh: String = "", giaGoc: Long? = null, namMua: Int? = null,
            trangThai: String = HangSo.TIN_DANG_BAN, giaBan: Long? = null, banCachDayNgay: Double? = null,
            mua: String? = null, luotXem: Int = 0, dayTin: Boolean = false, soLanGiaHan: Int = 0, soBaoCao: Int = 0
        ): TinDang {
            val ngayTao = bayGio - (gioTruoc * MOT_GIO).toLong()
            return TinDang(
                nguoiBanId = id(ban),
                tieuDe = tieuDe,
                moTa = moTa,
                danhMucCha = HangSo.timChaCua(maCon).ma,
                danhMucCon = maCon,
                gia = gia,
                giaGoc = giaGoc,
                namMua = namMua,
                tinhTrang = tinhTrang,
                thuocTinh = thuocTinh,
                maTruong = truong,
                thanhPho = HangSo.timTruong(truong)?.thanhPho ?: "",
                diemGiaoDich = diem,
                xacNhanDiaDiemLuc = ngayTao,
                trangThai = trangThai,
                giaBan = giaBan,
                ngayBan = banCachDayNgay?.let { bayGio - (it * MOT_NGAY).toLong() },
                nguoiMuaId = mua?.let { id(it) },
                luotXem = luotXem,
                soBaoCao = soBaoCao,
                soLanGiaHan = soLanGiaHan,
                soLanDayTin = if (dayTin) 1 else 0,
                doanhThuDayTin = if (dayTin) HangSo.PHI_DAY_TIN else 0,
                dayTinDen = if (dayTin) bayGio + 2 * MOT_NGAY else null,
                tuKhoa = DinhDang.boDau("$tieuDe $moTa"),
                ngayTao = ngayTao,
                ngayCapNhat = ngayTao,
                ngayHetHan = ngayTao + HangSo.SO_NGAY_HIEN_TIN * MOT_NGAY
            )
        }

        val ban = HangSo.TIN_DA_BAN
        val dsTin = listOf(
            // ===== 12 tin trong bản thiết kế =====
            tin("minhanh", "Giáo trình Kinh tế vi mô – NEU", "GIAO_TRINH", 45_000, "TOT", "NEU", "Cổng chính", 2.0,
                "Sách còn mới, có ghi chú bút chì vài trang đầu chương 2. Mình học xong học kỳ 1 nên pass lại.",
                "Môn học: Kinh tế vi mô\nNXB: ĐH Kinh tế Quốc dân, 2022\nGiá bìa: 80.000đ", giaGoc = 80_000, luotXem = 36),
            tin("quanghuy", "Laptop Dell Inspiron 14, Core i5, RAM 8GB", "LAPTOP", 7_650_000, "TOT", "NEU", "Thư viện", 20.0,
                "Máy dùng học văn phòng, pin còn khoảng 4 tiếng, vài vết xước nhẹ ở nắp. Tặng kèm túi chống sốc.",
                "Hãng: Dell\nModel: Inspiron 14 5420\nCPU / RAM: Core i5 / 8GB\nNăm mua: 2024\nBảo hành: Hết",
                giaGoc = 15_000_000, namMua = 2024, luotXem = 128, dayTin = true),
            tin("trang", "Bàn học gấp gọn kèm ghế nhựa", "NOI_THAT", 300_000, "TOT", "NEU", "Sảnh giảng đường", 30.0,
                "Bàn gấp được, hợp phòng trọ nhỏ.", "Chất liệu: Gỗ MDF, khung sắt\nKích thước: 80 × 50 cm",
                trangThai = HangSo.TIN_DANG_GIAO_DICH, luotXem = 54, dayTin = true),
            tin("nam", "Quạt cây Senko 3 cánh", "GIA_DUNG", 180_000, "TOT", "HUST", "Căng tin", 5.0,
                "Quạt chạy êm, đủ 3 số. Chuyển trọ nên bán lại.", "Hãng: Senko\nNăm mua: 2024", namMua = 2024, luotXem = 22),
            tin("my", "Máy tính Casio FX-580VN X", "MAY_TINH_CAM_TAY", 350_000, "NHU_MOI", "FTU", "Thư viện", 24.0,
                "Mua đầu năm, dùng rất ít, còn hộp và sách hướng dẫn.", "Hãng: Casio\nModel: FX-580VN X", luotXem = 41),
            tin("hoa", "Giáo trình Nguyên lý kế toán", "GIAO_TRINH", 35_000, "KHA", "NEU", "Căng tin", 50.0,
                "Có gạch chân bút dạ quang, không rách trang.", "Môn học: Nguyên lý kế toán\nGiá bìa: 75.000đ",
                giaGoc = 75_000, luotXem = 17),
            tin("long", "Nồi cơm điện Sharp 1,8 lít", "GIA_DUNG", 250_000, "KHA", "HUST", "Cổng chính", 72.0,
                "Nấu vẫn ngon, lòng nồi hơi tróc chống dính ở đáy.", "Hãng: Sharp\nNăm mua: 2023", namMua = 2023, luotXem = 29),
            tin("my", "Bộ sách IELTS Cambridge 15–18", "THAM_KHAO", 120_000, "TOT", "FTU", "Thư viện", 96.0,
                "Bản in màu, chưa viết vào phần bài tập.", "Số cuốn: 4\nGiá bìa: 480.000đ", giaGoc = 480_000, luotXem = 63),
            tin("bao", "Giáo trình Kinh tế vĩ mô (Mankiw, bản dịch)", "GIAO_TRINH", 60_000, "NHU_MOI", "UEH", "Cổng chính", 8.0,
                "Sách gần như mới.", "Môn học: Kinh tế vĩ mô\nGiá bìa: 120.000đ", giaGoc = 120_000, luotXem = 12),
            tin("linh", "Tai nghe Sony WH-CH520", "PHU_KIEN", 650_000, "TOT", "UEH", "Sảnh giảng đường", 12.0,
                "Pin trâu, đệm tai còn đẹp.", "Hãng: Sony\nNăm mua: 2025", namMua = 2025, luotXem = 38, dayTin = true),
            tin("thanh", "Xe đạp Thống Nhất 26 inch", "XE_DAP", 900_000, "KHA", "HCMUT", "Cổng chính", 40.0,
                "Xe đi êm, vừa thay lốp sau.", "Hãng: Thống Nhất\nLoại xe: Đường phố", luotXem = 44),
            tin("khoa", "iPhone 11 64GB, pin 82%", "DIEN_THOAI", 4_200_000, "TOT", "DUE", "Thư viện", 16.0,
                "Máy zin, chưa sửa chữa, trầy nhẹ viền.", "Hãng: Apple\nModel: iPhone 11\nDung lượng: 64GB", luotXem = 90),

            // ===== Tin của Lan (màn "Tin của tôi") =====
            tin("lan", "Sách Kinh tế lượng (Nguyễn Quang Dong)", "GIAO_TRINH", 50_000, "TOT", "NEU", "Thư viện", 3 * 24.0,
                "Sách sạch, không ghi chú. Kèm đề cương ôn tập.", "Môn học: Kinh tế lượng\nGiá bìa: 95.000đ",
                giaGoc = 95_000, luotXem = 36),
            tin("lan", "Quạt bàn Asia 16 inch", "GIA_DUNG", 120_000, "TOT", "NEU", "Căng tin", 18 * 24.0,
                "Quạt chạy êm, có hẹn giờ.", "Hãng: Asia\nNăm mua: 2024", namMua = 2024, luotXem = 18),
            tin("lan", "Kệ sách gỗ 3 tầng", "NOI_THAT", 300_000, "TOT", "NEU", "Cổng chính", 10 * 24.0,
                "Kệ chắc chắn, tháo lắp dễ.", "Chất liệu: Gỗ thông", trangThai = HangSo.TIN_DANG_GIAO_DICH, luotXem = 54),
            tin("lan", "Giáo trình Toán cao cấp 1", "GIAO_TRINH", 30_000, "KHA", "NEU", "Cổng chính", 20 * 24.0,
                "Có ghi chú bút chì.", "Môn học: Toán cao cấp 1", trangThai = ban, giaBan = 28_000, banCachDayNgay = 9.0,
                mua = "huy", luotXem = 25),
            tin("lan", "Casio FX-570ES Plus", "MAY_TINH_CAM_TAY", 200_000, "TOT", "NEU", "Thư viện", 32 * 24.0,
                "Máy còn tốt, đủ chức năng.", "Hãng: Casio", trangThai = HangSo.TIN_HET_HAN, soLanGiaHan = 1, luotXem = 30),

            // ===== Mẫu giá "Giáo trình Kinh tế vi mô" ở Hà Nội (ví dụ 4.6.4 trong tài liệu thiết kế) =====
            tin("nam", "Giáo trình Kinh tế vi mô", "GIAO_TRINH", 45_000, "TOT", "HUST", "Thư viện", 40 * 24.0,
                "Sách học kỳ trước.", trangThai = ban, giaBan = 40_000, banCachDayNgay = 35.0, mua = "huy"),
            tin("my", "Kinh tế vi mô – giáo trình chuẩn", "GIAO_TRINH", 50_000, "NHU_MOI", "FTU", "Căng tin", 6 * 24.0,
                "Mới mua, chưa dùng.", luotXem = 9),
            tin("hoa", "Giáo trình Kinh tế vi mô bản cũ", "GIAO_TRINH", 40_000, "KHA", "NEU", "Căng tin", 55 * 24.0,
                "Có gạch chân.", trangThai = ban, giaBan = 35_000, banCachDayNgay = 50.0, mua = "lan"),
            tin("long", "Giáo trình Kinh tế vi mô, sách cũ", "GIAO_TRINH", 30_000, "CU", "HUST", "Cổng chính", 70 * 24.0,
                "Bìa sờn, đủ trang.", trangThai = ban, giaBan = 30_000, banCachDayNgay = 65.0, mua = "huy"),
            tin("trang", "Giáo trình Kinh tế vi mô (bản mới nhất)", "GIAO_TRINH", 60_000, "NHU_MOI", "NEU", "Thư viện", 25 * 24.0,
                "Như mới.", trangThai = ban, giaBan = 55_000, banCachDayNgay = 20.0, mua = "hoa"),
            tin("khai", "Combo giáo trình Kinh tế vi mô + sách bài tập", "GIAO_TRINH", 150_000, "TOT", "HUST", "Căng tin", 4 * 24.0,
                "Combo 2 cuốn kèm đề cương.", luotXem = 15),

            // ===== Thêm tin đã bán / đang bán để thống kê có số liệu =====
            tin("long", "Chuột Logitech M331 không dây", "PHU_KIEN", 150_000, "TOT", "HUST", "Cổng chính", 12 * 24.0,
                "Bấm êm, pin AA.", "Hãng: Logitech", trangThai = ban, giaBan = 140_000, banCachDayNgay = 2.0, mua = "nam"),
            tin("minhanh", "Sách Tư duy nhanh và chậm", "TRUYEN", 70_000, "TOT", "NEU", "Cổng chính", 15 * 24.0,
                "Sách gốc NXB Thế giới.", giaGoc = 159_000, trangThai = ban, giaBan = 65_000, banCachDayNgay = 1.0, mua = "lan"),
            tin("thao", "Áo hoodie UniClub size L", "THOI_TRANG", 120_000, "KHA", "FTU", "Căng tin", 9 * 24.0,
                "Mặc vài lần, không xù lông.", trangThai = ban, giaBan = 100_000, banCachDayNgay = 3.0, mua = "my"),
            tin("hoa", "Nồi lẩu điện mini 1,5L", "GIA_DUNG", 160_000, "TOT", "NEU", "Căng tin", 20 * 24.0,
                "Dùng 1 học kỳ.", trangThai = ban, giaBan = 150_000, banCachDayNgay = 5.0, mua = "huy"),
            tin("linh", "Bàn phím cơ Akko 3068", "PHU_KIEN", 650_000, "TOT", "UEH", "Sảnh giảng đường", 18 * 24.0,
                "Switch Akko Pink, đủ keycap.", trangThai = ban, giaBan = 600_000, banCachDayNgay = 4.0, mua = "bao"),
            tin("minhanh", "Giáo trình Marketing căn bản", "GIAO_TRINH", 40_000, "KHA", "NEU", "Thư viện", 25 * 24.0,
                "Có ghi chú.", giaGoc = 85_000, trangThai = ban, giaBan = 35_000, banCachDayNgay = 6.0, mua = "lan"),
            tin("khoa", "Xe đạp Asama mini", "XE_DAP", 1_200_000, "TOT", "DUE", "Cổng chính", 30 * 24.0,
                "Xe nhẹ, phanh tốt.", trangThai = ban, giaBan = 1_100_000, banCachDayNgay = 10.0, mua = "khai"),
            tin("thanh", "Samsung Galaxy A52 128GB", "DIEN_THOAI", 3_500_000, "TOT", "HCMUT", "Thư viện", 48.0,
                "Máy đẹp, pin tốt, kèm ốp.", "Hãng: Samsung\nModel: Galaxy A52", giaGoc = 8_000_000, namMua = 2022, luotXem = 33),
            tin("khai", "Ghế xoay văn phòng", "NOI_THAT", 450_000, "KHA", "HUST", "Sảnh giảng đường", 96.0,
                "Ghế còn chắc, đệm hơi xẹp.", luotXem = 14),
            tin("huy", "Từ điển Oxford Advanced Learner's", "THAM_KHAO", 150_000, "TOT", "NEU", "Cổng chính", 26.0,
                "Bản 10th, có đĩa.", giaGoc = 450_000, luotXem = 7),
            tin("trang", "Đèn bàn học LED chống cận", "KHAC", 90_000, "NHU_MOI", "NEU", "Sảnh giảng đường", 10.0,
                "3 chế độ sáng, sạc USB.", luotXem = 5),
            tin("my", "MacBook Air M1 2020 8/256", "LAPTOP", 13_500_000, "TOT", "FTU", "Thư viện", 72.0,
                "Pin 89%, không cấn móp.", "Hãng: Apple\nModel: MacBook Air M1\nNăm mua: 2021",
                giaGoc = 22_000_000, namMua = 2021, luotXem = 70),

            // ===== Tin bị báo cáo (màn kiểm duyệt của quản trị) =====
            tin("tung", "Bán đề thi cuối kỳ Kinh tế lượng có đáp án", "GIAO_TRINH", 100_000, "TOT", "NEU", "Căng tin", 30.0,
                "Đề thi các năm kèm đáp án.", trangThai = HangSo.TIN_BI_AN, soBaoCao = 3, luotXem = 40),
            tin("khai", "iPhone 13 Pro Max giá 2 triệu, cần cọc trước", "DIEN_THOAI", 2_000_000, "NHU_MOI", "HUST", "Cổng chính", 14.0,
                "Chuyển khoản cọc trước 500k để giữ máy.", soBaoCao = 2, luotXem = 60),
            tin("thao", "Áo khoác đồng phục khoa", "KHAC", 80_000, "TOT", "FTU", "Căng tin", 60.0,
                "Size M.", soBaoCao = 1, luotXem = 11)
        )
        val idTin = db.tinDangDao().themNhieu(dsTin)
        fun tinCo(tieuDe: String) = idTin[dsTin.indexOfFirst { it.tieuDe == tieuDe }]

        // ---------- 3. Yêu thích ----------
        db.yeuThichDao().themNhieu(
            listOf(
                YeuThich(id("lan"), tinCo("Máy tính Casio FX-580VN X")),
                YeuThich(id("lan"), tinCo("Laptop Dell Inspiron 14, Core i5, RAM 8GB")),
                YeuThich(id("huy"), tinCo("Giáo trình Kinh tế vi mô – NEU"))
            )
        )

        // ---------- 4. Báo cáo vi phạm ----------
        val deThi = tinCo("Bán đề thi cuối kỳ Kinh tế lượng có đáp án")
        val iphone = tinCo("iPhone 13 Pro Max giá 2 triệu, cần cọc trước")
        val aoKhoac = tinCo("Áo khoác đồng phục khoa")
        db.baoCaoDao().themNhieu(
            listOf(
                BaoCao(tinDangId = deThi, nguoiBaoCaoId = id("lan"), lyDo = "Hàng cấm", ngayTao = bayGio - 20 * MOT_GIO),
                BaoCao(tinDangId = deThi, nguoiBaoCaoId = id("huy"), lyDo = "Hàng cấm", ngayTao = bayGio - 18 * MOT_GIO),
                BaoCao(tinDangId = deThi, nguoiBaoCaoId = id("hoa"), lyDo = "Thông tin sai sự thật", ngayTao = bayGio - 6 * MOT_GIO),
                BaoCao(tinDangId = iphone, nguoiBaoCaoId = id("huy"), lyDo = "Lừa đảo", ghiChu = "Đòi chuyển cọc trước", ngayTao = bayGio - 10 * MOT_GIO),
                BaoCao(tinDangId = iphone, nguoiBaoCaoId = id("nam"), lyDo = "Lừa đảo", ngayTao = bayGio - 4 * MOT_GIO),
                BaoCao(tinDangId = aoKhoac, nguoiBaoCaoId = id("lan"), lyDo = "Sai danh mục", ngayTao = bayGio - 30 * MOT_GIO)
            )
        )

        // ---------- 5. Phòng chat, lịch hẹn, đánh giá ----------
        napGiaoDich(db, bayGio, dsTin, idTin, ::id, ::tinCo)
    }

    /**
     * Giao dịch mẫu:
     * (a) Mỗi tin ĐÃ BÁN có người mua → một phòng chat + lịch HOAN_TAT + đánh giá (người bán có điểm sao thật).
     * (b) 4 cuộc trao đổi đang diễn ra ở các trạng thái khác nhau để demo tab Tin nhắn và thanh ghim.
     */
    private suspend fun napGiaoDich(
        db: AppDatabase, bayGio: Long, dsTin: List<TinDang>, idTin: List<Long>,
        id: (String) -> Long, tinCo: (String) -> Long
    ) {
        val cuocDao = db.cuocTroChuyenDao()
        val lichDao = db.lichHenDao()
        val tinNhanDao = db.tinNhanDao()
        fun luc(gioTruoc: Double) = bayGio - (gioTruoc * MOT_GIO).toLong()
        fun chu(cuoc: Long, nguoi: Long, noiDung: String, thoiGian: Long) =
            TinNhan(cuocTroChuyenId = cuoc, nguoiGuiId = nguoi, noiDung = noiDung, thoiGian = thoiGian)
        fun suKien(cuoc: Long, nguoi: Long, loai: String, cumTu: String, thoiGian: Long, lich: LichHen? = null) =
            TinNhan(
                cuocTroChuyenId = cuoc, nguoiGuiId = nguoi, loai = HangSo.TIN_NHAN_SU_KIEN, noiDung = cumTu, suKien = loai,
                diaDiem = lich?.diaDiem, thoiGianHen = lich?.thoiGianHen, thoiGian = thoiGian
            )
        fun sao(n: Int) = "★".repeat(n) + "☆".repeat(5 - n)

        // (a) Tin đã bán → đánh giá
        val dsSao = listOf(5, 4, 5, 5, 4, 5)
        val dsNhanXet = listOf(
            "Người bán nhiệt tình, đồ đúng mô tả", "Đúng hẹn, hàng như ảnh", "Giao dịch nhanh gọn",
            "Hàng ok, giá hợp lý", "Bạn bán dễ thương, sẽ ủng hộ tiếp", ""
        )
        var thuTu = 0
        dsTin.forEachIndexed { i, t ->
            val nguoiMua = t.nguoiMuaId ?: return@forEachIndexed
            val ngayBan = t.ngayBan ?: return@forEachIndexed
            val soSao = dsSao[thuTu % dsSao.size]
            val nhanXet = dsNhanXet[thuTu % dsNhanXet.size]
            thuTu++
            val gia = DinhDang.gia(t.giaBan ?: t.gia)
            val cuoc = cuocDao.them(
                CuocTroChuyen(
                    tinDangId = idTin[i], nguoiMuaId = nguoiMua, nguoiBanId = t.nguoiBanId,
                    tinNhanCuoi = "Đánh giá ${sao(soSao)}", capNhatLuc = ngayBan + MOT_GIO
                )
            )
            val lich = LichHen(
                cuocTroChuyenId = cuoc, tinDangId = idTin[i], nguoiMuaId = nguoiMua, nguoiBanId = t.nguoiBanId,
                maTruong = t.maTruong, diaDiem = t.diemGiaoDich, thoiGianHen = ngayBan,
                trangThai = HangSo.LICH_HOAN_TAT, nguoiDeXuatId = nguoiMua, soSao = soSao, nhanXet = nhanXet.ifEmpty { null },
                ngayTao = ngayBan - MOT_NGAY, ngayChot = ngayBan - MOT_NGAY + MOT_GIO, hetHanLuc = ngayBan
            )
            lichDao.them(lich)
            val danhGia = "đánh giá người bán ${sao(soSao)}" + if (nhanXet.isNotEmpty()) " · “$nhanXet”" else ""
            tinNhanDao.themNhieu(
                listOf(
                    chu(cuoc, nguoiMua, "Chào bạn, món này còn không?", ngayBan - MOT_NGAY - MOT_GIO),
                    chu(cuoc, t.nguoiBanId, "Còn bạn nhé", ngayBan - MOT_NGAY - 30 * 60_000),
                    suKien(cuoc, nguoiMua, HangSo.SK_DAT_MUA, "gửi yêu cầu mua", ngayBan - MOT_NGAY, lich),
                    suKien(cuoc, t.nguoiBanId, HangSo.SK_CHOT_HEN, "xác nhận · lịch hẹn đã chốt", ngayBan - MOT_NGAY + MOT_GIO, lich),
                    suKien(cuoc, t.nguoiBanId, HangSo.SK_DA_BAN, "đánh dấu đã bán · $gia", ngayBan),
                    suKien(cuoc, nguoiMua, HangSo.SK_DANH_GIA, danhGia, ngayBan + MOT_GIO)
                )
            )
        }

        // (b) Các cuộc trao đổi đang diễn ra; giờ hẹn lấy theo gợi ý "Mai 11:30", "Mai 17:00"
        val goiY = QuyTacLichHen.goiYGioHen(bayGio).toMap()
        val mai1130 = goiY.getValue("Mai 11:30")
        val mai1700 = goiY.getValue("Mai 17:00")

        // 1. Huy hỏi mua sách Kinh tế lượng của Lan → CHỜ LAN XÁC NHẬN
        val sachKTL = tinCo("Sách Kinh tế lượng (Nguyễn Quang Dong)")
        val c1 = cuocDao.them(
            CuocTroChuyen(
                tinDangId = sachKTL, nguoiMuaId = id("huy"), nguoiBanId = id("lan"),
                tinNhanCuoi = "Mình lấy luôn nếu sách ok", chuaDocNguoiBan = 2, capNhatLuc = luc(0.08)
            )
        )
        val l1 = LichHen(
            cuocTroChuyenId = c1, tinDangId = sachKTL, nguoiMuaId = id("huy"), nguoiBanId = id("lan"), maTruong = "NEU",
            diaDiem = "Thư viện", thoiGianHen = mai1130, loiNhan = "Mình lấy luôn nếu sách ok",
            nguoiDeXuatId = id("huy"), ngayTao = luc(0.1), hetHanLuc = luc(0.1) + MOT_NGAY
        )
        lichDao.them(l1)
        tinNhanDao.themNhieu(
            listOf(
                chu(c1, id("huy"), "Chào bạn, sách còn không?", luc(5.0)),
                chu(c1, id("lan"), "Còn bạn nhé, sách mình giữ kỹ lắm", luc(4.5)),
                suKien(c1, id("huy"), HangSo.SK_DAT_MUA, "gửi yêu cầu mua", luc(0.1), l1),
                chu(c1, id("huy"), "Mình lấy luôn nếu sách ok", luc(0.08))
            )
        )

        // 2. Huy chỉ nhắn tin trả giá giáo trình Vi mô của Minh Anh → CHƯA ĐẶT MUA
        val viMo = tinCo("Giáo trình Kinh tế vi mô – NEU")
        val c2 = cuocDao.them(
            CuocTroChuyen(
                tinDangId = viMo, nguoiMuaId = id("huy"), nguoiBanId = id("minhanh"),
                tinNhanCuoi = "Mình để 43k nhé, sách còn mới lắm", chuaDocNguoiMua = 1, capNhatLuc = luc(2.0)
            )
        )
        tinNhanDao.themNhieu(
            listOf(
                chu(c2, id("huy"), "Bớt còn 40k được không bạn?", luc(3.0)),
                chu(c2, id("minhanh"), "Mình để 43k nhé, sách còn mới lắm", luc(2.0))
            )
        )

        // 3. Lan mua Casio của My → ĐÃ CHỐT HẸN, tin chuyển "Đang giao dịch"
        val casio = tinCo("Máy tính Casio FX-580VN X")
        val c3 = cuocDao.them(
            CuocTroChuyen(
                tinDangId = casio, nguoiMuaId = id("lan"), nguoiBanId = id("my"),
                tinNhanCuoi = "Ok bạn, 5h chiều mai mình ở thư viện tầng 1", capNhatLuc = luc(18.0)
            )
        )
        val l3 = LichHen(
            cuocTroChuyenId = c3, tinDangId = casio, nguoiMuaId = id("lan"), nguoiBanId = id("my"), maTruong = "FTU",
            diaDiem = "Thư viện", thoiGianHen = mai1700, trangThai = HangSo.LICH_DA_CHOT,
            nguoiDeXuatId = id("lan"), ngayTao = luc(20.0), ngayChot = luc(19.0), hetHanLuc = luc(20.0) + MOT_NGAY
        )
        lichDao.them(l3)
        tinNhanDao.themNhieu(
            listOf(
                chu(c3, id("lan"), "Máy còn bảo hành không bạn?", luc(21.0)),
                chu(c3, id("my"), "Còn 6 tháng nhé, đủ hộp", luc(20.5)),
                suKien(c3, id("lan"), HangSo.SK_DAT_MUA, "gửi yêu cầu mua", luc(20.0), l3),
                suKien(c3, id("my"), HangSo.SK_CHOT_HEN, "xác nhận · lịch hẹn đã chốt", luc(19.0), l3),
                chu(c3, id("my"), "Ok bạn, 5h chiều mai mình ở thư viện tầng 1", luc(18.0))
            )
        )
        db.tinDangDao().layTheoId(casio)?.let { db.tinDangDao().capNhat(it.copy(trangThai = HangSo.TIN_DANG_GIAO_DICH)) }

        // 4. Trang hỏi quạt của Lan → CHƯA ĐẶT MUA, Lan chưa đọc
        val quat = tinCo("Quạt bàn Asia 16 inch")
        val c4 = cuocDao.them(
            CuocTroChuyen(
                tinDangId = quat, nguoiMuaId = id("trang"), nguoiBanId = id("lan"),
                tinNhanCuoi = "Quạt còn chạy êm không bạn?", chuaDocNguoiBan = 1, capNhatLuc = luc(1.0)
            )
        )
        tinNhanDao.them(chu(c4, id("trang"), "Quạt còn chạy êm không bạn?", luc(1.0)))

        // Thông báo đi kèm (trung tâm thông báo làm ở Sprint 5)
        val tb = db.thongBaoDao()
        tb.them(
            ThongBao(
                nguoiNhanId = id("lan"), loai = "DAT_MUA", tieuDe = "Có người muốn mua",
                noiDung = "Phạm Đức Huy muốn mua “Sách Kinh tế lượng (Nguyễn Quang Dong)” · Thư viện, ${DinhDang.gioHen(mai1130, bayGio)}",
                loaiLienKet = "CHAT", lienKetId = c1, daDay = true, thoiGian = luc(0.1)
            )
        )
        for (nguoi in listOf(id("lan"), id("my"))) {
            tb.them(
                ThongBao(
                    nguoiNhanId = nguoi, loai = "CHOT_HEN", tieuDe = "Đã chốt hẹn",
                    noiDung = "Thư viện, ${DinhDang.gioHen(mai1700, bayGio)} · “Máy tính Casio FX-580VN X”",
                    loaiLienKet = "CHAT", lienKetId = c3, daDay = true, daDoc = nguoi == id("my"), thoiGian = luc(19.0)
                )
            )
        }
    }
}


package com.unimarket.du_lieu.repository

import android.content.Context
import androidx.room.withTransaction
import com.unimarket.du_lieu.database.AppDatabase
import com.unimarket.mo_hinh.CuocTroChuyen
import com.unimarket.mo_hinh.LichHen
import com.unimarket.mo_hinh.NguoiDung
import com.unimarket.mo_hinh.ThongBao
import com.unimarket.mo_hinh.TinDang
import com.unimarket.mo_hinh.TinNhan
import com.unimarket.tien_ich.DinhDang
import com.unimarket.tien_ich.HangSo
import com.unimarket.xu_ly_nghiep_vu.LichNhac
import com.unimarket.xu_ly_nghiep_vu.QuyTacLichHen

/**
 * Xử lý GIAO DỊCH giữa người mua và người bán: phòng chat, tin nhắn, đặt mua, xác nhận / đổi / huỷ lịch hẹn,
 * đã bán, đánh giá.
 *
 * Mỗi thao tác lên lịch hẹn chạy trong MỘT transaction gồm 3 việc (BR-39, BR-41):
 *   1. đổi bảng lich_hen (và tin_dang nếu cần)
 *   2. thêm một thẻ sự kiện vào tin_nhan (không sửa/xoá được, dùng làm bằng chứng)
 *   3. thêm thông báo vào thong_bao cho đúng người liên quan
 * Lỗi nghiệp vụ ném ra bằng error("câu tiếng Việt") để màn hình hiển thị.
 */
class GiaoDichRepository(context: Context) {

    private val db = AppDatabase.layDb(context)
    private val cuocDao = db.cuocTroChuyenDao()
    private val tinNhanDao = db.tinNhanDao()
    private val lichDao = db.lichHenDao()
    private val tinDao = db.tinDangDao()
    private val ndDao = db.nguoiDungDao()
    private val tbDao = db.thongBaoDao()

    /** Toàn bộ dữ liệu để vẽ một phòng chat. */
    data class PhongChat(
        val cuoc: CuocTroChuyen,
        val tin: TinDang,
        val nguoiMua: NguoiDung,
        val nguoiBan: NguoiDung,
        val lich: LichHen?,                 // lịch hẹn mới nhất của phòng (null = chưa đặt mua)
        val dsTinNhan: List<TinNhan>,
        val laNguoiMua: Boolean             // người đang xem là người mua?
    ) {
        val doiPhuong get() = if (laNguoiMua) nguoiBan else nguoiMua
    }

    /** Một dòng ở tab Tin nhắn. */
    data class DongCuoc(
        val cuoc: CuocTroChuyen,
        val tin: TinDang,
        val doiPhuong: NguoiDung,
        val lich: LichHen?,
        val laNguoiMua: Boolean,
        val chuaDoc: Int
    )

    /** Người có thể là người mua khi người bán bấm "Đã bán". */
    data class UngVienNguoiMua(val id: Long, val hoTen: String, val ghiChu: String, val daChot: Boolean)

    // ===================== PHÒNG CHAT =====================

    /** Mở phòng chat của người mua với một tin (tạo mới nếu chưa có). Trả về id phòng. */
    suspend fun moPhong(tinId: Long, nguoiMuaId: Long): Long = db.withTransaction {
        val tin = tinDao.layTheoId(tinId) ?: error("Tin không còn tồn tại.")
        if (tin.nguoiBanId == nguoiMuaId) error("Bạn không thể chat với tin của chính mình.") // BR-31
        cuocDao.timPhong(tinId, nguoiMuaId)?.id ?: run {
            if (!QuyTacLichHen.tinConNhanDatMua(tin)) error("Tin này không còn bán.")
            cuocDao.them(CuocTroChuyen(tinDangId = tinId, nguoiMuaId = nguoiMuaId, nguoiBanId = tin.nguoiBanId))
        }
    }

    /** Đọc phòng chat; trả về null nếu không tồn tại hoặc người xem không thuộc phòng này. */
    suspend fun layPhong(cuocId: Long, nguoiDungId: Long): PhongChat? {
        val cuoc = cuocDao.layTheoId(cuocId) ?: return null
        if (nguoiDungId != cuoc.nguoiMuaId && nguoiDungId != cuoc.nguoiBanId) return null
        val tin = tinDao.layTheoId(cuoc.tinDangId) ?: return null
        val nguoiMua = ndDao.layTheoId(cuoc.nguoiMuaId) ?: return null
        val nguoiBan = ndDao.layTheoId(cuoc.nguoiBanId) ?: return null
        return PhongChat(
            cuoc, tin, nguoiMua, nguoiBan,
            lichDao.layMoiNhatCuaCuoc(cuocId),
            tinNhanDao.layTheoCuoc(cuocId),
            laNguoiMua = nguoiDungId == cuoc.nguoiMuaId
        )
    }

    /** Mở phòng thì coi như đã đọc hết tin nhắn của bên kia. */
    suspend fun danhDauDaDoc(cuocId: Long, nguoiDungId: Long) {
        val cuoc = cuocDao.layTheoId(cuocId) ?: return
        val moi = if (nguoiDungId == cuoc.nguoiMuaId) cuoc.copy(chuaDocNguoiMua = 0) else cuoc.copy(chuaDocNguoiBan = 0)
        if (moi != cuoc) cuocDao.capNhat(moi)
    }

    suspend fun guiTinNhan(cuocId: Long, nguoiGuiId: Long, noiDung: String) {
        val chu = noiDung.trim()
        if (chu.isEmpty()) return
        db.withTransaction {
            ghiTinNhan(cuocId, TinNhan(cuocTroChuyenId = cuocId, nguoiGuiId = nguoiGuiId, noiDung = chu), chu)
        }
    }

    /** Danh sách cuộc trò chuyện cho tab Tin nhắn. */
    suspend fun layDsCuoc(nguoiDungId: Long): List<DongCuoc> =
        cuocDao.layCuaNguoiDung(nguoiDungId).mapNotNull { c ->
            val laNguoiMua = c.nguoiMuaId == nguoiDungId
            val tin = tinDao.layTheoId(c.tinDangId) ?: return@mapNotNull null
            val doiPhuong = ndDao.layTheoId(if (laNguoiMua) c.nguoiBanId else c.nguoiMuaId) ?: return@mapNotNull null
            DongCuoc(
                c, tin, doiPhuong, lichDao.layMoiNhatCuaCuoc(c.id), laNguoiMua,
                if (laNguoiMua) c.chuaDocNguoiMua else c.chuaDocNguoiBan
            )
        }

    suspend fun tongChuaDoc(nguoiDungId: Long): Int = cuocDao.tongChuaDoc(nguoiDungId)

    /** Số người đang hỏi mua từng tin (dòng "N người đang hỏi mua" ở Tin của tôi). */
    suspend fun demHoiMua(dsTin: List<TinDang>): Map<Long, Int> =
        dsTin.associate { it.id to cuocDao.demTheoTin(it.id) }

    /** Phòng chat và lịch hẹn hiện tại của một người mua với một tin (cho nút ở màn Chi tiết). */
    suspend fun lichCuaNguoiMua(tinId: Long, nguoiMuaId: Long): Pair<Long?, LichHen?> {
        val cuoc = cuocDao.timPhong(tinId, nguoiMuaId) ?: return null to null
        return cuoc.id to lichDao.layMoiNhatCuaCuoc(cuoc.id)
    }

    // ===================== ĐẶT MUA (UC-34) =====================

    /** Người mua gửi yêu cầu mua kèm địa điểm, giờ hẹn. Trả về id phòng chat để mở ngay. */
    suspend fun datMua(tinId: Long, nguoiMuaId: Long, diaDiem: String, thoiGianHen: Long, loiNhan: String): Long =
        db.withTransaction {
            val bayGio = System.currentTimeMillis()
            val tin = tinDao.layTheoId(tinId) ?: error("Tin không còn tồn tại.")
            if (tin.nguoiBanId == nguoiMuaId) error("Bạn không thể đặt mua tin của chính mình.")
            if (!QuyTacLichHen.tinConNhanDatMua(tin)) error("Tin này không còn nhận đặt mua.")
            QuyTacLichHen.loiDiaDiem(diaDiem)?.let { error(it) }
            QuyTacLichHen.loiGioHen(thoiGianHen, bayGio)?.let { error(it) }

            val cuocId = cuocDao.timPhong(tinId, nguoiMuaId)?.id
                ?: cuocDao.them(CuocTroChuyen(tinDangId = tinId, nguoiMuaId = nguoiMuaId, nguoiBanId = tin.nguoiBanId))
            // BR-33: 1 yêu cầu đang hoạt động cho mỗi tin, tối đa 5 yêu cầu đang chờ
            if (QuyTacLichHen.dangHoatDong(lichDao.layMoiNhatCuaCuoc(cuocId))) {
                error("Bạn đã có một yêu cầu đang hoạt động cho tin này.")
            }
            if (lichDao.demChoCuaNguoiMua(nguoiMuaId) >= HangSo.SO_YEU_CAU_CHO_TOI_DA) {
                error("Bạn đang có ${HangSo.SO_YEU_CAU_CHO_TOI_DA} yêu cầu mua chờ xác nhận. Hãy huỷ bớt trước khi đặt thêm.")
            }

            val lich = LichHen(
                cuocTroChuyenId = cuocId, tinDangId = tinId, nguoiMuaId = nguoiMuaId, nguoiBanId = tin.nguoiBanId,
                maTruong = tin.maTruong, diaDiem = diaDiem.trim(), thoiGianHen = thoiGianHen, loiNhan = loiNhan.trim(),
                nguoiDeXuatId = nguoiMuaId, ngayTao = bayGio, hetHanLuc = bayGio + HangSo.MOT_NGAY
            )
            lichDao.them(lich)
            ghiSuKien(cuocId, nguoiMuaId, HangSo.SK_DAT_MUA, "gửi yêu cầu mua", "Yêu cầu mua · ${hen(lich)}", lich)
            if (lich.loiNhan.isNotEmpty()) {
                ghiTinNhan(cuocId, TinNhan(cuocTroChuyenId = cuocId, nguoiGuiId = nguoiMuaId, noiDung = lich.loiNhan), lich.loiNhan)
            }
            val nguoiMua = ndDao.layTheoId(nguoiMuaId)
            thongBao(tin.nguoiBanId, "DAT_MUA", "Có người muốn mua", "${nguoiMua?.hoTen} muốn mua “${tin.tieuDe}” · ${hen(lich)}", cuocId)
            cuocId
        }

    /** Người bán mời người mua (đang chỉ chat) bấm Đặt mua. */
    suspend fun moiDatMua(cuocId: Long, nguoiBanId: Long) = db.withTransaction {
        val cuoc = cuocDao.layTheoId(cuocId) ?: error("Không tìm thấy cuộc trò chuyện.")
        if (cuoc.nguoiBanId != nguoiBanId) error("Chỉ người bán mới gửi được lời mời.")
        if (QuyTacLichHen.dangHoatDong(lichDao.layMoiNhatCuaCuoc(cuocId))) error("Người mua đã có lịch hẹn đang hoạt động.")
        val tin = tinDao.layTheoId(cuoc.tinDangId) ?: error("Tin không còn tồn tại.")
        ghiSuKien(cuocId, nguoiBanId, HangSo.SK_MOI_DAT_MUA, "gửi lời mời đặt mua", "Lời mời đặt mua", null)
        val nguoiBan = ndDao.layTheoId(nguoiBanId)
        thongBao(cuoc.nguoiMuaId, "MOI_DAT_MUA", "Người bán mời bạn đặt mua",
            "${nguoiBan?.hoTen} mời bạn đặt lịch hẹn mua “${tin.tieuDe}”", cuocId)
    }

    // ===================== XÁC NHẬN / ĐỔI / HUỶ (UC-35, UC-36) =====================

    /** Bên còn lại chấp nhận đúng địa điểm và giờ mới nhất → lịch DA_CHOT, tin "Đang giao dịch". */
    suspend fun xacNhan(lichId: Long, nguoiDungId: Long) = db.withTransaction {
        val bayGio = System.currentTimeMillis()
        val lich = lichDao.layTheoId(lichId) ?: error("Không tìm thấy lịch hẹn.")
        if (lich.trangThai != HangSo.LICH_CHO_XAC_NHAN) error("Lịch hẹn này không còn chờ xác nhận.")
        if (QuyTacLichHen.nguoiPhaiXacNhan(lich) != nguoiDungId) error("Bạn đang chờ bên kia xác nhận.") // BR-35
        if (lich.thoiGianHen <= bayGio) error("Giờ hẹn đã qua. Hãy đề xuất giờ khác.")
        val tin = tinDao.layTheoId(lich.tinDangId) ?: error("Tin không còn tồn tại.")
        if (!QuyTacLichHen.tinConNhanDatMua(tin)) error("Tin không còn bán.")
        if (lichDao.layDangHoatDongCuaTin(tin.id).any { it.id != lich.id && it.trangThai == HangSo.LICH_DA_CHOT }) {
            error("Bạn đã chốt hẹn với người khác cho tin này. Huỷ lịch đó trước khi xác nhận người mới.")
        }

        val daChot = lich.copy(trangThai = HangSo.LICH_DA_CHOT, ngayChot = bayGio)
        lichDao.capNhat(daChot)
        if (tin.trangThai == HangSo.TIN_DANG_BAN) { // BR-36
            tinDao.capNhat(tin.copy(trangThai = HangSo.TIN_DANG_GIAO_DICH, ngayCapNhat = bayGio))
        }
        ghiSuKien(lich.cuocTroChuyenId, nguoiDungId, HangSo.SK_CHOT_HEN, "xác nhận · lịch hẹn đã chốt", "Đã chốt hẹn · ${hen(lich)}", lich)
        for (nguoiNhan in listOf(lich.nguoiMuaId, lich.nguoiBanId)) {
            // Người vừa bấm xác nhận đã thấy ngay trên màn hình → không cần bật banner cho họ
            thongBao(nguoiNhan, "CHOT_HEN", "Đã chốt hẹn", "${hen(lich)} · “${tin.tieuDe}”", lich.cuocTroChuyenId, daDay = nguoiNhan == nguoiDungId)
        }
    }

    /** Một bên đề xuất địa điểm/giờ khác → chờ bên kia xác nhận lại (BR-35). */
    suspend fun doiHen(lichId: Long, nguoiDungId: Long, diaDiem: String, thoiGianHen: Long) = db.withTransaction {
        val bayGio = System.currentTimeMillis()
        val lich = lichDao.layTheoId(lichId) ?: error("Không tìm thấy lịch hẹn.")
        if (lich.trangThai != HangSo.LICH_CHO_XAC_NHAN) error("Chỉ đổi được lịch hẹn đang chờ xác nhận.")
        if (nguoiDungId != lich.nguoiMuaId && nguoiDungId != lich.nguoiBanId) error("Bạn không thuộc lịch hẹn này.")
        QuyTacLichHen.loiDiaDiem(diaDiem)?.let { error(it) }
        QuyTacLichHen.loiGioHen(thoiGianHen, bayGio)?.let { error(it) }

        val moi = lich.copy(
            diaDiem = diaDiem.trim(), thoiGianHen = thoiGianHen,
            nguoiDeXuatId = nguoiDungId, hetHanLuc = bayGio + HangSo.MOT_NGAY
        )
        lichDao.capNhat(moi)
        ghiSuKien(lich.cuocTroChuyenId, nguoiDungId, HangSo.SK_DOI_HEN, "đề xuất đổi địa điểm/giờ", "Đề xuất đổi · ${hen(moi)}", moi)
        val nguoiDoi = ndDao.layTheoId(nguoiDungId)
        val vaiTro = if (nguoiDungId == lich.nguoiBanId) "Người bán" else "Người mua"
        thongBao(QuyTacLichHen.nguoiPhaiXacNhan(moi), "DOI_HEN", "Đề xuất đổi địa điểm",
            "$vaiTro ${nguoiDoi?.hoTen} đề xuất đổi sang ${hen(moi)}. Bạn đồng ý không?", lich.cuocTroChuyenId)
    }

    /**
     * Huỷ lịch hẹn (hoặc người bán TỪ CHỐI yêu cầu). Lịch đã chốt bắt buộc có lý do (BR-38).
     * Nếu tin không còn lịch đã chốt nào thì tin trở lại "Đang bán".
     */
    suspend fun huyHen(lichId: Long, nguoiDungId: Long, lyDo: String, tuChoi: Boolean = false) = db.withTransaction {
        val bayGio = System.currentTimeMillis()
        val lich = lichDao.layTheoId(lichId) ?: error("Không tìm thấy lịch hẹn.")
        if (!QuyTacLichHen.dangHoatDong(lich)) error("Lịch hẹn đã kết thúc.")
        if (nguoiDungId != lich.nguoiMuaId && nguoiDungId != lich.nguoiBanId) error("Bạn không thuộc lịch hẹn này.")
        if (lich.trangThai == HangSo.LICH_DA_CHOT && lyDo.isBlank()) error("Huỷ lịch đã chốt cần chọn lý do.")

        lichDao.capNhat(lich.copy(trangThai = HangSo.LICH_DA_HUY, lyDoHuy = lyDo.ifBlank { "Không nêu lý do" }, nguoiHuyId = nguoiDungId))
        val cumTu = when {
            tuChoi -> "từ chối yêu cầu mua · $lyDo"
            lich.trangThai == HangSo.LICH_DA_CHOT -> "huỷ lịch hẹn đã chốt · $lyDo"
            else -> "huỷ yêu cầu mua · $lyDo"
        }
        ghiSuKien(lich.cuocTroChuyenId, nguoiDungId, HangSo.SK_HUY_HEN, cumTu, "Lịch hẹn đã huỷ", lich)

        val tin = tinDao.layTheoId(lich.tinDangId)
        // BR-38: không còn lịch đã chốt nào → tin trở về Đang bán
        if (tin != null && tin.trangThai == HangSo.TIN_DANG_GIAO_DICH &&
            lichDao.layDangHoatDongCuaTin(tin.id).none { it.trangThai == HangSo.LICH_DA_CHOT }
        ) {
            tinDao.capNhat(tin.copy(trangThai = HangSo.TIN_DANG_BAN, ngayCapNhat = bayGio))
        }
        val nguoiNhan = if (nguoiDungId == lich.nguoiMuaId) lich.nguoiBanId else lich.nguoiMuaId
        val tenNguoiHuy = ndDao.layTheoId(nguoiDungId)?.hoTen
        thongBao(
            nguoiNhan, "HUY_HEN",
            if (tuChoi) "Yêu cầu mua bị từ chối" else "Lịch hẹn đã huỷ",
            "$tenNguoiHuy ${if (tuChoi) "từ chối" else "huỷ"} lịch hẹn “${tin?.tieuDe}” · Lý do: $lyDo",
            lich.cuocTroChuyenId
        )
    }

    // ===================== ĐÃ BÁN (UC-14) VÀ ĐÁNH GIÁ (UC-32) =====================

    /** Những người có thể là người mua: người đã chốt hẹn đứng đầu, rồi người đang chờ, rồi người chỉ nhắn tin. */
    suspend fun layUngVienNguoiMua(tinId: Long): List<UngVienNguoiMua> =
        cuocDao.layTheoTin(tinId).mapNotNull { c ->
            val nd = ndDao.layTheoId(c.nguoiMuaId) ?: return@mapNotNull null
            val lich = lichDao.layMoiNhatCuaCuoc(c.id)
            val (ghiChu, thuTu) = when (lich?.trangThai) {
                HangSo.LICH_DA_CHOT -> "đã chốt hẹn · ${hen(lich)}" to 0
                HangSo.LICH_CHO_XAC_NHAN -> "đang chờ xác nhận" to 1
                else -> "đã nhắn tin" to 2
            }
            thuTu to UngVienNguoiMua(nd.id, nd.hoTen, ghiChu, thuTu == 0)
        }.sortedBy { it.first }.map { it.second }

    /**
     * Người bán đánh dấu đã bán với giá chốt thật.
     * - Lịch của người mua được chọn → HOAN_TAT, người đó nhận quyền đánh giá (BR-30).
     * - Các yêu cầu còn lại của tin tự huỷ, người gửi nhận thông báo (BR-36).
     * nguoiMuaId = null nghĩa là "Người khác / không nhớ" → không phát sinh quyền đánh giá.
     */
    suspend fun daBan(tinId: Long, nguoiBanId: Long, giaBan: Long, nguoiMuaId: Long?) = db.withTransaction {
        val bayGio = System.currentTimeMillis()
        val tin = tinDao.layTheoId(tinId) ?: error("Tin không còn tồn tại.")
        if (tin.nguoiBanId != nguoiBanId) error("Chỉ chủ tin mới đánh dấu đã bán được.")
        if (tin.trangThai != HangSo.TIN_DANG_BAN && tin.trangThai != HangSo.TIN_DANG_GIAO_DICH) error("Tin không ở trạng thái đang bán.")
        if (giaBan < 0) error("Giá bán không hợp lệ.")

        tinDao.capNhat(
            tin.copy(
                trangThai = HangSo.TIN_DA_BAN, giaBan = giaBan, ngayBan = bayGio,
                nguoiMuaId = nguoiMuaId, dayTinDen = null, ngayCapNhat = bayGio
            )
        )
        val dsHoatDong = lichDao.layDangHoatDongCuaTin(tinId)
        val gia = DinhDang.gia(giaBan)

        if (nguoiMuaId != null) {
            val cuocId = cuocDao.timPhong(tinId, nguoiMuaId)?.id
                ?: cuocDao.them(CuocTroChuyen(tinDangId = tinId, nguoiMuaId = nguoiMuaId, nguoiBanId = nguoiBanId))
            val lichCu = dsHoatDong.find { it.nguoiMuaId == nguoiMuaId }
            if (lichCu != null) {
                lichDao.capNhat(lichCu.copy(trangThai = HangSo.LICH_HOAN_TAT, ngayChot = lichCu.ngayChot ?: bayGio))
            } else {
                // Giao dịch ngoài app (chỉ nhắn tin rồi gặp): vẫn ghi một lịch HOAN_TAT để người mua đánh giá được
                lichDao.them(
                    LichHen(
                        cuocTroChuyenId = cuocId, tinDangId = tinId, nguoiMuaId = nguoiMuaId, nguoiBanId = nguoiBanId,
                        maTruong = tin.maTruong, diaDiem = tin.diemGiaoDich, thoiGianHen = bayGio,
                        trangThai = HangSo.LICH_HOAN_TAT, nguoiDeXuatId = nguoiBanId, ngayTao = bayGio, ngayChot = bayGio
                    )
                )
            }
            ghiSuKien(cuocId, nguoiBanId, HangSo.SK_DA_BAN, "đánh dấu đã bán · $gia", "Đã bán $gia", null)
            val nguoiBan = ndDao.layTheoId(nguoiBanId)
            thongBao(nguoiMuaId, "MOI_DANH_GIA", "Mời đánh giá người bán",
                "Bạn đã mua “${tin.tieuDe}” giá $gia. Đánh giá ${nguoiBan?.hoTen} để giúp người khác nhé.", cuocId)
        }

        for (l in dsHoatDong) if (l.nguoiMuaId != nguoiMuaId) {
            lichDao.capNhat(l.copy(trangThai = HangSo.LICH_DA_HUY, lyDoHuy = "Tin đã bán cho người khác", nguoiHuyId = 0))
            ghiSuKien(l.cuocTroChuyenId, 0, HangSo.SK_HUY_HEN, "lịch hẹn tự huỷ vì tin đã bán cho người khác", "Lịch hẹn đã huỷ", l)
            thongBao(l.nguoiMuaId, "HUY_HEN", "Yêu cầu mua đã huỷ", "“${tin.tieuDe}” đã được bán cho người khác.", l.cuocTroChuyenId)
        }
    }

    /** Người mua đánh giá người bán 1–5 sao, mỗi giao dịch một lần (BR-30). */
    suspend fun danhGia(lichId: Long, nguoiMuaId: Long, soSao: Int, nhanXet: String) = db.withTransaction {
        val lich = lichDao.layTheoId(lichId) ?: error("Không tìm thấy giao dịch.")
        if (lich.trangThai != HangSo.LICH_HOAN_TAT || lich.nguoiMuaId != nguoiMuaId) error("Chỉ người đã mua mới đánh giá được.")
        if (lich.soSao != null) error("Bạn đã đánh giá giao dịch này rồi.")
        if (soSao !in 1..5) error("Chọn từ 1 đến 5 sao.")

        lichDao.capNhat(lich.copy(soSao = soSao, nhanXet = nhanXet.trim().ifEmpty { null }))
        val sao = "★".repeat(soSao) + "☆".repeat(5 - soSao)
        val cumTu = "đánh giá người bán $sao" + if (nhanXet.isNotBlank()) " · “${nhanXet.trim()}”" else ""
        ghiSuKien(lich.cuocTroChuyenId, nguoiMuaId, HangSo.SK_DANH_GIA, cumTu, "Đánh giá $sao", null)
        val tin = tinDao.layTheoId(lich.tinDangId)
        val nguoiMua = ndDao.layTheoId(nguoiMuaId)
        thongBao(lich.nguoiBanId, "DANH_GIA", "Bạn có đánh giá mới",
            "${nguoiMua?.hoTen} đánh giá $soSao sao cho “${tin?.tieuDe}”", lich.cuocTroChuyenId)
    }

    // ===================== TÁC VỤ TỰ ĐỘNG (UC-62, UC-63) =====================

    /**
     * Hệ thống tự chạy (khi mở app và theo báo thức khi app đóng):
     * - yêu cầu chờ xác nhận quá 24 giờ → tự huỷ (BR-15)
     * - lịch đã chốt còn ≤ 1 giờ → nhắc cả hai bên (BR-37)
     * Trả về số việc đã làm.
     */
    suspend fun chayTacVuTuDong(bayGio: Long = System.currentTimeMillis()): Int = db.withTransaction {
        var dem = 0
        for (lich in lichDao.layDangHoatDong()) {
            when {
                LichNhac.quaHanPhanHoi(lich, bayGio) -> { tuHuyQuaHan(lich); dem++ }
                LichNhac.canNhacHen(lich, bayGio) -> { nhacHen(lich, bayGio); dem++ }
            }
        }
        dem
    }

    private suspend fun tuHuyQuaHan(lich: LichHen) {
        lichDao.capNhat(lich.copy(trangThai = HangSo.LICH_DA_HUY, lyDoHuy = "Quá 24 giờ chưa được xác nhận", nguoiHuyId = 0))
        ghiSuKien(lich.cuocTroChuyenId, 0, HangSo.SK_HUY_HEN, "yêu cầu tự huỷ vì quá 24 giờ chưa được xác nhận", "Lịch hẹn đã tự huỷ", lich)
        val tin = "“${tinDao.layTheoId(lich.tinDangId)?.tieuDe}”"
        val nguoiMuaDeXuat = lich.nguoiDeXuatId == lich.nguoiMuaId
        val tenNguoiMua = ndDao.layTheoId(lich.nguoiMuaId)?.hoTen
        thongBao(
            lich.nguoiMuaId, "HUY_HEN", "Yêu cầu mua đã tự huỷ",
            if (nguoiMuaDeXuat) "Người bán chưa phản hồi sau 24 giờ, yêu cầu mua $tin đã tự huỷ."
            else "Bạn chưa trả lời đề xuất của người bán sau 24 giờ, yêu cầu mua $tin đã tự huỷ.",
            lich.cuocTroChuyenId
        )
        thongBao(
            lich.nguoiBanId, "HUY_HEN", "Yêu cầu mua đã tự huỷ",
            if (nguoiMuaDeXuat) "Bạn chưa xác nhận sau 24 giờ, yêu cầu mua $tin của $tenNguoiMua đã tự huỷ."
            else "$tenNguoiMua chưa trả lời đề xuất của bạn sau 24 giờ, yêu cầu mua $tin đã tự huỷ.",
            lich.cuocTroChuyenId
        )
    }

    private suspend fun nhacHen(lich: LichHen, bayGio: Long) {
        lichDao.capNhat(lich.copy(daNhacLuc = bayGio))
        val conLai = LichNhac.conLai(lich.thoiGianHen, bayGio)
        ghiSuKien(lich.cuocTroChuyenId, 0, HangSo.SK_NHAC_HEN, "nhắc hẹn · còn $conLai nữa", "Nhắc hẹn · ${hen(lich)}", lich)
        val gio = DinhDang.gioHen(lich.thoiGianHen, bayGio)
        val nguoiMua = ndDao.layTheoId(lich.nguoiMuaId)?.hoTen
        val nguoiBan = ndDao.layTheoId(lich.nguoiBanId)?.hoTen
        thongBao(lich.nguoiMuaId, "NHAC_HEN", "Nhắc hẹn", "Còn $conLai nữa: gặp $nguoiBan ở ${lich.diaDiem} lúc $gio.", lich.cuocTroChuyenId)
        thongBao(lich.nguoiBanId, "NHAC_HEN", "Nhắc hẹn", "Còn $conLai nữa: gặp $nguoiMua ở ${lich.diaDiem} lúc $gio.", lich.cuocTroChuyenId)
    }

    // ===================== HÀM PHỤ =====================

    private fun hen(lich: LichHen) = "${lich.diaDiem}, ${DinhDang.gioHen(lich.thoiGianHen)}"

    /**
     * Ghi một tin nhắn và cập nhật phòng chat: dòng tin cuối, số chưa đọc của bên NHẬN, thời điểm hoạt động.
     * nguoiGuiId = 0 là hệ thống (cả hai bên cùng nhận).
     */
    private suspend fun ghiTinNhan(cuocId: Long, tinNhan: TinNhan, tomTat: String) {
        tinNhanDao.them(tinNhan)
        val c = cuocDao.layTheoId(cuocId) ?: return
        cuocDao.capNhat(
            c.copy(
                tinNhanCuoi = tomTat,
                chuaDocNguoiMua = c.chuaDocNguoiMua + if (tinNhan.nguoiGuiId == c.nguoiMuaId) 0 else 1,
                chuaDocNguoiBan = c.chuaDocNguoiBan + if (tinNhan.nguoiGuiId == c.nguoiBanId) 0 else 1,
                capNhatLuc = tinNhan.thoiGian
            )
        )
    }

    /** Thẻ sự kiện: noiDung là CỤM ĐỘNG TỪ ("gửi yêu cầu mua"); màn hình ghép chủ ngữ "Bạn" hoặc tên người gửi. */
    private suspend fun ghiSuKien(cuocId: Long, nguoiGuiId: Long, suKien: String, cumTu: String, tomTat: String, lich: LichHen?) =
        ghiTinNhan(
            cuocId,
            TinNhan(
                cuocTroChuyenId = cuocId, nguoiGuiId = nguoiGuiId, loai = HangSo.TIN_NHAN_SU_KIEN, noiDung = cumTu,
                suKien = suKien, diaDiem = lich?.diaDiem, thoiGianHen = lich?.thoiGianHen
            ),
            tomTat
        )

    /** Ghi thông báo; bấm vào sẽ mở phòng chat. daDay = true nếu không cần bật banner / thông báo hệ thống. */
    private suspend fun thongBao(
        nguoiNhanId: Long, loai: String, tieuDe: String, noiDung: String, cuocId: Long, daDay: Boolean = false
    ) {
        tbDao.them(
            ThongBao(
                nguoiNhanId = nguoiNhanId, loai = loai, tieuDe = tieuDe, noiDung = noiDung,
                loaiLienKet = "CHAT", lienKetId = cuocId, daDay = daDay
            )
        )
    }
}

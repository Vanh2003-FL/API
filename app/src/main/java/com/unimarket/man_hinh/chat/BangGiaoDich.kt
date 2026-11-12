package com.unimarket.man_hinh.chat

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.view.View
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.radiobutton.MaterialRadioButton
import com.unimarket.databinding.BottomSheetDaBanBinding
import com.unimarket.databinding.BottomSheetDanhGiaBinding
import com.unimarket.databinding.BottomSheetHenBinding
import com.unimarket.databinding.ChipLocBinding
import com.unimarket.du_lieu.repository.GiaoDichRepository.UngVienNguoiMua
import com.unimarket.mo_hinh.LichHen
import com.unimarket.mo_hinh.TinDang
import com.unimarket.tien_ich.DinhDang
import com.unimarket.tien_ich.HangSo
import com.unimarket.xu_ly_nghiep_vu.QuyTacLichHen
import java.util.Calendar

/**
 * Các bảng (bottom sheet / hộp thoại) của giao dịch, dùng chung cho màn Chi tiết tin, Phòng chat và Tin của tôi:
 * - moBangHen: Đặt mua & hẹn địa điểm (SCR-31) / Đổi địa điểm, giờ (SCR-32)
 * - hoiLyDo: chọn lý do huỷ / từ chối
 * - moBangDaBan: nhập giá chốt và chọn người mua
 * - moBangDanhGia: đánh giá người bán (SCR-28)
 * Bảng chỉ kiểm tra dữ liệu nhập; việc ghi database do màn hình gọi repository.
 */
object BangGiaoDich {

    data class KetQuaHen(val diaDiem: String, val thoiGian: Long, val loiNhan: String)

    private const val TAG_KHAC = "KHAC"

    /**
     * @param lichHienTai khác null = đang ĐỔI hẹn (hiện dòng "Đề xuất hiện tại", ẩn lời nhắn)
     * @param canhBao dòng cảnh báo vàng (ví dụ tin đã có người hẹn, yêu cầu sẽ xếp hàng)
     */
    fun moBangHen(
        activity: AppCompatActivity,
        tin: TinDang,
        lichHienTai: LichHen?,
        canhBao: String?,
        khiGui: (KetQuaHen) -> Unit
    ) {
        val laDoiHen = lichHienTai != null
        val bang = BottomSheetDialog(activity)
        val b = BottomSheetHenBinding.inflate(activity.layoutInflater)
        bang.setContentView(b.root)
        bang.behavior.state = BottomSheetBehavior.STATE_EXPANDED

        b.tvTieuDe.text = if (laDoiHen) "Đổi địa điểm / giờ hẹn" else "Đặt mua & hẹn địa điểm"
        b.tvTenTin.text = "${tin.tieuDe} · ${DinhDang.gia(tin.gia)}"
        b.btnGui.text = if (laDoiHen) "Gửi đề xuất mới" else "Gửi yêu cầu mua"
        b.oLoiNhan.visibility = if (laDoiHen) View.GONE else View.VISIBLE
        lichHienTai?.let {
            b.tvDeXuatHienTai.visibility = View.VISIBLE
            b.tvDeXuatHienTai.text = "Đề xuất hiện tại: ${it.diaDiem} · ${DinhDang.gioHen(it.thoiGianHen)}"
        }
        if (canhBao != null) {
            b.tvCanhBao.visibility = View.VISIBLE
            b.tvCanhBao.text = canhBao
        }

        // ----- Địa điểm: điểm của người bán đứng đầu, ghi rõ "người bán chọn" (BR-17) -----
        val dsDiem = (listOf(tin.diemGiaoDich) + HangSo.DIEM_GIAO_DICH).distinct()
        var diemChon = lichHienTai?.diaDiem ?: tin.diemGiaoDich
        val laDiemTuNhap = diemChon !in dsDiem
        val luaChonDiem = dsDiem.map { it to if (it == tin.diemGiaoDich) "$it (người bán chọn)" else it } + (TAG_KHAC to "Khác…")
        taoChip(activity, b.nhomDiaDiem, luaChonDiem, if (laDiemTuNhap) TAG_KHAC else diemChon) { ma ->
            diemChon = ma
            b.oDiaDiemKhac.visibility = if (ma == TAG_KHAC) View.VISIBLE else View.GONE
        }
        if (laDiemTuNhap) {
            b.oDiaDiemKhac.visibility = View.VISIBLE
            b.nhapDiaDiemKhac.setText(diemChon)
            diemChon = TAG_KHAC
        }

        // ----- Giờ hẹn: 3 gợi ý + "Chọn giờ khác" (mở lịch và đồng hồ) -----
        val bayGio = System.currentTimeMillis()
        val goiY = QuyTacLichHen.goiYGioHen(bayGio)
        var gioChon: Long? = if (laDoiHen) null else goiY.first().second
        var gioTuChon: Long? = null // giờ người dùng tự chọn bằng đồng hồ (null nếu bấm huỷ)
        taoChip(
            activity, b.nhomGio,
            goiY.map { it.second.toString() to it.first } + (TAG_KHAC to "Chọn giờ khác"),
            if (laDoiHen) "" else goiY.first().second.toString()
        ) { ma ->
            b.tvLoiGio.visibility = View.GONE
            gioChon = if (ma == TAG_KHAC) gioTuChon else ma.toLong()
        }
        val chipKhac = b.nhomGio.findViewWithTag<Chip>(TAG_KHAC)
        chipKhac.setOnClickListener {
            chonNgayGio(activity) { luc ->
                gioTuChon = luc
                gioChon = luc
                chipKhac.text = DinhDang.gioHen(luc)
                b.nhomGio.check(chipKhac.id)
                b.tvLoiGio.visibility = View.GONE
            }
        }

        b.btnHuy.setOnClickListener { bang.dismiss() }
        b.btnGui.setOnClickListener {
            val diaDiem = if (diemChon == TAG_KHAC) b.nhapDiaDiemKhac.text.toString().trim() else diemChon
            val loiDiaDiem = QuyTacLichHen.loiDiaDiem(diaDiem)
            b.oDiaDiemKhac.error = if (diemChon == TAG_KHAC) loiDiaDiem else null
            val gio = gioChon
            val loiGio = if (gio == null) "Chọn giờ hẹn." else QuyTacLichHen.loiGioHen(gio, System.currentTimeMillis())
            b.tvLoiGio.text = loiGio
            b.tvLoiGio.visibility = if (loiGio == null) View.GONE else View.VISIBLE
            if (loiDiaDiem != null || loiGio != null || gio == null) return@setOnClickListener
            bang.dismiss()
            khiGui(KetQuaHen(diaDiem, gio, b.nhapLoiNhan.text.toString().trim()))
        }
        bang.show()
    }

    /** Chọn ngày (trong 7 ngày tới) rồi chọn giờ. */
    private fun chonNgayGio(activity: AppCompatActivity, khiChon: (Long) -> Unit) {
        val lich = Calendar.getInstance()
        val hopNgay = DatePickerDialog(activity, { _, nam, thang, ngay ->
            TimePickerDialog(activity, { _, gio, phut ->
                lich.set(nam, thang, ngay, gio, phut, 0)
                lich.set(Calendar.MILLISECOND, 0)
                khiChon(lich.timeInMillis)
            }, 17, 0, true).show()
        }, lich.get(Calendar.YEAR), lich.get(Calendar.MONTH), lich.get(Calendar.DAY_OF_MONTH))
        hopNgay.datePicker.minDate = lich.timeInMillis - 1000
        hopNgay.datePicker.maxDate = lich.timeInMillis + HangSo.SO_NGAY_HEN_TOI_DA * HangSo.MOT_NGAY
        hopNgay.show()
    }

    /** Hộp thoại chọn một lý do trong danh sách. Bấm vào lý do là xác nhận luôn. */
    fun hoiLyDo(activity: AppCompatActivity, tieuDe: String, dsLyDo: List<String>, khiChon: (String) -> Unit) {
        AlertDialog.Builder(activity)
            .setTitle(tieuDe)
            .setItems(dsLyDo.toTypedArray()) { _, i -> khiChon(dsLyDo[i]) }
            .setNegativeButton("Quay lại", null)
            .show()
    }

    /**
     * Bảng "Đánh dấu đã bán": giá bán thực tế (mặc định = giá rao) và "Bạn bán cho ai?".
     * Người đã chốt hẹn được chọn sẵn; chọn "Người khác / không nhớ" thì nguoiMuaId = null.
     */
    fun moBangDaBan(
        activity: AppCompatActivity,
        tin: TinDang,
        dsUngVien: List<UngVienNguoiMua>,
        khiXacNhan: (giaBan: Long, nguoiMuaId: Long?) -> Unit
    ) {
        val bang = BottomSheetDialog(activity)
        val b = BottomSheetDaBanBinding.inflate(activity.layoutInflater)
        bang.setContentView(b.root)
        bang.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        b.tvTenTin.text = "${tin.tieuDe} · giá rao ${DinhDang.gia(tin.gia)}"
        b.nhapGiaBan.setText(tin.gia.toString())

        val coUngVien = dsUngVien.isNotEmpty()
        b.tvNhanNguoiMua.visibility = if (coUngVien) View.VISIBLE else View.GONE
        b.nhomNguoiMua.visibility = if (coUngVien) View.VISIBLE else View.GONE
        var nguoiMuaChon: Long? = dsUngVien.firstOrNull { it.daChot }?.id
        if (coUngVien) {
            for (uv in dsUngVien) themLuaChon(activity, b.nhomNguoiMua, "${uv.hoTen} · ${uv.ghiChu}", uv.id == nguoiMuaChon) {
                nguoiMuaChon = uv.id
            }
            themLuaChon(activity, b.nhomNguoiMua, "Người khác / không nhớ", nguoiMuaChon == null) { nguoiMuaChon = null }
        }

        b.btnHuy.setOnClickListener { bang.dismiss() }
        b.btnXacNhan.setOnClickListener {
            val gia = b.nhapGiaBan.text.toString().toLongOrNull()
            if (gia == null || gia < 0) {
                b.oGiaBan.error = "Nhập giá đã bán (0 nếu tặng)"
                return@setOnClickListener
            }
            bang.dismiss()
            khiXacNhan(gia, nguoiMuaChon)
        }
        bang.show()
    }

    /** Bảng đánh giá người bán 1–5 sao kèm nhận xét. */
    fun moBangDanhGia(activity: AppCompatActivity, tenNguoiBan: String, tieuDeTin: String, khiGui: (Int, String) -> Unit) {
        val bang = BottomSheetDialog(activity)
        val b = BottomSheetDanhGiaBinding.inflate(activity.layoutInflater)
        bang.setContentView(b.root)
        bang.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        b.tvNguoiBan.text = "$tenNguoiBan · “$tieuDeTin”"
        val moTa = listOf("", "Rất tệ", "Tệ", "Bình thường", "Tốt", "Rất tốt")
        b.tvMucSao.text = moTa[5]
        b.thanhSao.setOnRatingBarChangeListener { thanh, sao, _ ->
            if (sao < 1f) thanh.rating = 1f // ít nhất 1 sao
            b.tvMucSao.text = moTa[thanh.rating.toInt().coerceIn(1, 5)]
        }
        b.btnHuy.setOnClickListener { bang.dismiss() }
        b.btnGui.setOnClickListener {
            bang.dismiss()
            khiGui(b.thanhSao.rating.toInt().coerceIn(1, 5), b.nhapNhanXet.text.toString())
        }
        bang.show()
    }

    /** Báo lỗi nghiệp vụ (BR) bằng hộp thoại để người dùng đọc kỹ. */
    fun baoLoi(activity: AppCompatActivity, loi: String?) {
        AlertDialog.Builder(activity)
            .setTitle("Chưa thực hiện được")
            .setMessage(loi ?: "Đã có lỗi xảy ra, thử lại sau.")
            .setPositiveButton("Đã hiểu", null)
            .show()
    }

    // ------------------------------------------------------------------
    private fun taoChip(
        activity: AppCompatActivity,
        nhom: ChipGroup,
        luaChon: List<Pair<String, String>>,
        dangChon: String,
        khiChon: (String) -> Unit
    ) {
        for ((ma, chu) in luaChon) {
            val chip = ChipLocBinding.inflate(activity.layoutInflater, nhom, false).root
            chip.id = View.generateViewId()
            chip.text = chu
            chip.tag = ma
            nhom.addView(chip)
            if (ma == dangChon) nhom.check(chip.id)
        }
        nhom.setOnCheckedStateChangeListener { g, ids ->
            val chip = ids.firstOrNull()?.let { g.findViewById<Chip>(it) } ?: return@setOnCheckedStateChangeListener
            khiChon(chip.tag as String)
        }
    }

    private fun themLuaChon(activity: AppCompatActivity, nhom: RadioGroup, chu: String, chon: Boolean, khiChon: () -> Unit) {
        val nut = MaterialRadioButton(activity).apply {
            id = View.generateViewId()
            text = chu
            setOnClickListener { khiChon() }
        }
        nhom.addView(nut)
        if (chon) nhom.check(nut.id)
    }
}

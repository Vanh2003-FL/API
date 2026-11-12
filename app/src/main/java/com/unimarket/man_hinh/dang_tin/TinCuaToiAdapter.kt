package com.unimarket.man_hinh.dang_tin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.unimarket.databinding.ItemTinCuaToiBinding
import com.unimarket.mo_hinh.TinDang
import com.unimarket.tien_ich.DinhDang
import com.unimarket.tien_ich.HangSo
import com.unimarket.tien_ich.LuuAnh
import com.unimarket.xu_ly_nghiep_vu.LocTinDang
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Danh sách tin trong "Tin của tôi". Mỗi trạng thái hiện số liệu và nút thao tác khác nhau. */
class TinCuaToiAdapter(private val thaoTac: ThaoTac) : RecyclerView.Adapter<TinCuaToiAdapter.ONhin>() {

    /** Các việc màn hình cần làm khi người dùng bấm nút trên một tin. */
    interface ThaoTac {
        fun mo(tin: TinDang)
        fun sua(tin: TinDang)
        fun daBan(tin: TinDang)
        fun dayTin(tin: TinDang)
        fun giaHan(tin: TinDang)
        fun xoa(tin: TinDang)
        fun xemHoiMua(tin: TinDang)
    }

    private var ds: List<TinDang> = emptyList()
    private var luotLuu: Map<Long, Int> = emptyMap()
    private var hoiMua: Map<Long, Int> = emptyMap()
    private val ngayThang = SimpleDateFormat("dd/MM", Locale("vi"))

    fun capNhat(dsMoi: List<TinDang>, soLuotLuu: Map<Long, Int>, soHoiMua: Map<Long, Int>) {
        ds = dsMoi
        luotLuu = soLuotLuu
        hoiMua = soHoiMua
        notifyDataSetChanged()
    }

    class ONhin(val b: ItemTinCuaToiBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ONhin(ItemTinCuaToiBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = ds.size

    override fun onBindViewHolder(holder: ONhin, position: Int) {
        val tin = ds[position]
        val b = holder.b
        val ctx = b.root.context
        val cha = HangSo.timChaCua(tin.danhMucCon)
        val bayGio = System.currentTimeMillis()

        b.khungAnh.setBackgroundColor(ContextCompat.getColor(ctx, cha.mauNen))
        val coAnh = LuuAnh.hienAnh(b.anhTin, tin.anh.split(";").firstOrNull { it.isNotBlank() }, 200)
        b.anhTin.visibility = if (coAnh) View.VISIBLE else View.GONE
        b.iconTin.visibility = if (coAnh) View.GONE else View.VISIBLE
        b.iconTin.setImageResource(HangSo.timDanhMucCon(tin.danhMucCon)?.icon ?: cha.icon)
        b.iconTin.imageTintList = ContextCompat.getColorStateList(ctx, cha.mauIcon)

        b.tvTieuDe.text = tin.tieuDe
        b.tvGia.text = DinhDang.gia(tin.gia)
        b.tvDiaDiem.text = "${tin.diemGiaoDich} · ${tin.maTruong}"
        b.tvSoLieu.text = when (tin.trangThai) {
            HangSo.TIN_DANG_BAN -> {
                // Làm tròn lên: tin vừa đăng hiện "còn 30 ngày" chứ không phải 29
                val conNgay = ((tin.ngayHetHan - bayGio + HangSo.MOT_NGAY - 1) / HangSo.MOT_NGAY).coerceAtLeast(0)
                "${tin.luotXem} lượt xem · ${luotLuu[tin.id] ?: 0} lượt lưu · còn $conNgay ngày"
            }
            HangSo.TIN_DANG_GIAO_DICH -> "Đã chốt hẹn với người mua · ${tin.luotXem} lượt xem"
            HangSo.TIN_DA_BAN -> "Bán được ${DinhDang.gia(tin.giaBan ?: tin.gia)}" +
                (tin.ngayBan?.let { " · ngày ${ngayThang.format(Date(it))}" } ?: "")
            HangSo.TIN_HET_HAN -> "Hết hạn · đã gia hạn ${tin.soLanGiaHan}/${HangSo.SO_LAN_GIA_HAN_TOI_DA} lần"
            HangSo.TIN_BI_AN -> "Tạm ẩn do ${tin.soBaoCao} báo cáo · chờ quản trị viên xử lý"
            HangSo.TIN_BI_GO -> "Đã bị quản trị viên gỡ vì vi phạm quy định"
            else -> ""
        }

        val noiBat = LocTinDang.dangNoiBat(tin, bayGio)
        b.tvNhanDayTin.visibility = if (noiBat) View.VISIBLE else View.GONE
        b.tvNhanDayTin.text = "Đang đẩy tin · đến ${tin.dayTinDen?.let { ngayThang.format(Date(it)) }}"

        // Nút theo trạng thái
        val dangBan = tin.trangThai == HangSo.TIN_DANG_BAN
        val giaoDich = tin.trangThai == HangSo.TIN_DANG_GIAO_DICH
        val hetHan = tin.trangThai == HangSo.TIN_HET_HAN

        // "N người đang hỏi mua" → tab Tin nhắn lọc theo tin này
        val soHoiMua = hoiMua[tin.id] ?: 0
        b.tvHoiMua.visibility = if ((dangBan || giaoDich) && soHoiMua > 0) View.VISIBLE else View.GONE
        b.tvHoiMua.text = "$soHoiMua người đang hỏi mua ›"
        b.tvHoiMua.setOnClickListener { thaoTac.xemHoiMua(tin) }
        b.btnSua.visibility = if (dangBan || hetHan) View.VISIBLE else View.GONE
        b.btnDaBan.visibility = if (dangBan || giaoDich) View.VISIBLE else View.GONE
        b.btnDayTin.visibility = if (dangBan && !noiBat) View.VISIBLE else View.GONE
        b.btnGiaHan.visibility =
            if (hetHan && tin.soLanGiaHan < HangSo.SO_LAN_GIA_HAN_TOI_DA) View.VISIBLE else View.GONE
        b.btnXoa.visibility = if (dangBan || hetHan) View.VISIBLE else View.GONE
        b.khungNut.visibility = if (dangBan || giaoDich || hetHan) View.VISIBLE else View.GONE

        b.root.setOnClickListener { thaoTac.mo(tin) }
        b.btnSua.setOnClickListener { thaoTac.sua(tin) }
        b.btnDaBan.setOnClickListener { thaoTac.daBan(tin) }
        b.btnDayTin.setOnClickListener { thaoTac.dayTin(tin) }
        b.btnGiaHan.setOnClickListener { thaoTac.giaHan(tin) }
        b.btnXoa.setOnClickListener { thaoTac.xoa(tin) }
    }
}

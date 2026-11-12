package com.unimarket.man_hinh.trang_chu

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.unimarket.R
import com.unimarket.databinding.ItemTinDangBinding
import com.unimarket.mo_hinh.TinDang
import com.unimarket.tien_ich.DinhDang
import com.unimarket.tien_ich.HangSo
import com.unimarket.tien_ich.LuuAnh
import com.unimarket.xu_ly_nghiep_vu.LocTinDang

/**
 * Adapter hiển thị danh sách tin đăng dạng thẻ (Trang chủ, Tìm kiếm, Đã lưu, Tin tương tự...).
 * - ngang = true: thẻ rộng cố định để xếp thành hàng ngang cuộn được.
 * - khiBam: chạm vào thẻ (mở chi tiết).
 * - khiBamTim: chạm vào trái tim (lưu / bỏ lưu); null thì ẩn nút trái tim.
 */
class TinDangAdapter(
    private val ngang: Boolean = false,
    private val khiBamTim: ((TinDang) -> Unit)? = null,
    private val khiBam: (TinDang) -> Unit
) : RecyclerView.Adapter<TinDangAdapter.ONhin>() {

    private var ds: List<TinDang> = emptyList()
    private var daLuu: Set<Long> = emptySet()

    fun capNhat(dsMoi: List<TinDang>, idDaLuu: Set<Long> = daLuu) {
        ds = dsMoi
        daLuu = idDaLuu
        notifyDataSetChanged()
    }

    class ONhin(val b: ItemTinDangBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ONhin {
        val b = ItemTinDangBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        if (ngang) b.root.layoutParams.width = (160 * parent.resources.displayMetrics.density).toInt()
        return ONhin(b)
    }

    override fun getItemCount() = ds.size

    override fun onBindViewHolder(holder: ONhin, position: Int) {
        val tin = ds[position]
        val b = holder.b
        val ctx = b.root.context
        val cha = HangSo.timChaCua(tin.danhMucCon)
        val con = HangSo.timDanhMucCon(tin.danhMucCon)

        // Ảnh: có ảnh thật thì hiện ảnh, không thì hiện icon danh mục trên nền màu danh mục
        b.khungAnh.setBackgroundColor(ContextCompat.getColor(ctx, cha.mauNen))
        val anhBia = tin.anh.split(";").firstOrNull { it.isNotBlank() }
        if (LuuAnh.hienAnh(b.anhTin, anhBia, 400)) {
            b.anhTin.visibility = View.VISIBLE
            b.iconTin.visibility = View.GONE
        } else {
            b.anhTin.visibility = View.GONE
            b.iconTin.visibility = View.VISIBLE
            b.iconTin.setImageResource(con?.icon ?: cha.icon)
            b.iconTin.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(ctx, cha.mauIcon))
        }

        // Nhãn góc ảnh theo trạng thái
        val nhan = when {
            tin.trangThai == HangSo.TIN_DA_BAN -> "Đã bán"
            tin.trangThai == HangSo.TIN_HET_HAN -> "Hết hạn"
            tin.trangThai == HangSo.TIN_BI_AN -> "Tạm ẩn"
            tin.trangThai == HangSo.TIN_DANG_GIAO_DICH -> "Đang giao dịch"
            LocTinDang.dangNoiBat(tin) -> "Nổi bật"
            else -> null
        }
        b.nhanTrangThai.visibility = if (nhan == null) View.GONE else View.VISIBLE
        b.nhanTrangThai.text = nhan

        // Trái tim
        if (khiBamTim == null) {
            b.btnTim.visibility = View.GONE
        } else {
            val coLuu = tin.id in daLuu
            b.btnTim.visibility = View.VISIBLE
            b.btnTim.setImageResource(if (coLuu) R.drawable.ic_heart_filled else R.drawable.ic_heart)
            b.btnTim.imageTintList = ContextCompat.getColorStateList(ctx, if (coLuu) R.color.loi else R.color.chu)
            b.btnTim.contentDescription = if (coLuu) "Bỏ lưu" else "Lưu tin"
            b.btnTim.setOnClickListener { khiBamTim.invoke(tin) }
        }

        b.tvGia.text = DinhDang.gia(tin.gia)
        b.tvTieuDe.text = tin.tieuDe
        val tinhTrang = HangSo.timTinhTrang(tin.tinhTrang)
        b.tvTinhTrang.text = tinhTrang.ten
        b.tvTinhTrang.setTextColor(ContextCompat.getColor(ctx, tinhTrang.mau))
        b.tvNoiBan.text = "${tin.diemGiaoDich} · ${tin.maTruong} · ${DinhDang.thoiGianTruoc(tin.ngayTao)}"

        b.root.setOnClickListener { khiBam(tin) }
    }
}

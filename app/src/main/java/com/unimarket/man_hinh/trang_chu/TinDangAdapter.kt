package com.unimarket.man_hinh.trang_chu

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.unimarket.databinding.ItemTinDangBinding
import com.unimarket.mo_hinh.TinDang
import com.unimarket.tien_ich.DinhDang
import com.unimarket.tien_ich.HangSo
import com.unimarket.tien_ich.LuuAnh

/**
 * Adapter hiển thị danh sách tin đăng dạng thẻ (dùng ở Trang chủ, kết quả tìm kiếm, Đã lưu...).
 * khiBam: việc cần làm khi người dùng chạm vào một thẻ.
 */
class TinDangAdapter(
    private val khiBam: (TinDang) -> Unit
) : RecyclerView.Adapter<TinDangAdapter.ONhin>() {

    private var ds: List<TinDang> = emptyList()

    fun capNhat(dsMoi: List<TinDang>) {
        ds = dsMoi
        notifyDataSetChanged()
    }

    class ONhin(val b: ItemTinDangBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ONhin =
        ONhin(ItemTinDangBinding.inflate(LayoutInflater.from(parent.context), parent, false))

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

        // Nhãn góc ảnh: đang giao dịch / nổi bật
        val noiBat = (tin.dayTinDen ?: 0) > System.currentTimeMillis()
        when {
            tin.trangThai == HangSo.TIN_DANG_GIAO_DICH -> {
                b.nhanTrangThai.text = "Đang giao dịch"
                b.nhanTrangThai.visibility = View.VISIBLE
            }
            noiBat -> {
                b.nhanTrangThai.text = "Nổi bật"
                b.nhanTrangThai.visibility = View.VISIBLE
            }
            else -> b.nhanTrangThai.visibility = View.GONE
        }

        b.tvGia.text = DinhDang.gia(tin.gia)
        b.tvTieuDe.text = tin.tieuDe
        val tinhTrang = HangSo.timTinhTrang(tin.tinhTrang)
        b.tvTinhTrang.text = tinhTrang.ten
        b.tvTinhTrang.setTextColor(ContextCompat.getColor(ctx, tinhTrang.mau))
        b.tvNoiBan.text = "${tin.maTruong} · ${DinhDang.thoiGianTruoc(tin.ngayTao)}"

        b.root.setOnClickListener { khiBam(tin) }
    }
}

package com.unimarket.man_hinh.chat

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.unimarket.databinding.ItemCuocTroChuyenBinding
import com.unimarket.du_lieu.repository.GiaoDichRepository.DongCuoc
import com.unimarket.tien_ich.DinhDang
import com.unimarket.xu_ly_nghiep_vu.QuyTacLichHen

/** Danh sách cuộc trao đổi ở tab Tin nhắn: mỗi dòng có nhãn trạng thái hẹn và số tin chưa đọc. */
class CuocTroChuyenAdapter(private val khiBam: (DongCuoc) -> Unit) :
    RecyclerView.Adapter<CuocTroChuyenAdapter.ONhin>() {

    private var ds: List<DongCuoc> = emptyList()
    private var toiId = 0L

    fun capNhat(dsMoi: List<DongCuoc>, nguoiXemId: Long) {
        ds = dsMoi
        toiId = nguoiXemId
        notifyDataSetChanged()
    }

    class ONhin(val b: ItemCuocTroChuyenBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ONhin(ItemCuocTroChuyenBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = ds.size

    override fun onBindViewHolder(holder: ONhin, position: Int) {
        val d = ds[position]
        val b = holder.b
        val ctx = b.root.context

        b.tvVietTat.text = DinhDang.vietTat(d.doiPhuong.hoTen)
        b.tvTen.text = d.doiPhuong.hoTen
        b.tvGio.text = DinhDang.gioNgan(d.cuoc.capNhatLuc)
        b.tvTin.text = (if (d.laNguoiMua) "Mua · " else "Bán · ") + d.tin.tieuDe

        val (nhan, kieu) = QuyTacLichHen.nhanTrangThai(d.lich, toiId)
        val mau = MauLichHen.cua(kieu)
        b.tvNhan.text = nhan
        b.tvNhan.setTextColor(ContextCompat.getColor(ctx, mau.chu))
        b.tvNhan.backgroundTintList = ContextCompat.getColorStateList(ctx, mau.nen)

        b.tvChuaDoc.visibility = if (d.chuaDoc > 0) View.VISIBLE else View.GONE
        b.tvChuaDoc.text = d.chuaDoc.toString()
        b.tvDongCuoi.text = d.cuoc.tinNhanCuoi.ifBlank { "Chưa có tin nhắn" }
        b.tvDongCuoi.setTypeface(null, if (d.chuaDoc > 0) Typeface.BOLD else Typeface.NORMAL)

        b.root.setOnClickListener { khiBam(d) }
    }
}

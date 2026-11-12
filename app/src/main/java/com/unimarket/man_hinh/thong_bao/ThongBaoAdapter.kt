package com.unimarket.man_hinh.thong_bao

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.unimarket.R
import com.unimarket.databinding.ItemThongBaoBinding
import com.unimarket.mo_hinh.ThongBao
import com.unimarket.tien_ich.DinhDang

/** Danh sách thông báo chia nhóm "Hôm nay / Hôm qua / Trước đó"; mục chưa đọc in đậm, có chấm xanh. */
class ThongBaoAdapter(private val khiBam: (ThongBao) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    /** Một dòng trong danh sách: tiêu đề nhóm, hoặc một thông báo. */
    private sealed class Dong {
        data class Nhom(val ten: String) : Dong()
        data class Muc(val tb: ThongBao) : Dong()
    }

    private var ds: List<Dong> = emptyList()

    fun capNhat(dsThongBao: List<ThongBao>, bayGio: Long = System.currentTimeMillis()) {
        val ketQua = mutableListOf<Dong>()
        var nhomTruoc = ""
        for (tb in dsThongBao) {
            val nhom = when (DinhDang.soNgayLech(tb.thoiGian, bayGio)) {
                0 -> "Hôm nay"
                1 -> "Hôm qua"
                else -> "Trước đó"
            }
            if (nhom != nhomTruoc) {
                ketQua += Dong.Nhom(nhom)
                nhomTruoc = nhom
            }
            ketQua += Dong.Muc(tb)
        }
        ds = ketQua
        notifyDataSetChanged()
    }

    class ONhom(val tv: TextView) : RecyclerView.ViewHolder(tv)
    class OMuc(val b: ItemThongBaoBinding) : RecyclerView.ViewHolder(b.root)

    override fun getItemCount() = ds.size

    override fun getItemViewType(position: Int) = if (ds[position] is Dong.Nhom) 0 else 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == 1) return OMuc(ItemThongBaoBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        val mat = parent.resources.displayMetrics.density
        val tv = TextView(parent.context).apply {
            setTextAppearance(R.style.NhanMuc)
            setPadding((16 * mat).toInt(), (16 * mat).toInt(), (16 * mat).toInt(), (4 * mat).toInt())
        }
        return ONhom(tv)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val d = ds[position]) {
            is Dong.Nhom -> (holder as ONhom).tv.text = d.ten
            is Dong.Muc -> veMuc((holder as OMuc).b, d.tb)
        }
    }

    private fun veMuc(b: ItemThongBaoBinding, tb: ThongBao) {
        val ctx = b.root.context
        val kieu = MoThongBao.kieuCua(tb.loai)
        b.icon.setImageResource(kieu.icon)
        b.icon.imageTintList = ContextCompat.getColorStateList(ctx, kieu.mauIcon)
        b.khungIcon.backgroundTintList = ContextCompat.getColorStateList(ctx, kieu.mauNen)
        b.tvTieuDe.text = tb.tieuDe
        b.tvTieuDe.setTypeface(null, if (tb.daDoc) Typeface.NORMAL else Typeface.BOLD)
        b.tvNoiDung.text = tb.noiDung
        b.tvGio.text = DinhDang.thoiGianTruoc(tb.thoiGian)
        b.chamChuaDoc.visibility = if (tb.daDoc) View.INVISIBLE else View.VISIBLE
        b.hang.setBackgroundColor(ContextCompat.getColor(ctx, if (tb.daDoc) R.color.nen else R.color.be_mat))
        b.hang.setOnClickListener { khiBam(tb) }
    }
}

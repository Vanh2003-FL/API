package com.unimarket.man_hinh.quan_tri

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.unimarket.R
import kotlin.math.ceil
import kotlin.math.max

/**
 * Biểu đồ CỘT NHÓM tự vẽ bằng Canvas (không dùng thư viện biểu đồ).
 * Mỗi nhãn trục X (một ngày) có nhiều cột cạnh nhau – mỗi cột một chuỗi số liệu (ví dụ: tin đăng, tin bán).
 */
class BieuDoCotView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    data class Cot(val nhan: String, val giaTri: List<Int>)

    private var dsCot: List<Cot> = emptyList()
    private var dsMau: List<Int> = emptyList()
    private val dp = resources.displayMetrics.density

    private val butCot = Paint(Paint.ANTI_ALIAS_FLAG)
    private val butLuoi = Paint().apply { color = ContextCompat.getColor(context, R.color.duong_ke); strokeWidth = 1f * resources.displayMetrics.density }
    private val butChu = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.chu_phu)
        textSize = 10f * resources.displayMetrics.scaledDensity
    }
    private val butSo = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.chu)
        textSize = 10f * resources.displayMetrics.scaledDensity
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    private val khung = RectF()

    /** @param mau màu (đã đổi từ resource) cho từng chuỗi, theo đúng thứ tự trong Cot.giaTri */
    fun datDuLieu(ds: List<Cot>, mau: List<Int>, moTa: String) {
        dsCot = ds
        dsMau = mau
        contentDescription = moTa // cho trình đọc màn hình
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (dsCot.isEmpty()) return
        val trai = 28 * dp
        val tren = 14 * dp
        val duoi = height - 20 * dp
        val phai = width - 4 * dp
        val cao = duoi - tren

        // Trục Y: làm tròn giá trị lớn nhất lên bội số đẹp, chia 4 vạch
        val lonNhat = max(dsCot.maxOf { c -> c.giaTri.maxOrNull() ?: 0 }, 1)
        val buoc = max(ceil(lonNhat / 4.0).toInt(), 1)
        val tranY = buoc * 4
        butChu.textAlign = Paint.Align.RIGHT
        for (i in 0..4) {
            val y = duoi - cao * i / 4
            canvas.drawLine(trai, y, phai, y, butLuoi)
            canvas.drawText((buoc * i).toString(), trai - 4 * dp, y + 3 * dp, butChu)
        }

        // Các nhóm cột
        val rongNhom = (phai - trai) / dsCot.size
        val soChuoi = max(dsMau.size, 1)
        val rongCot = rongNhom * 0.72f / soChuoi
        val bo = minOf(rongCot / 2, 4 * dp)
        val hienSo = dsCot.size <= 10
        val buocNhan = ceil(dsCot.size / 7.0).toInt() // tối đa ~7 nhãn ngày cho khỏi chồng chữ
        butChu.textAlign = Paint.Align.CENTER
        dsCot.forEachIndexed { i, cot ->
            val xNhom = trai + rongNhom * i + rongNhom * 0.14f
            cot.giaTri.forEachIndexed { k, gt ->
                val x = xNhom + rongCot * k
                val y = duoi - cao * gt / tranY
                butCot.color = dsMau.getOrElse(k) { dsMau.last() }
                if (gt > 0) {
                    khung.set(x, y, x + rongCot * 0.92f, duoi)
                    canvas.drawRoundRect(khung, bo, bo, butCot)
                    canvas.drawRect(x, maxOf(y, duoi - bo), x + rongCot * 0.92f, duoi, butCot) // đáy vuông
                    if (hienSo) canvas.drawText(gt.toString(), x + rongCot * 0.46f, y - 3 * dp, butSo)
                }
            }
            if (i % buocNhan == 0 || i == dsCot.size - 1) {
                canvas.drawText(cot.nhan, trai + rongNhom * i + rongNhom / 2, height - 6 * dp, butChu)
            }
        }
    }
}

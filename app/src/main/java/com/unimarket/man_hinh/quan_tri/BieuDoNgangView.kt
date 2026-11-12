package com.unimarket.man_hinh.quan_tri

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.text.TextUtils
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.unimarket.R

/**
 * Biểu đồ THANH NGANG tự vẽ: mỗi dòng một danh mục, thanh dài theo số tin.
 * Chiều cao tự tính theo số dòng.
 */
class BieuDoNgangView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    data class Thanh(val nhan: String, val giaTri: Int, val mau: Int)

    private var ds: List<Thanh> = emptyList()
    private val dp = resources.displayMetrics.density
    private val caoDong = 30 * dp

    private val butThanh = Paint(Paint.ANTI_ALIAS_FLAG)
    private val butNen = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = ContextCompat.getColor(context, R.color.nen_lom) }
    private val butChu = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.chu)
        textSize = 12f * resources.displayMetrics.scaledDensity
    }
    private val butSo = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.chu)
        textSize = 12f * resources.displayMetrics.scaledDensity
        isFakeBoldText = true
    }
    private val khung = RectF()

    fun datDuLieu(dsMoi: List<Thanh>, moTa: String) {
        ds = dsMoi
        contentDescription = moTa
        requestLayout()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val rong = MeasureSpec.getSize(widthMeasureSpec)
        setMeasuredDimension(rong, (ds.size * caoDong + 4 * dp).toInt())
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (ds.isEmpty()) return
        val rongNhan = width * 0.38f
        val batDau = rongNhan + 8 * dp
        val rongToiDa = width - batDau - 32 * dp
        val lonNhat = ds.maxOf { it.giaTri }.coerceAtLeast(1)
        val bo = 5 * dp
        ds.forEachIndexed { i, t ->
            val giua = i * caoDong + caoDong / 2
            val nhan = TextUtils.ellipsize(t.nhan, butChu, rongNhan, TextUtils.TruncateAt.END).toString()
            canvas.drawText(nhan, 0f, giua + 4 * dp, butChu)
            khung.set(batDau, giua - 8 * dp, batDau + rongToiDa, giua + 8 * dp)
            canvas.drawRoundRect(khung, bo, bo, butNen)
            val dai = rongToiDa * t.giaTri / lonNhat
            butThanh.color = t.mau
            khung.set(batDau, giua - 8 * dp, batDau + dai.coerceAtLeast(2 * dp), giua + 8 * dp)
            canvas.drawRoundRect(khung, bo, bo, butThanh)
            canvas.drawText(t.giaTri.toString(), batDau + rongToiDa + 6 * dp, giua + 4 * dp, butSo)
        }
    }
}

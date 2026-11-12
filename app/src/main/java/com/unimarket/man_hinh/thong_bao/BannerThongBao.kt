package com.unimarket.man_hinh.thong_bao

import android.app.Activity
import android.content.Intent
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.unimarket.databinding.BannerThongBaoBinding
import com.unimarket.mo_hinh.ThongBao

/**
 * Banner trượt từ đỉnh màn hình khi có thông báo mới lúc app đang mở (SCR-20: hiện 4 giây).
 * Một thông báo → bấm mở đúng nội dung; nhiều thông báo → "Bạn có N thông báo mới", bấm mở trung tâm thông báo.
 */
object BannerThongBao {

    private const val THOI_GIAN_HIEN = 4000L

    fun hien(activity: Activity, dsMoi: List<ThongBao>) {
        if (dsMoi.isEmpty()) return
        val goc = activity.findViewById<ViewGroup>(android.R.id.content) ?: return
        val b = BannerThongBaoBinding.inflate(activity.layoutInflater, goc, false)
        val moiNhat = dsMoi.last()
        val kieu = MoThongBao.kieuCua(moiNhat.loai)

        if (dsMoi.size == 1) {
            b.tvTieuDe.text = moiNhat.tieuDe
            b.tvNoiDung.text = moiNhat.noiDung
        } else {
            b.tvTieuDe.text = "Bạn có ${dsMoi.size} thông báo mới"
            b.tvNoiDung.text = "${moiNhat.tieuDe}: ${moiNhat.noiDung}"
        }
        b.icon.setImageResource(kieu.icon)
        b.icon.imageTintList = ContextCompat.getColorStateList(activity, kieu.mauIcon)
        b.khungIcon.backgroundTintList = ContextCompat.getColorStateList(activity, kieu.mauNen)

        // Đặt dưới thanh trạng thái
        val tren = ViewCompat.getRootWindowInsets(goc)?.getInsets(WindowInsetsCompat.Type.systemBars())?.top ?: 0
        val lp = b.root.layoutParams as FrameLayout.LayoutParams
        lp.gravity = Gravity.TOP
        lp.topMargin = tren + (8 * activity.resources.displayMetrics.density).toInt()
        goc.addView(b.root, lp)

        val the = b.root
        val an = Runnable {
            the.animate().translationY(-the.height.toFloat() - lp.topMargin).alpha(0f).setDuration(250)
                .withEndAction { goc.removeView(the) }.start()
        }
        the.post {
            the.translationY = -the.height.toFloat() - lp.topMargin
            the.animate().translationY(0f).setDuration(250).start()
        }
        the.postDelayed(an, THOI_GIAN_HIEN)
        the.setOnClickListener {
            the.removeCallbacks(an)
            goc.removeView(the)
            if (dsMoi.size == 1) MoThongBao.mo(activity, moiNhat)
            else activity.startActivity(Intent(activity, ThongBaoActivity::class.java))
        }
    }
}

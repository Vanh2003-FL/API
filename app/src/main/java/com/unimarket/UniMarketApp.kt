package com.unimarket

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.unimarket.du_lieu.repository.DanhMucRepository
import com.unimarket.man_hinh.dang_nhap.DangKyActivity
import com.unimarket.man_hinh.dang_nhap.DangNhapActivity
import com.unimarket.tien_ich.ThongBaoHeThong
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

/**
 * Lớp Application của app: chạy một lần khi app khởi động.
 * - Tạo kênh thông báo.
 * - Theo dõi màn hình đang mở: mỗi lần một màn hiện lên thì kiểm tra thông báo mới (hiện banner);
 *   khi app bị ẩn thì đặt báo thức để vẫn nhắc hẹn được.
 */
class UniMarketApp : Application(), Application.ActivityLifecycleCallbacks {

    companion object {
        private var dangMo: WeakReference<Activity>? = null
        private var soManHinhHien = 0

        /** Màn hình đang hiện trên cùng và hiện được banner (null = đang chuyển màn, ở màn đăng nhập, hoặc app ở nền). */
        fun manHinhDangMo(): Activity? = dangMo?.get()?.takeIf { !it.isFinishing }

        /** Còn ít nhất một màn của app đang hiển thị (kể cả màn đăng nhập, lúc chuyển màn). */
        fun appDangHien(): Boolean = soManHinhHien > 0
    }

    private val phamVi = MainScope()

    override fun onCreate() {
        super.onCreate()
        ThongBaoHeThong.taoKenh(this)
        registerActivityLifecycleCallbacks(this)
        // Mở app từ thông báo (bỏ qua màn đăng nhập) vẫn có danh mục mới nhất
        phamVi.launch { DanhMucRepository(this@UniMarketApp).napVaoBoNho() }
    }

    override fun onActivityResumed(activity: Activity) {
        // Màn đăng nhập/đăng ký chỉ hiện thoáng qua → không bật banner ở đây
        if (activity is DangNhapActivity || activity is DangKyActivity) return
        dangMo = WeakReference(activity)
        phamVi.launch { ThongBaoHeThong.kiemTra(this@UniMarketApp) }
    }

    override fun onActivityPaused(activity: Activity) {
        if (dangMo?.get() === activity) dangMo = null
        phamVi.launch { ThongBaoHeThong.datBaoThuc(this@UniMarketApp) }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {
        soManHinhHien++
    }

    override fun onActivityStopped(activity: Activity) {
        soManHinhHien--
    }
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}

package com.unimarket.tien_ich

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import com.unimarket.R
import com.unimarket.UniMarketApp
import com.unimarket.du_lieu.repository.DangTinRepository
import com.unimarket.du_lieu.repository.GiaoDichRepository
import com.unimarket.du_lieu.repository.ThongBaoRepository
import com.unimarket.man_hinh.thong_bao.BannerThongBao
import com.unimarket.man_hinh.thong_bao.ThongBaoActivity
import com.unimarket.man_hinh.trang_chu.ManHinhChinhActivity
import com.unimarket.mo_hinh.ThongBao
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Cầu nối giữa bảng thong_bao và hệ điều hành Android (FR-37, FR-38):
 * 1. Chạy tác vụ tự động (tự huỷ yêu cầu quá 24 giờ, nhắc hẹn trước 1 giờ, báo tin sắp hết hạn).
 * 2. Hiện thông báo mới của người đang đăng nhập:
 *    - app đang mở  → banner trượt từ trên xuống (4 giây)
 *    - app đã đóng → thông báo hệ thống Android (thanh trạng thái)
 * 3. Đặt báo thức AlarmManager cho lần kiểm tra kế tiếp, để app đóng vẫn nhắc hẹn đúng giờ.
 * Chỉ dùng thư viện có sẵn của Android, không thêm WorkManager/Firebase.
 */
object ThongBaoHeThong {

    /** Màn hình nào cần vẽ lại khi có thông báo mới (số trên chuông, danh sách thông báo) thì cài interface này. */
    interface CoThongBaoMoi {
        fun khiCoThongBaoMoi()
    }

    private const val KENH = "giao_dich"
    private val khoa = Mutex() // tránh hai lần kiểm tra chạy chồng lên nhau

    /** Tạo kênh thông báo (bắt buộc từ Android 8). Gọi một lần khi app khởi động. */
    fun taoKenh(context: Context) {
        val kenh = NotificationChannel(KENH, "Giao dịch và lịch hẹn", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Có người muốn mua, chốt hẹn, nhắc hẹn trước 1 giờ, tin sắp hết hạn"
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(kenh)
    }

    /** Chạy tác vụ tự động rồi hiện thông báo mới. Gọi khi mở màn hình, sau thao tác giao dịch và khi báo thức reo. */
    suspend fun kiemTra(context: Context) = khoa.withLock {
        val app = context.applicationContext
        val bayGio = System.currentTimeMillis()
        GiaoDichRepository(app).chayTacVuTuDong(bayGio)
        DangTinRepository(app).baoTinSapHetHan(bayGio)

        val toi = PhienDangNhap.layId(app)
        if (toi != PhienDangNhap.CHUA_DANG_NHAP) {
            val repo = ThongBaoRepository(app)
            val dangMo = UniMarketApp.manHinhDangMo()
            when {
                // App đang mở một màn bình thường → banner trong app
                dangMo != null -> {
                    val moi = repo.layVaDanhDauDaDay(toi)
                    if (moi.isNotEmpty()) {
                        BannerThongBao.hien(dangMo, moi)
                        (dangMo as? CoThongBaoMoi)?.khiCoThongBaoMoi() // cập nhật số trên chuông / danh sách đang mở
                    }
                }
                // App đang hiện nhưng ở màn đăng nhập hoặc đang chuyển màn (ví dụ báo thức reo lúc vừa mở app):
                // chưa đánh dấu, lần mở màn kế tiếp sẽ hiện banner thay vì bật thông báo hệ thống
                UniMarketApp.appDangHien() -> Unit
                // App ở nền / đã đóng → thông báo hệ thống Android
                else -> repo.layVaDanhDauDaDay(toi).takeLast(5).forEach { dayLenMay(app, it) }
            }
        }
        datBaoThuc(app, bayGio)
    }

    /** Đặt báo thức cho mốc sớm nhất cần kiểm tra (giờ nhắc hẹn, hạn 24 giờ, mốc sắp hết hạn của tin). */
    suspend fun datBaoThuc(context: Context, bayGio: Long = System.currentTimeMillis()) {
        val app = context.applicationContext
        val luc = ThongBaoRepository(app).thoiDiemKiemTraTiepTheo(bayGio)
        val baoThuc = app.getSystemService(AlarmManager::class.java)
        val pi = PendingIntent.getBroadcast(
            app, 0, Intent(app, BaoThucReceiver::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        // setAndAllowWhileIdle: không cần quyền "báo thức chính xác", máy ngủ sâu vẫn được đánh thức (có thể trễ vài phút)
        if (luc == null) baoThuc.cancel(pi) else baoThuc.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, luc, pi)
    }

    /** Thông báo hệ thống Android. Bấm vào → mở màn chính rồi tới nội dung liên quan. */
    private fun dayLenMay(context: Context, tb: ThongBao) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return // người dùng chưa cho phép hiện thông báo → vẫn xem được trong trung tâm thông báo

        val moKhiBam = TaskStackBuilder.create(context)
            .addNextIntent(Intent(context, ManHinhChinhActivity::class.java))
            .addNextIntent(ThongBaoActivity.intentMo(context, tb.id))
            .getPendingIntent(tb.id.toInt(), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val thongBao = NotificationCompat.Builder(context, KENH)
            .setSmallIcon(R.drawable.ic_bell)
            .setColor(ContextCompat.getColor(context, R.color.chinh))
            .setContentTitle(tb.tieuDe)
            .setContentText(tb.noiDung)
            .setStyle(NotificationCompat.BigTextStyle().bigText(tb.noiDung))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(moKhiBam)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(tb.id.toInt(), thongBao)
        } catch (e: SecurityException) {
            // Quyền bị thu hồi giữa chừng: bỏ qua, thông báo vẫn nằm trong trung tâm thông báo
        }
    }
}

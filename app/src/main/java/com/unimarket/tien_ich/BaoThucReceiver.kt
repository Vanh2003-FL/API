package com.unimarket.tien_ich

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Nhận báo thức do ThongBaoHeThong.datBaoThuc đặt (và sự kiện khởi động lại máy).
 * Khi reo: chạy tác vụ tự động, đẩy thông báo hệ thống, rồi đặt báo thức cho lần sau.
 */
class BaoThucReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val ketQua = goAsync() // giữ receiver sống tới khi đọc/ghi database xong
        CoroutineScope(SupervisorJob() + Dispatchers.Main).launch {
            try {
                ThongBaoHeThong.kiemTra(context.applicationContext)
            } finally {
                ketQua.finish()
            }
        }
    }
}

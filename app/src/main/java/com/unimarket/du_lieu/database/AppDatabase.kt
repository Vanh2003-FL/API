package com.unimarket.du_lieu.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.unimarket.du_lieu.dao.BaoCaoDao
import com.unimarket.du_lieu.dao.CuocTroChuyenDao
import com.unimarket.du_lieu.dao.LichHenDao
import com.unimarket.du_lieu.dao.NguoiDungDao
import com.unimarket.du_lieu.dao.ThongBaoDao
import com.unimarket.du_lieu.dao.TinDangDao
import com.unimarket.du_lieu.dao.TinNhanDao
import com.unimarket.du_lieu.dao.YeuThichDao
import com.unimarket.mo_hinh.BaoCao
import com.unimarket.mo_hinh.CuocTroChuyen
import com.unimarket.mo_hinh.LichHen
import com.unimarket.mo_hinh.NguoiDung
import com.unimarket.mo_hinh.ThongBao
import com.unimarket.mo_hinh.TinDang
import com.unimarket.mo_hinh.TinNhan
import com.unimarket.mo_hinh.YeuThich

/**
 * Cơ sở dữ liệu SQLite của app (dùng thư viện Room).
 * File lưu trên máy: /data/data/com.unimarket/databases/unimarket.db
 */
@Database(
    entities = [
        NguoiDung::class, TinDang::class, YeuThich::class, CuocTroChuyen::class,
        TinNhan::class, LichHen::class, ThongBao::class, BaoCao::class
    ],
    version = 2, // v2 (Sprint 3): thêm cột tin_dang.doanhThuDayTin
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun nguoiDungDao(): NguoiDungDao
    abstract fun tinDangDao(): TinDangDao
    abstract fun yeuThichDao(): YeuThichDao
    abstract fun cuocTroChuyenDao(): CuocTroChuyenDao
    abstract fun tinNhanDao(): TinNhanDao
    abstract fun lichHenDao(): LichHenDao
    abstract fun thongBaoDao(): ThongBaoDao
    abstract fun baoCaoDao(): BaoCaoDao

    companion object {
        const val TEN_FILE = "unimarket.db"

        @Volatile
        private var banDuyNhat: AppDatabase? = null

        /** Cả app dùng chung MỘT đối tượng database (mở nhiều lần sẽ tốn bộ nhớ). */
        fun layDb(context: Context): AppDatabase =
            banDuyNhat ?: synchronized(this) {
                banDuyNhat ?: Room.databaseBuilder(
                    context.applicationContext, AppDatabase::class.java, TEN_FILE
                )
                    // Đồ án chưa phát hành: khi đổi cấu trúc bảng thì tạo lại DB và nạp lại dữ liệu mẫu
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { banDuyNhat = it }
            }
    }
}

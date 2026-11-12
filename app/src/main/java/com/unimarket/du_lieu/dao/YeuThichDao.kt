package com.unimarket.du_lieu.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.unimarket.mo_hinh.TinDang
import com.unimarket.mo_hinh.YeuThich

/** Các câu lệnh đọc/ghi bảng yeu_thich. */
@Dao
interface YeuThichDao {

    // Lưu trùng thì bỏ qua (mỗi người chỉ lưu một tin một lần)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun them(yeuThich: YeuThich)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun themNhieu(ds: List<YeuThich>)

    @Query("DELETE FROM yeu_thich WHERE nguoiDungId = :nguoiDungId AND tinDangId = :tinDangId")
    suspend fun xoa(nguoiDungId: Long, tinDangId: Long)

    @Query("SELECT COUNT(*) FROM yeu_thich WHERE nguoiDungId = :nguoiDungId")
    suspend fun demCuaNguoiDung(nguoiDungId: Long): Int

    /** Id các tin người dùng đã lưu (để tô đỏ trái tim trên thẻ tin). */
    @Query("SELECT tinDangId FROM yeu_thich WHERE nguoiDungId = :nguoiDungId")
    suspend fun layIdTinDaLuu(nguoiDungId: Long): List<Long>

    /** Danh sách tin đã lưu, mới lưu xếp trước (nối 2 bảng yeu_thich và tin_dang). */
    @Query(
        "SELECT t.* FROM tin_dang t JOIN yeu_thich y ON y.tinDangId = t.id " +
            "WHERE y.nguoiDungId = :nguoiDungId AND t.trangThai != 'DA_XOA' ORDER BY y.ngayTao DESC"
    )
    suspend fun layTinDaLuu(nguoiDungId: Long): List<TinDang>

    @Query("SELECT COUNT(*) FROM yeu_thich WHERE nguoiDungId = :nguoiDungId AND tinDangId = :tinDangId")
    suspend fun daLuu(nguoiDungId: Long, tinDangId: Long): Int
}

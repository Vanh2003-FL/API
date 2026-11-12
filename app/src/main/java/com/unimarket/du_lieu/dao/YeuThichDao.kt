package com.unimarket.du_lieu.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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
}

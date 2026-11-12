package com.unimarket.mo_hinh

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Bảng danh_muc: các DANH MỤC CON do quản trị viên quản lý (UC-53).
 * 3 danh mục cha (Sách / Đồ điện tử / Đồ dùng) cố định trong code vì mỗi cha có cách nhập và
 * cách ước tính giá riêng; ở đây chỉ lưu mã cha.
 * Không xoá danh mục (tin cũ vẫn cần tên), chỉ ẩn.
 */
@Entity(tableName = "danh_muc", indices = [Index("maCha")])
data class DanhMuc(
    @PrimaryKey val ma: String,          // ví dụ "LAPTOP", danh mục mới: "CON_MAY_ANH"
    val maCha: String,                   // SACH / DIEN_TU / DO_DUNG
    val ten: String,
    val tenIcon: String,                 // tên icon trong bộ icon cho phép, ví dụ "ic_laptop"
    val khauHaoNam: Double,              // tỷ lệ mất giá mỗi năm, dùng cho gợi ý giá khi thiếu dữ liệu
    val an: Boolean = false,
    val thuTu: Int = 0                   // thứ tự hiển thị trong danh mục cha
)

# 03 – Đặc tả yêu cầu

## 3.1. Tác nhân (Actor)

| Actor | Mô tả | Kế thừa |
|---|---|---|
| **Khách** | Chưa đăng nhập; chỉ xem/tìm tin, không thấy SĐT người bán | – |
| **Thành viên** | Đã đăng nhập; mua & bán | Khách |
| **Thành viên đã xác thực** | Thành viên đã được duyệt là sinh viên; có huy hiệu, được đẩy tin | Thành viên |
| **Quản trị viên (Admin)** | Duyệt xác thực, xử lý vi phạm, quản lý danh mục, xem thống kê | Thành viên |
| **Hệ thống** (tác nhân phụ) | Tự động hết hạn tin, tự ẩn tin bị báo cáo nhiều | – |

## 3.2. Danh sách use case

| Mã | Tên use case | Actor | Ưu tiên |
|---|---|---|---|
| **Tài khoản** ||||
| UC-01 | Đăng ký bằng email | Khách | M |
| UC-02 | Đăng nhập (email / Google) | Khách | M |
| UC-03 | Quên mật khẩu | Khách | M |
| UC-04 | Thiết lập & cập nhật hồ sơ (chọn trường, thành phố, SĐT) | Thành viên | M |
| UC-05 | Gửi yêu cầu xác thực sinh viên | Thành viên | S |
| UC-06 | Đăng xuất | Thành viên | M |
| **Tin đăng** ||||
| UC-10 | Đăng tin bán | Thành viên | M |
| UC-11 | Nhận gợi ý giá | Thành viên | M (nâng cao) |
| UC-12 | Sửa tin | Thành viên (chủ tin) | M |
| UC-13 | Xoá tin | Thành viên (chủ tin) | M |
| UC-14 | Đánh dấu đã bán | Thành viên (chủ tin) | M |
| UC-15 | Gia hạn tin | Thành viên (chủ tin) | S |
| UC-16 | Xem "Tin của tôi" | Thành viên | M |
| UC-17 | Đẩy tin (mô phỏng thanh toán) | Thành viên đã xác thực | C |
| UC-18 | Tạm ẩn / đánh dấu "Đang giao dịch" | Thành viên (chủ tin) | S |
| **Tìm kiếm & xem** ||||
| UC-20 | Xem trang chủ | Khách | M |
| UC-21 | Tìm kiếm theo từ khoá | Khách | M |
| UC-22 | Lọc & sắp xếp kết quả | Khách | M |
| UC-23 | Duyệt theo danh mục | Khách | M |
| UC-24 | Xem chi tiết tin | Khách | M |
| UC-25 | Xem hồ sơ người bán | Khách | S |
| UC-26 | Lưu / bỏ lưu tin yêu thích | Thành viên | S |
| UC-27 | Đổi phạm vi xem (Trường / Thành phố / Toàn quốc) | Khách | M |
| **Liên hệ & tin cậy** ||||
| UC-30 | Liên hệ qua gọi điện / Zalo | Thành viên | M |
| UC-31 | Chat với người bán | Thành viên | S |
| UC-32 | Đánh giá người bán | Thành viên (người mua) | C |
| UC-33 | Chia sẻ tin ra ngoài (link) | Khách | C |
| UC-40 | Báo cáo tin vi phạm | Thành viên | S |
| **Quản trị** ||||
| UC-50 | Duyệt yêu cầu xác thực sinh viên | Admin | S |
| UC-51 | Xử lý tin bị báo cáo | Admin | M |
| UC-52 | Khoá / mở khoá tài khoản | Admin | S |
| UC-53 | Quản lý danh mục | Admin | C |
| UC-54 | Xem thống kê KPI | Admin | S |
| **Hệ thống** ||||
| UC-60 | Tự động hết hạn tin | Hệ thống | S |
| UC-61 | Tự động ẩn tin bị báo cáo nhiều | Hệ thống | S |

### Sơ đồ use case (PlantUML – dán vào plantuml.com hoặc plugin để xuất hình cho báo cáo)

```plantuml
@startuml
left to right direction
actor "Khách" as Guest
actor "Thành viên" as Member
actor "Admin" as Admin
Member --|> Guest
Admin --|> Member

rectangle UniMarket {
  usecase "Đăng ký / Đăng nhập" as UC01
  usecase "Xem trang chủ" as UC20
  usecase "Tìm kiếm" as UC21
  usecase "Lọc & sắp xếp" as UC22
  usecase "Xem chi tiết tin" as UC24
  usecase "Đăng tin" as UC10
  usecase "Nhận gợi ý giá" as UC11
  usecase "Quản lý tin của tôi" as UC16
  usecase "Đánh dấu đã bán" as UC14
  usecase "Lưu tin" as UC26
  usecase "Chat" as UC31
  usecase "Báo cáo tin" as UC40
  usecase "Xác thực sinh viên" as UC05
  usecase "Duyệt xác thực" as UC50
  usecase "Xử lý báo cáo" as UC51
  usecase "Xem thống kê" as UC54
}

Guest --> UC01
Guest --> UC20
Guest --> UC21
Guest --> UC24
UC21 <.. UC22 : <<extend>>
Member --> UC10
UC10 ..> UC11 : <<include>>
Member --> UC16
UC16 <.. UC14 : <<extend>>
Member --> UC26
Member --> UC31
Member --> UC40
Member --> UC05
Admin --> UC50
Admin --> UC51
Admin --> UC54
@enduml
```

## 3.3. Đặc tả use case chi tiết

### UC-02 – Đăng nhập
| Mục | Nội dung |
|---|---|
| Actor | Khách |
| Tiền điều kiện | Có Internet |
| Hậu điều kiện | Phiên đăng nhập được lưu; nếu hồ sơ chưa hoàn tất → chuyển SCR-05 |
| Luồng chính | 1. Khách mở SCR-03, nhập email + mật khẩu → 2. Bấm "Đăng nhập" → 3. Hệ thống xác thực qua Firebase Auth → 4. Kiểm tra `users/{uid}` đã có `schoolId` → 5. Vào trang chủ |
| Luồng thay thế | 1a. Chọn "Đăng nhập với Google" → chọn tài khoản → tiếp bước 4. 4a. Chưa có hồ sơ → SCR-05 thiết lập hồ sơ |
| Ngoại lệ | Sai mật khẩu → "Email hoặc mật khẩu không đúng". Tài khoản bị khoá (`isBanned`) → đăng xuất, báo "Tài khoản đã bị khoá, liên hệ…" |
| BR | BR-12 |

### UC-04 – Thiết lập hồ sơ
| Mục | Nội dung |
|---|---|
| Actor | Thành viên |
| Luồng chính | 1. Nhập họ tên → 2. Chọn trường từ danh sách có tìm kiếm (tự điền thành phố theo trường) → 3. Nhập SĐT (bắt buộc), Zalo (tuỳ chọn, mặc định = SĐT) → 4. Chọn ảnh đại diện (tuỳ chọn) → 5. Lưu |
| Luồng thay thế | 2a. Không thấy trường → chọn "Trường khác" và nhập tên; tin vẫn hiển thị theo thành phố |
| Ngoại lệ | SĐT sai định dạng → báo lỗi tại trường |
| BR | BR-13 |

### UC-05 – Gửi yêu cầu xác thực sinh viên
| Mục | Nội dung |
|---|---|
| Actor | Thành viên |
| Tiền điều kiện | `verifyStatus` ∈ {NONE, REJECTED} |
| Hậu điều kiện | `verifyStatus = PENDING` hoặc `VERIFIED` |
| Luồng chính (ảnh thẻ) | 1. Mở SCR-20 → 2. Chụp ảnh mặt trước thẻ SV → 3. Nhập MSSV → 4. Gửi → 5. Trạng thái "Chờ duyệt" |
| Luồng thay thế (email trường) | 2a. Nhập email trường (có domain khớp `schools.emailDomains`) → hệ thống gửi link xác minh → người dùng bấm link → `VERIFIED` ngay |
| BR | BR-14, BR-15 |

### UC-10 – Đăng tin bán ⭐
| Mục | Nội dung |
|---|---|
| Actor | Thành viên |
| Tiền điều kiện | Đã đăng nhập, hồ sơ đầy đủ (có SĐT, trường), không bị khoá, chưa vượt BR-04 |
| Hậu điều kiện | Tin được tạo với `status = ACTIVE`, `expiresAt = now + 30 ngày`, xuất hiện trong "Tin mới" |
| Luồng chính | 1. Bấm nút ➕ → **Bước 1 (SCR-12):** chọn/chụp 1–6 ảnh, sắp xếp, chọn ảnh bìa → 2. **Bước 2 (SCR-13):** chọn danh mục cha → danh mục con → 3. **Bước 3 (SCR-14):** nhập tiêu đề, tình trạng, mô tả, các trường riêng theo danh mục (xem 3.7), giá gốc (tuỳ chọn), khu vực giao dịch (mặc định trường/thành phố của người dùng), "Có thể ship" → 4. Hệ thống hiển thị **gợi ý giá** (include UC-11) → 5. Người dùng nhập giá hoặc bấm "Dùng giá này" → 6. **Xem trước (SCR-15)** → 7. Bấm "Đăng tin" → 8. App nén & tải ảnh lên Storage, tạo tài liệu `listings`, sinh `keywords` → 9. Thông báo "Đăng tin thành công", chuyển tới chi tiết tin |
| Luồng thay thế | 1a. Người dùng thoát giữa chừng → hỏi "Lưu nháp?" → lưu `status = DRAFT` (C). 5a. Giá chênh > 50% so với gợi ý (độ tin cậy Trung bình trở lên) → hiện cảnh báo nhẹ "Giá của bạn cao/thấp hơn nhiều so với thị trường", vẫn cho đăng |
| Ngoại lệ | Mất mạng khi tải ảnh → giữ dữ liệu form, nút "Thử lại". Vượt 20 tin đang bán → chặn từ bước 1, gợi ý xoá/đánh dấu đã bán tin cũ. Nội dung chứa từ khoá cấm → chặn và báo lý do (BR-07) |
| BR | BR-01, BR-02, BR-03, BR-04, BR-07, BR-08 |

### UC-11 – Nhận gợi ý giá ⭐
| Mục | Nội dung |
|---|---|
| Actor | Thành viên (trong UC-10 hoặc UC-12) |
| Tiền điều kiện | Đã chọn danh mục con, đã nhập tiêu đề (≥ 10 ký tự) và tình trạng |
| Hậu điều kiện | Hiển thị thẻ gợi ý; lưu `suggestedPrice`, `suggestionConfidence` vào tin khi đăng |
| Luồng chính | 1. Người dùng nhập xong tiêu đề + tình trạng (hoặc đổi một trong hai) → 2. Sau 800 ms không gõ, hệ thống chạy thuật toán (04, mục 4.6) → 3. Hiển thị: giá đề xuất, khoảng giá, số mẫu, độ tin cậy → 4. Người dùng bấm "Dùng giá này" → điền vào ô giá |
| Luồng thay thế | 2a. Không đủ mẫu nhưng có giá gốc/giá bìa → hiển thị "Ước tính theo khấu hao". 2b. Không đủ mẫu và không có giá gốc → hiển thị "Chưa đủ dữ liệu – nhập giá gốc để nhận ước tính" |
| Ngoại lệ | Lỗi mạng → ẩn thẻ gợi ý, không chặn việc đăng tin |
| BR | BR-20 → BR-24 |

### UC-14 – Đánh dấu đã bán
| Mục | Nội dung |
|---|---|
| Actor | Chủ tin |
| Tiền điều kiện | Tin ở `ACTIVE` hoặc `RESERVED` |
| Hậu điều kiện | `status = SOLD`, có `soldPrice`, `soldAt`; tin biến mất khỏi kết quả tìm kiếm |
| Luồng chính | 1. Tại "Tin của tôi" hoặc chi tiết tin, bấm "Đã bán" → 2. Hộp thoại: nhập **giá bán thực tế** (mặc định = giá rao) → 3. Chọn người mua từ danh sách người đã chat về tin này (tuỳ chọn) → 4. Xác nhận |
| Luồng thay thế | 3a. Bỏ qua chọn người mua → không phát sinh quyền đánh giá |
| Hậu quả nghiệp vụ | Người mua được chọn nhận quyền đánh giá người bán (UC-32). `soldPrice` được dùng làm dữ liệu gợi ý giá (trọng số cao) |
| BR | BR-06, BR-21, BR-30 |

### UC-21/22 – Tìm kiếm, lọc, sắp xếp
| Mục | Nội dung |
|---|---|
| Actor | Khách |
| Luồng chính | 1. Bấm thanh tìm kiếm (SCR-07), nhập từ khoá → 2. Hệ thống chuẩn hoá từ khoá (bỏ dấu, chữ thường), truy vấn tin `ACTIVE` có chứa từ khoá theo phạm vi xem hiện tại → 3. Hiển thị lưới kết quả, phân trang 20 tin/lần → 4. Người dùng mở bộ lọc (SCR-08): danh mục, khoảng giá, tình trạng, chỉ người bán đã xác thực, có thể ship → 5. Chọn sắp xếp: Mới nhất (mặc định) / Giá tăng dần / Giá giảm dần → 6. Kết quả cập nhật |
| Luồng thay thế | 2a. Từ khoá rỗng nhưng có bộ lọc → liệt kê theo bộ lọc. 3a. Không có kết quả → trạng thái rỗng + nút "Mở rộng ra Toàn quốc" / "Xoá bộ lọc" |
| Ghi chú | Lưu 10 từ khoá gần nhất trên máy (C) |
| BR | BR-05, BR-09 |

### UC-31 – Chat với người bán
| Mục | Nội dung |
|---|---|
| Actor | Thành viên (người mua) |
| Tiền điều kiện | Đã đăng nhập, không phải chủ tin |
| Luồng chính | 1. Tại chi tiết tin, bấm "Chat" → 2. Hệ thống mở phòng chat có id `{listingId}_{buyerUid}` (tạo mới nếu chưa có) với thẻ thông tin tin đăng ở đầu → 3. Gửi tin nhắn văn bản; có các câu gợi ý nhanh: "Món này còn không bạn?", "Có thể bớt không?", "Hẹn gặp ở đâu?" → 4. Người bán thấy trong danh sách chat (SCR-21), có chấm đỏ chưa đọc |
| Ngoại lệ | Tin đã bán/bị xoá → phòng chat vẫn xem được nhưng hiện nhãn "Tin không còn bán" |
| BR | BR-31 |

### UC-40 – Báo cáo tin
| Mục | Nội dung |
|---|---|
| Actor | Thành viên |
| Luồng chính | 1. Tại chi tiết tin, bấm ⋮ → "Báo cáo" → 2. Chọn lý do: Lừa đảo / Hàng cấm / Sai danh mục / Thông tin sai sự thật / Spam / Khác (+ mô tả) → 3. Gửi → 4. Thông báo "Cảm ơn bạn" |
| Hậu điều kiện | Tạo `reports`; tăng `reportCount` của tin; nếu đạt BR-10 → tin chuyển `HIDDEN` (UC-61) |
| BR | BR-10, BR-11 |

### UC-51 – Xử lý tin bị báo cáo
| Mục | Nội dung |
|---|---|
| Actor | Admin |
| Luồng chính | 1. Mở SCR-26, danh sách báo cáo `PENDING` nhóm theo tin → 2. Xem chi tiết tin + các lý do → 3. Chọn: **Giữ tin** (báo cáo → `DISMISSED`, nếu tin đang `HIDDEN` → `ACTIVE`) / **Ẩn tin** (tin → `HIDDEN` vĩnh viễn, báo cáo → `RESOLVED`) / **Ẩn tin + khoá người bán** (UC-52) |
| BR | BR-11 |

### UC-54 – Xem thống kê
| Mục | Nội dung |
|---|---|
| Actor | Admin |
| Luồng chính | 1. Mở SCR-24 → 2. Chọn khoảng thời gian (7 ngày / 30 ngày / tất cả) → 3. Xem các thẻ KPI (KPI-01, 02, 04, 05, 06, 07, 11, 12) và biểu đồ số tin đăng / số tin bán theo ngày, phân bổ theo danh mục |
| Ghi chú | Dùng truy vấn tổng hợp `count()` của Firestore; số liệu này dùng trực tiếp cho chương đánh giá trong báo cáo |

## 3.4. User story (theo backlog)

| Mã | Là… | Tôi muốn… | Để… | UC | Ưu tiên |
|---|---|---|---|---|---|
| US-01 | sinh viên người bán | đăng tin trong dưới 2 phút | bán nhanh mà không mất công | UC-10 | M |
| US-02 | tân sinh viên | tìm giáo trình theo tên môn và trường | mua đúng sách | UC-21 | M |
| US-03 | người bán | biết món đồ đáng giá bao nhiêu | đặt giá hợp lý | UC-11 | M |
| US-04 | người bán | đánh dấu tin đã bán | không bị hỏi nữa | UC-14 | M |
| US-05 | người mua | lọc theo khoảng giá và tình trạng | không mất thời gian xem đồ quá đắt/quá cũ | UC-22 | M |
| US-06 | người mua | chỉ xem tin trong thành phố của mình | dễ gặp trực tiếp | UC-27 | M |
| US-07 | người mua | thấy người bán đã được xác thực là sinh viên | yên tâm giao dịch | UC-05 | S |
| US-08 | người mua | lưu các tin quan tâm | so sánh trước khi mua | UC-26 | S |
| US-09 | người mua | nhắn tin trong app | không phải lộ SĐT cá nhân | UC-31 | S |
| US-10 | người dùng | báo cáo tin lừa đảo | bảo vệ cộng đồng | UC-40 | S |
| US-11 | người bán | gia hạn tin hết hạn | không phải đăng lại | UC-15 | S |
| US-12 | người mua | đánh giá người bán sau khi mua | giúp người khác chọn người bán uy tín | UC-32 | C |
| US-13 | người bán | đẩy tin lên nổi bật | bán nhanh hơn | UC-17 | C |
| US-14 | admin | xem số liệu giao dịch | đánh giá hiệu quả của app | UC-54 | S |

### Tiêu chí chấp nhận (Acceptance Criteria) cho các story chính

**US-01**
- Cho biết tôi đã đăng nhập và có hồ sơ đầy đủ, khi tôi hoàn thành 3 bước và bấm "Đăng tin", thì tin xuất hiện đầu mục "Tin mới" của thành phố tôi trong ≤ 5 giây (mạng 4G).
- Khi tôi bỏ trống trường bắt buộc, nút "Tiếp" bị vô hiệu và trường lỗi được đánh dấu đỏ kèm thông báo.

**US-03**
- Khi có ≥ 5 tin tương tự, thẻ gợi ý hiển thị giá đề xuất, khoảng giá, số mẫu và độ tin cậy.
- Khi tôi bấm "Dùng giá này", ô giá được điền giá đề xuất.
- Khi tôi đổi tình trạng từ "Tốt" sang "Cũ", giá gợi ý giảm tương ứng.

**US-04**
- Khi tôi đánh dấu "Đã bán" và nhập giá bán thực tế, tin chuyển sang tab "Đã bán" và không còn xuất hiện trong kết quả tìm kiếm.

## 3.5. Yêu cầu chức năng (FR)

| Mã | Mô tả | UC | Ưu tiên |
|---|---|---|---|
| FR-01 | Đăng ký bằng email + mật khẩu, gửi email xác minh | UC-01 | M |
| FR-02 | Đăng nhập email/mật khẩu và Google Sign-In | UC-02 | M |
| FR-03 | Gửi email đặt lại mật khẩu | UC-03 | M |
| FR-04 | Chọn trường từ danh sách có tìm kiếm; thành phố tự điền theo trường | UC-04 | M |
| FR-05 | Xác thực SV bằng ảnh thẻ (admin duyệt) hoặc email trường | UC-05 | S |
| FR-10 | Đăng tin theo 3 bước + xem trước | UC-10 | M |
| FR-11 | Nén ảnh ≤ 500 KB, cạnh dài ≤ 1280 px trước khi tải | UC-10 | M |
| FR-12 | Trường nhập riêng theo danh mục (xem 3.7) | UC-10 | S |
| FR-13 | Tự sinh `keywords` từ tiêu đề + trường riêng | UC-10 | M |
| FR-14 | Gợi ý giá với khoảng giá, số mẫu, độ tin cậy | UC-11 | M |
| FR-15 | Sửa, xoá (mềm) tin | UC-12, 13 | M |
| FR-16 | Đánh dấu đã bán, nhập giá bán thực tế, chọn người mua | UC-14 | M |
| FR-17 | Gia hạn tin hết hạn thêm 30 ngày | UC-15 | S |
| FR-18 | "Tin của tôi" với các tab: Đang bán, Đang giao dịch, Đã bán, Hết hạn, Bị ẩn | UC-16 | M |
| FR-19 | Đẩy tin mô phỏng: chọn gói, màn hình "thanh toán" giả lập, `boostedUntil = now + 3 ngày` | UC-17 | C |
| FR-20 | Trang chủ: tìm kiếm, lưới danh mục, "Nổi bật", "Tin mới", "Sách cho trường bạn" | UC-20 | M |
| FR-21 | Tìm kiếm không phân biệt dấu & hoa thường | UC-21 | M |
| FR-22 | Lọc theo danh mục, khoảng giá, tình trạng, đã xác thực, có thể ship | UC-22 | M |
| FR-23 | Sắp xếp: mới nhất, giá tăng, giá giảm | UC-22 | M |
| FR-24 | Phân trang vô hạn 20 tin/lần | UC-21 | M |
| FR-25 | Chuyển phạm vi xem: Trường / Thành phố / Toàn quốc; ghi nhớ lựa chọn | UC-27 | M |
| FR-26 | Chi tiết tin: slide ảnh, giá, tình trạng, mô tả, trường riêng, người bán, lượt xem, tin tương tự | UC-24 | M |
| FR-27 | Lưu / bỏ lưu tin; danh sách "Đã lưu" | UC-26 | S |
| FR-28 | Hồ sơ người bán: thông tin, huy hiệu, điểm đánh giá, các tin đang bán | UC-25 | S |
| FR-30 | Nút Gọi điện (mở trình quay số) và Zalo (mở `zalo.me/{sđt}`) | UC-30 | M |
| FR-31 | Chat văn bản thời gian thực, câu gợi ý nhanh, trạng thái đã đọc | UC-31 | S |
| FR-32 | Đánh giá 1–5 sao + nhận xét, chỉ khi được chọn là người mua | UC-32 | C |
| FR-33 | Chia sẻ tin qua Android Share Sheet | UC-33 | C |
| FR-40 | Báo cáo tin với lý do | UC-40 | S |
| FR-50 | Danh sách yêu cầu xác thực, xem ảnh thẻ, duyệt/từ chối kèm lý do | UC-50 | S |
| FR-51 | Danh sách báo cáo, xử lý giữ/ẩn/khoá | UC-51 | M |
| FR-52 | Khoá/mở tài khoản | UC-52 | S |
| FR-53 | Thêm/sửa/ẩn danh mục | UC-53 | C |
| FR-54 | Dashboard KPI | UC-54 | S |
| FR-60 | Tin quá hạn không hiển thị và được chuyển `EXPIRED` | UC-60 | S |
| FR-61 | Tự ẩn tin khi đạt ngưỡng báo cáo | UC-61 | S |

## 3.6. Quy tắc nghiệp vụ (BR)

### Tin đăng
| Mã | Quy tắc |
|---|---|
| BR-01 | Tin có 1–6 ảnh; ảnh đầu tiên là ảnh bìa |
| BR-02 | Giá là số nguyên từ 0 đến 50.000.000đ. Giá 0 hiển thị "Tặng miễn phí" |
| BR-03 | Tin hết hạn sau **30 ngày** kể từ khi đăng/gia hạn; được gia hạn tối đa **2 lần** |
| BR-04 | Mỗi tài khoản tối đa **20 tin** ở trạng thái ACTIVE + RESERVED |
| BR-05 | Chỉ tin `ACTIVE` (và chưa quá `expiresAt`) mới xuất hiện trong tìm kiếm, trang chủ, danh mục |
| BR-06 | Tin `SOLD` không thể chuyển lại `ACTIVE`; muốn bán lại phải đăng tin mới |
| BR-07 | Chặn tin chứa từ khoá cấm (danh sách cấu hình: vũ khí, ma tuý, đề thi, tài liệu thi, hàng giả…) |
| BR-08 | Khi sửa tin, các trường thay đổi được: ảnh, tiêu đề, mô tả, giá, tình trạng, khu vực. Không đổi được danh mục cha |
| BR-09 | Xoá tin là **xoá mềm** (`status = DELETED`); dữ liệu giữ lại cho thống kê, không hiện với ai trừ admin |
| BR-10 | Tin bị **3 người khác nhau** báo cáo (báo cáo `PENDING`) → tự chuyển `HIDDEN` chờ admin |
| BR-11 | Một người chỉ báo cáo một tin một lần |

### Tài khoản
| Mã | Quy tắc |
|---|---|
| BR-12 | Tài khoản bị khoá không đăng nhập được; các tin `ACTIVE` của họ chuyển `HIDDEN` |
| BR-13 | Bắt buộc có SĐT và trường trước khi đăng tin hoặc chat |
| BR-14 | Ảnh thẻ SV chỉ admin và chủ tài khoản xem được; xoá sau khi duyệt xong |
| BR-15 | Bị từ chối xác thực được gửi lại sau 24 giờ |
| BR-16 | Khách không thấy SĐT/Zalo của người bán |

### Gợi ý giá (chi tiết thuật toán ở 04, mục 4.6)
| Mã | Quy tắc |
|---|---|
| BR-20 | Mẫu so sánh: cùng danh mục con, tương đồng tiêu đề ≥ 50%, trong 90 ngày, trạng thái ACTIVE hoặc SOLD |
| BR-21 | Tin SOLD dùng `soldPrice` và có trọng số **2**, tin ACTIVE dùng giá rao, trọng số **1** |
| BR-22 | Loại ngoại lai bằng quy tắc IQR (ngoài [Q1 − 1,5·IQR; Q3 + 1,5·IQR]) |
| BR-23 | Độ tin cậy: ≥ 10 mẫu **Cao**; 5–9 **Trung bình**; 3–4 **Thấp**; < 3 → chuyển sang ước tính khấu hao |
| BR-24 | Giá đề xuất làm tròn: < 100.000đ → bội số 1.000đ; 100.000đ–1.000.000đ → bội số 5.000đ; > 1.000.000đ → bội số 50.000đ |

### Giao dịch & tương tác
| Mã | Quy tắc |
|---|---|
| BR-30 | Chỉ người được chủ tin chọn khi "Đã bán" mới được đánh giá, mỗi tin một lần |
| BR-31 | Không tự chat với tin của mình |
| BR-32 | Chỉ thành viên đã xác thực mới được đẩy tin; một tin không đẩy chồng khi đang trong thời gian đẩy |

## 3.7. Danh mục và thuộc tính riêng

| Danh mục cha | Danh mục con | Thuộc tính riêng (`attributes`) |
|---|---|---|
| 📚 **Sách** (`books`) | Giáo trình (`textbook`) | Tên môn học*, Mã môn, Tác giả, NXB, Năm XB, **Giá bìa** |
| | Sách tham khảo, ngoại ngữ (`reference`) | Tác giả, NXB, Giá bìa |
| | Truyện, tiểu thuyết (`novel`) | Tác giả, Giá bìa |
| 💻 **Đồ điện tử** (`electronics`) | Điện thoại, máy tính bảng (`phone`) | **Hãng***, **Model***, Dung lượng, Năm mua, Còn bảo hành |
| | Laptop (`laptop`) | **Hãng***, **Model***, CPU, RAM, Năm mua, Còn bảo hành |
| | Tai nghe, loa, phụ kiện (`accessory`) | Hãng, Năm mua |
| | Máy tính cầm tay (`calculator`) | Hãng, Model |
| 🏠 **Đồ dùng** (`household`) | Gia dụng (`appliance`) | Hãng, Năm mua |
| | Nội thất (`furniture`) | Chất liệu, Kích thước |
| | Quần áo, giày dép (`fashion`) | Size, Giới tính |
| | Xe đạp (`bicycle`) | Hãng, Loại xe |
| | Dụng cụ học tập, khác (`other`) | – |

(*) bắt buộc. Tất cả danh mục đều có trường chung "Giá mua gốc" (tuỳ chọn) để phục vụ ước tính khấu hao và KPI-09.

**Tình trạng (chuẩn hoá):**

| Mã | Hiển thị | Mô tả cho người dùng | Hệ số giá |
|---|---|---|---|
| `LIKE_NEW` | Như mới | Dùng rất ít, không trầy xước, còn đủ phụ kiện | 1,00 |
| `GOOD` | Tốt | Có dấu hiệu sử dụng nhẹ, hoạt động hoàn hảo | 0,85 |
| `FAIR` | Khá | Trầy xước/ghi chú rõ, vẫn dùng tốt | 0,70 |
| `USED` | Cũ | Hao mòn nhiều hoặc có lỗi nhỏ (ghi rõ trong mô tả) | 0,50 |

## 3.8. Vòng đời trạng thái

### Tin đăng
```mermaid
stateDiagram-v2
    [*] --> DRAFT: Lưu nháp
    [*] --> ACTIVE: Đăng tin
    DRAFT --> ACTIVE: Đăng
    ACTIVE --> RESERVED: Chủ tin đánh dấu "Đang giao dịch"
    RESERVED --> ACTIVE: Huỷ giao dịch
    ACTIVE --> SOLD: Đánh dấu đã bán
    RESERVED --> SOLD: Đánh dấu đã bán
    ACTIVE --> EXPIRED: Quá 30 ngày
    EXPIRED --> ACTIVE: Gia hạn (≤ 2 lần)
    ACTIVE --> HIDDEN: ≥3 báo cáo / Admin ẩn / Chủ bị khoá
    HIDDEN --> ACTIVE: Admin giữ tin
    ACTIVE --> DELETED: Chủ tin xoá
    EXPIRED --> DELETED: Chủ tin xoá
    DRAFT --> DELETED: Chủ tin xoá
    SOLD --> [*]
    DELETED --> [*]
```

### Xác thực sinh viên
```mermaid
stateDiagram-v2
    [*] --> NONE
    NONE --> PENDING: Gửi ảnh thẻ
    NONE --> VERIFIED: Xác minh email trường
    PENDING --> VERIFIED: Admin duyệt
    PENDING --> REJECTED: Admin từ chối
    REJECTED --> PENDING: Gửi lại sau 24h
```

## 3.9. Quy tắc kiểm tra dữ liệu nhập (validation)

| Trường | Bắt buộc | Ràng buộc | Thông báo lỗi |
|---|---|---|---|
| Email | ✓ | Định dạng email | "Email không hợp lệ" |
| Mật khẩu | ✓ | ≥ 8 ký tự, có chữ và số | "Mật khẩu tối thiểu 8 ký tự gồm chữ và số" |
| Họ tên | ✓ | 2–50 ký tự | "Vui lòng nhập họ tên" |
| SĐT | ✓ | 10 số, bắt đầu bằng 0 | "Số điện thoại không hợp lệ" |
| Tiêu đề tin | ✓ | 10–80 ký tự | "Tiêu đề từ 10 đến 80 ký tự" |
| Mô tả | ✓ | 20–2.000 ký tự | "Mô tả tối thiểu 20 ký tự để người mua hiểu rõ hơn" |
| Giá | ✓ | 0–50.000.000, số nguyên | "Giá từ 0 đến 50 triệu" |
| Giá gốc / giá bìa | | ≥ giá rao (nếu nhập) | "Giá gốc thường cao hơn giá bán, bạn kiểm tra lại nhé" (cảnh báo, không chặn) |
| Ảnh | ✓ | 1–6 ảnh, JPG/PNG | "Cần ít nhất 1 ảnh" |
| Tình trạng | ✓ | 1 trong 4 giá trị | "Chọn tình trạng sản phẩm" |
| Năm mua | | 2000 – năm hiện tại | "Năm không hợp lệ" |

## 3.10. Yêu cầu phi chức năng (NFR)

| Mã | Nhóm | Yêu cầu | Cách đo |
|---|---|---|---|
| NFR-01 | Hiệu năng | Trang chủ hiển thị dữ liệu đầu tiên ≤ 2 giây trên 4G | Đo bằng Logcat/stopwatch, 10 lần lấy TB |
| NFR-02 | Hiệu năng | Gợi ý giá trả kết quả ≤ 1,5 giây | Đo trong app |
| NFR-03 | Hiệu năng | Cuộn danh sách mượt (không giật) với 200 tin | Kiểm tra thủ công / Profiler |
| NFR-04 | Dung lượng | Ảnh tải lên ≤ 500 KB/ảnh | Kiểm tra trên Firebase Storage |
| NFR-05 | Bảo mật | Chỉ chủ tin sửa/xoá tin; chỉ admin duyệt/khoá | Firestore Security Rules + test trên Emulator |
| NFR-06 | Bảo mật | Ảnh thẻ SV không truy cập công khai | Storage Rules |
| NFR-07 | Riêng tư | Khách không thấy SĐT | Rules + UI |
| NFR-08 | Khả dụng | Người mới đăng được tin đầu tiên không cần hướng dẫn | Pilot: ≥ 80% tự đăng thành công |
| NFR-09 | Khả dụng | Điểm SUS ≥ 68 | Khảo sát sau pilot |
| NFR-10 | Tương thích | Android 8.0 (API 26) trở lên, màn hình 5"–6,9" | Chạy trên ≥ 2 thiết bị/emulator |
| NFR-11 | Tin cậy | Mất mạng không làm mất dữ liệu form đang nhập | Kiểm thử thủ công |
| NFR-12 | Ngôn ngữ | Giao diện tiếng Việt, định dạng tiền `1.250.000đ`, ngày `dd/MM/yyyy` | Kiểm thử thủ công |
| NFR-13 | Trợ năng | Vùng chạm ≥ 48dp, có `contentDescription` cho ảnh/icon | Accessibility Scanner |
| NFR-14 | Bảo trì | Mã nguồn theo MVVM, tách tầng data/domain/ui; logic gợi ý giá có unit test | Review mã |

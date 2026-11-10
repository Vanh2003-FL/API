# 01 – Tổng quan đề tài

## 1.1. Bối cảnh

- Sinh viên thường xuyên phải mua những món đồ **chỉ dùng trong thời gian ngắn**: giáo trình theo học kỳ, máy tính cầm tay, quạt, nồi cơm, bàn học, xe đạp…
- Các thời điểm **dư cung**: cuối năm học, tốt nghiệp (tháng 6–7), chuyển trọ. Các thời điểm **dư cầu**: nhập học (tháng 8–9), đầu học kỳ.
- Kênh hiện tại: group Facebook trường/khu trọ, Chợ Tốt, Shopee, chợ sinh viên tự phát. Các kênh này **không được thiết kế riêng cho sinh viên**.

## 1.2. Vấn đề cần giải quyết

| # | Vấn đề | Hệ quả |
|---|---|---|
| P1 | Tin đăng trên group bị trôi, không phân loại, không lọc được | Người mua mất nhiều thời gian tìm kiếm (chi phí tìm kiếm cao) |
| P2 | Người bán **không biết định giá** | Bán quá rẻ (thiệt) hoặc quá đắt (không bán được) |
| P3 | Người mua **không biết chất lượng thật** của đồ cũ, không tin người lạ | Ngại mua, giao dịch không diễn ra (thông tin bất cân xứng) |
| P4 | Sàn đại trà không lọc theo trường, giáo trình không gắn với môn học | Khó tìm đúng sách của trường mình |
| P5 | Đồ còn dùng được bị bỏ đi khi ra trường/chuyển trọ | Lãng phí tài nguyên, rác thải |

## 1.3. Mục tiêu

### Mục tiêu sản phẩm
- **G1.** Giúp sinh viên đăng một tin bán trong **dưới 2 phút**.
- **G2.** Giúp người mua tìm được món đồ phù hợp qua danh mục, từ khoá, bộ lọc giá – tình trạng – trường/thành phố.
- **G3.** Cung cấp **gợi ý giá** có khoảng giá và độ tin cậy, giúp định giá hợp lý.
- **G4.** Tăng độ tin cậy giao dịch bằng xác thực sinh viên, đánh giá người bán, báo cáo vi phạm.

### Mục tiêu đo lường cho giai đoạn chạy thử (SMART)
| Mã | Mục tiêu | Ngưỡng đạt |
|---|---|---|
| O1 | Số người dùng tham gia pilot trong 2 tuần | ≥ 20 người |
| O2 | Số tin đăng thật | ≥ 50 tin |
| O3 | Tỷ lệ tin chuyển sang "Đã bán" | ≥ 15% |
| O4 | Tỷ lệ người bán dùng giá gợi ý (chênh ≤ 10%) | ≥ 40% |
| O5 | Điểm khả dụng SUS (xem phụ lục) | ≥ 68 (mức "chấp nhận được") |

### Mục tiêu học thuật (bài tập lớn)
- Vận dụng quy trình phát triển phần mềm: phân tích → thiết kế → cài đặt → kiểm thử → đánh giá.
- Vận dụng lý thuyết kinh tế (thông tin bất cân xứng, chi phí giao dịch, thị trường hai mặt, kinh tế tuần hoàn) để thiết kế và đánh giá sản phẩm.

## 1.4. Phạm vi

### Trong phạm vi
- Ứng dụng **Android** (Android 8.0 – API 26 trở lên).
- Tài khoản, hồ sơ, chọn trường, xác thực sinh viên.
- Đăng / sửa / xoá / gia hạn / đánh dấu đã bán tin đăng.
- Danh mục 3 nhóm: **Sách**, **Đồ điện tử**, **Đồ dùng**.
- Tìm kiếm, lọc, sắp xếp, lưu tin yêu thích.
- Liên hệ qua gọi điện/Zalo, chat trong app.
- Gợi ý giá bán.
- Báo cáo vi phạm, đánh giá người bán.
- Chức năng quản trị (admin) ngay trong app, thống kê KPI.
- Đẩy tin (**mô phỏng**, không thu tiền thật).

### Ngoài phạm vi
- Thanh toán trực tuyến, ví điện tử, đặt cọc qua app.
- Vận chuyển, theo dõi đơn hàng.
- Ứng dụng iOS, website.
- Đấu giá, trao đổi hàng đổi hàng có quy trình riêng.
- Gợi ý giá bằng mô hình học máy (chỉ dùng phương pháp thống kê – xem 04-thiet-ke-he-thong.md).

## 1.5. Đối tượng người dùng

| Nhóm | Mô tả | Nhu cầu chính |
|---|---|---|
| Khách | Chưa đăng nhập | Xem thử tin, tìm kiếm |
| Sinh viên – người bán | Có đồ cần thanh lý | Đăng nhanh, định giá đúng, quản lý tin |
| Sinh viên – người mua | Cần đồ giá rẻ | Tìm đúng món, gần mình, tin tưởng người bán |
| Quản trị viên | Vận hành app | Duyệt xác thực, xử lý vi phạm, theo dõi chỉ số |

Một tài khoản sinh viên có thể vừa là người bán vừa là người mua.

## 1.6. Persona

### Persona 1 – Lan, người bán
- 22 tuổi, sinh viên năm 4 ngành Kinh tế, Hà Nội.
- Sắp tốt nghiệp, cần thanh lý ~10 giáo trình, 1 quạt, 1 bàn học.
- **Nỗi đau:** không biết bán bao nhiêu; đăng group thì trôi; bị hỏi đi hỏi lại "còn không bạn?".
- **Mong muốn:** đăng nhanh, có gợi ý giá, đánh dấu đã bán để không bị làm phiền.

### Persona 2 – Minh, người mua
- 18 tuổi, tân sinh viên, từ tỉnh lên TP.HCM.
- Ngân sách hạn chế, cần mua giáo trình năm nhất và đồ dùng phòng trọ.
- **Nỗi đau:** không biết giá thế nào là hợp lý, sợ bị lừa, không biết sách nào đúng môn của trường.
- **Mong muốn:** lọc theo trường, xem giá tham khảo, thấy người bán là sinh viên thật.

### Persona 3 – Quản trị viên
- Chính người phát triển (trong phạm vi bài tập).
- **Mong muốn:** duyệt nhanh ảnh thẻ sinh viên, xử lý tin bị báo cáo, xem số liệu KPI để đánh giá.

## 1.7. Giá trị đề xuất (Value Proposition)

> **"Bán nhanh – mua rẻ – đúng giá – giữa sinh viên với nhau."**

| Cho người bán | Cho người mua |
|---|---|
| Đăng tin trong 2 phút, theo từng bước | Tìm theo trường, thành phố, danh mục, giá |
| Biết nên bán bao nhiêu nhờ gợi ý giá | Biết giá hợp lý trước khi trả giá |
| Tin không bị trôi, quản lý tập trung | Người bán đã xác thực, có đánh giá |
| Thu lại tiền từ đồ không còn dùng | Tiết kiệm chi phí so với mua mới |

## 1.8. Thuật ngữ

| Thuật ngữ | Giải thích |
|---|---|
| Tin đăng (Listing) | Một sản phẩm được rao bán |
| Tình trạng (Condition) | Mức độ mới/cũ, chuẩn hoá 4 mức: Như mới, Tốt, Khá, Cũ |
| Giá rao | Giá người bán đặt khi đăng |
| Giá bán thực tế | Giá chốt, người bán nhập khi đánh dấu "Đã bán" |
| Gợi ý giá | Giá đề xuất do hệ thống tính từ dữ liệu tương tự |
| Độ tin cậy | Mức đáng tin của gợi ý giá, phụ thuộc số mẫu |
| Phạm vi xem | Trường tôi / Thành phố tôi / Toàn quốc |
| Đẩy tin (Boost) | Đưa tin lên mục nổi bật trong một khoảng thời gian |
| MVP | Phiên bản tối thiểu có thể dùng được |
| Pilot | Giai đoạn chạy thử với người dùng thật |

## 1.9. Giả định và ràng buộc

**Giả định**
- A1. Đa số giao dịch là gặp mặt trực tiếp, thanh toán tiền mặt/chuyển khoản ngoài app.
- A2. Người dùng có smartphone Android và kết nối Internet.
- A3. Người bán trung thực khi chọn tình trạng; sai lệch được hạn chế bằng đánh giá và báo cáo.

**Ràng buộc**
- C1. Một người phát triển, không có ngân sách → dùng hạn mức miễn phí của Firebase.
- C2. Không thu tiền thật → tính năng doanh thu chỉ mô phỏng.
- C3. Firestore không hỗ trợ tìm kiếm toàn văn → phải tự thiết kế cơ chế từ khoá (xem 04, mục 4.5).

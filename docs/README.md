# UniMarket – Chợ đồ cũ sinh viên

> Tài liệu phân tích & thiết kế (BA) cho bài tập lớn: xây dựng ứng dụng Android bằng Android Studio.
> Phiên bản: **1.0** · Ngày: 10/11/2026 · Người thực hiện: 1 người (làm toàn bộ BA, thiết kế, lập trình, báo cáo)

## 1. Tóm tắt một đoạn

UniMarket là ứng dụng Android giúp **sinh viên trên toàn quốc** đăng bán, tìm kiếm và trao đổi đồ cũ (sách/giáo trình, đồ điện tử, đồ dùng). Điểm khác biệt so với group Facebook hay Chợ Tốt: dành cho sinh viên với **hồ sơ đầy đủ bắt buộc** (họ tên, SĐT, trường, điểm giao dịch), lọc theo **trường / thành phố**, tình trạng sản phẩm được chuẩn hoá, **mỗi lần đăng tin và mua bán đều xác nhận địa điểm giao dịch qua thông báo**, và có **gợi ý giá bán** dựa trên dữ liệu các sản phẩm tương tự. Đề tài được chấm **50% kỹ thuật – 50% kinh tế**, nên tài liệu gồm cả phân tích kinh tế (thị trường, mô hình kinh doanh, tài chính, KPI) lẫn thiết kế kỹ thuật (use case, CSDL, kiến trúc, giao diện).

## 2. Mục lục tài liệu

| File | Nội dung | Dùng cho |
|---|---|---|
| [01-tong-quan-de-tai.md](01-tong-quan-de-tai.md) | Bối cảnh, vấn đề, mục tiêu, phạm vi, persona, thuật ngữ | Báo cáo chương 1 |
| [02-phan-tich-kinh-te.md](02-phan-tich-kinh-te.md) | Cơ sở lý thuyết, thị trường, đối thủ, SWOT, Business Model Canvas, doanh thu – chi phí, KPI | Báo cáo chương 2, 3 |
| [03-dac-ta-yeu-cau.md](03-dac-ta-yeu-cau.md) | Actor, use case, user story, yêu cầu chức năng/phi chức năng, quy tắc nghiệp vụ | Báo cáo chương 4 · **Code** |
| [04-thiet-ke-he-thong.md](04-thiet-ke-he-thong.md) | Kiến trúc, công nghệ, CSDL Firestore, thuật toán tìm kiếm & gợi ý giá, bảo mật | Báo cáo chương 5 · **Code** |
| [05-thiet-ke-giao-dien.md](05-thiet-ke-giao-dien.md) | Design system, sơ đồ màn hình, đặc tả & wireframe từng màn hình | Báo cáo chương 5 · **Code / Figma** |
| [06-ke-hoach-kiem-thu.md](06-ke-hoach-kiem-thu.md) | Lộ trình sprint, kế hoạch kiểm thử, chạy thử (pilot), rủi ro | Báo cáo chương 6, 7 · **Quản lý dự án** |
| [phu-luc-khao-sat.md](phu-luc-khao-sat.md) | Bảng hỏi khảo sát trước khi làm & bảng hỏi sau pilot (SUS) | Báo cáo chương 2, 7 |
| [../design/unimarket-ui.html](../design/unimarket-ui.html) | Bản mẫu giao diện HTML bấm được, có chú thích từng màn hình | Xem nhanh bố cục · Slide bảo vệ |

## 3. Quy ước mã định danh

Mọi mục đều có mã để tham chiếu chéo giữa tài liệu ↔ code ↔ báo cáo (ví dụ ghi trong commit: `feat(UC-10): màn hình đăng tin`).

| Tiền tố | Ý nghĩa | Ví dụ |
|---|---|---|
| `UC-xx` | Use case | UC-10 Đăng tin |
| `US-xx` | User story | US-03 Gợi ý giá |
| `FR-xx` | Yêu cầu chức năng | FR-21 Lọc theo khoảng giá |
| `NFR-xx` | Yêu cầu phi chức năng | NFR-01 Thời gian tải |
| `BR-xx` | Quy tắc nghiệp vụ | BR-03 Tin hết hạn sau 30 ngày |
| `SCR-xx` | Màn hình | SCR-14 Đăng tin – bước 3 |
| `TC-xx` | Test case | TC-10 Đăng tin hợp lệ |
| `KPI-xx` | Chỉ số đo lường | KPI-01 Số giao dịch thành công/tháng |

Mức ưu tiên theo **MoSCoW**: **M** (Must – bắt buộc cho MVP), **S** (Should – nên có), **C** (Could – có thì tốt), **W** (Won't – không làm trong phạm vi này).

## 4. Các quyết định đã chốt

| # | Quyết định | Lý do |
|---|---|---|
| D1 | Phạm vi người dùng: **sinh viên toàn quốc**, mặc định hiển thị tin cùng thành phố | Mở rộng thị trường nhưng vẫn giữ giao dịch trực tiếp |
| D2 | Một người phát triển → dùng **Firebase** (Auth, Firestore, Storage), không tự viết server | Giảm khối lượng backend |
| D3 | Ngôn ngữ **Kotlin**, kiến trúc **MVVM** | Khuyến nghị của Google, dễ trình bày |
| D4 | Không thanh toán trong app, không vận chuyển; các tính năng thu phí (đẩy tin) được **mô phỏng** | Tránh pháp lý & tích hợp cổng thanh toán, vẫn chứng minh được mô hình doanh thu |
| D5 | Gợi ý giá tính **trên thiết bị** (client-side), không dùng Cloud Functions | Không cần gói trả phí, đủ nhanh với ≤ 50 mẫu |
| D6 | Admin nằm **trong cùng app**, phân quyền bằng `role` | Không phải làm thêm web admin |
| D7 | Có giai đoạn **chạy thử thật (pilot)** với 20–30 sinh viên | Lấy số liệu thật cho phần đánh giá kinh tế |
| D8 | **Bỏ xác thực sinh viên** (ảnh thẻ, email trường). Thay bằng **hồ sơ đầy đủ bắt buộc**: họ tên, SĐT, trường, điểm giao dịch quen thuộc | Linh hoạt, giảm rào cản tham gia; bớt khối lượng duyệt của admin |
| D9 | **Mỗi lần đăng tin và mỗi lần mua bán đều xác nhận địa điểm giao dịch**, có thông báo cho các bên; giao dịch được quản lý bằng **lịch hẹn** | Bù lại độ tin cậy khi không xác thực; giảm bùng hẹn, tăng an toàn khi gặp mặt |
| D10 | **Gộp Đặt mua và Chat**: mỗi cặp tin–người mua có một phòng chat, lịch hẹn hiển thị bằng thanh ghim + thẻ sự kiện ngay trong phòng chat; bỏ màn Lịch hẹn riêng, thay bằng bộ lọc trong tab Tin nhắn | Hai bên không phải chuyển màn; thương lượng và chốt hẹn ở một chỗ; có bản ghi thoả thuận; bớt một màn hình cho người làm một mình |

## 5. Lịch sử thay đổi

| Phiên bản | Ngày | Nội dung |
|---|---|---|
| 1.0 | 10/11/2026 | Bản BA đầu tiên |
| 1.1 | 11/11/2026 | Bỏ xác thực sinh viên (D8); thêm hồ sơ đầy đủ bắt buộc, xác nhận địa điểm khi đăng tin, đặt mua – lịch hẹn – thông báo (D9). Các mã **UC-05, UC-50, FR-05, FR-50, BR-14, BR-15, BR-32, SCR-20, SCR-25** được dùng lại với nội dung mới; xem chi tiết trong từng file |
| 1.2 | 11/11/2026 | Gộp Đặt mua và Chat (D10): phòng chat có thanh ghim lịch hẹn và thẻ sự kiện (UC-31, mục 3.3.1–3.3.2; BR-40, BR-41); bỏ SCR-25, tab Tin nhắn có bộ lọc theo trạng thái hẹn; dữ liệu `chats` thêm trạng thái hẹn, `messages` thêm loại `event` |

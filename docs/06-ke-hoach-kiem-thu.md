# 06 – Kế hoạch thực hiện & kiểm thử

## 6.1. Phương pháp
- Làm theo **Scrum rút gọn cho 1 người**: sprint 2 tuần, mỗi sprint có mục tiêu rõ, cuối sprint tự demo & ghi lại (ảnh chụp/video) để đưa vào báo cáo.
- Backlog = các FR/US ở tài liệu 03, quản lý bằng GitHub Projects hoặc Trello.
- Mỗi commit/nhánh ghi mã: `feat(UC-10): ...`, `fix(FR-21): ...`.

## 6.2. Lộ trình theo sprint

| Sprint | Mục tiêu | Công việc chính | Sản phẩm bàn giao |
|---|---|---|---|
| **S0** – Nghiên cứu | Có dữ liệu thực tế | Phát khảo sát (phụ lục A), phân tích đối thủ, hoàn thiện tài liệu 01–02 | Kết quả khảo sát, chương 2 báo cáo |
| **S1** – Thiết kế | Chốt thiết kế | Wireframe → mockup Figma; hoàn thiện ERD, cấu trúc Firestore, đặc tả Rules; chuẩn bị dữ liệu `schools`, `categories` | Prototype Figma, chương 4–5 |
| **S2** – Nền tảng | App chạy được, đăng nhập được | Tạo project, theme/design system, điều hướng, Firebase; SCR-01→05; UC-01→04, 06 | APK đăng ký/đăng nhập/thiết lập hồ sơ |
| **S3** – Đăng tin | Đăng được tin | SCR-12→15, nén & tải ảnh, KeywordGenerator, validation, BR-04, BR-07 | Đăng tin thật lên Firestore |
| **S4** – Xem & tìm | Tìm được tin | SCR-06→10, phạm vi xem, tìm kiếm, lọc, sắp xếp, phân trang | Luồng mua cơ bản |
| **S5** – Quản lý tin & hẹn | **MVP hoàn chỉnh** | SCR-16, sửa/xoá/đã bán/gia hạn, hết hạn, liên hệ Gọi/Zalo, kiểm tra hồ sơ đầy đủ (SCR-29), xác nhận địa điểm khi đăng (SCR-30), **đặt mua – phòng chat gắn lịch hẹn – thông báo trong app** (SCR-20, 21, 22, 31, 32), Security Rules v1 | **MVP** – có thể cho bạn bè dùng thử |
| **S6** – Gợi ý giá | Tính năng nâng cao | PriceSuggester + unit test, thẻ gợi ý, dữ liệu gieo 100–200 tin | Gợi ý giá hoạt động |
| **S7** – Tin cậy & tương tác | Hoàn thiện | Câu trả lời nhanh theo ngữ cảnh, đã lưu, báo cáo, hồ sơ người bán, đánh giá, nhắc hẹn (WorkManager), thông báo đẩy khi app đóng (FCM + Cloud Function) | Bản beta |
| **S8** – Admin & KPI | Vận hành & đo lường | SCR-24→26, dashboard KPI, đẩy tin mô phỏng, Rules hoàn chỉnh + test | Bản release candidate |
| **S9** – Pilot | Dữ liệu thật | Chạy thử 2 tuần (6.5), sửa lỗi nóng | Số liệu KPI, phản hồi, SUS |
| **S10** – Báo cáo | Bảo vệ | Phân tích số liệu pilot, hiệu chỉnh tham số gợi ý giá, viết báo cáo, slide, video demo | Báo cáo + slide + APK |

**Nguyên tắc cắt giảm khi trễ:** giữ nguyên S2→S6 (MVP + gợi ý giá) và S9 (pilot); cắt theo thứ tự: đẩy tin (C) → đánh giá (C) → câu trả lời nhanh theo ngữ cảnh → thông báo đẩy khi app đóng (S, chỉ giữ thông báo trong app). **Không cắt** phòng chat gắn lịch hẹn và xác nhận địa điểm (D9, D10).

## 6.3. Kế hoạch kiểm thử

### 6.3.1. Các mức kiểm thử
| Mức | Đối tượng | Công cụ | Tiêu chí đạt |
|---|---|---|---|
| Unit test | `PriceSuggester`, `KeywordGenerator`, `TextNormalizer`, `ListingValidator`, định dạng tiền | JUnit | 100% test pass; phủ các nhánh chính |
| Kiểm thử Rules | Firestore & Storage Rules | Firebase Emulator Suite + `@firebase/rules-unit-testing` | Mọi dòng trong bảng 4.8 có ít nhất 1 test cho phép & 1 test từ chối |
| Kiểm thử chức năng | Các luồng theo UC | Thủ công theo bảng TC | ≥ 95% TC mức M pass trước pilot |
| Kiểm thử tương thích | ≥ 2 thiết bị (1 máy thật + emulator API 26) | Android Studio | Không lỗi hiển thị/crash |
| Kiểm thử khả dụng | Người dùng thật | Pilot + SUS | SUS ≥ 68 |

### 6.3.2. Unit test cho gợi ý giá (bắt buộc – minh chứng kỹ thuật trong báo cáo)
| Mã | Tình huống | Kỳ vọng |
|---|---|---|
| UT-01 | Dữ liệu ví dụ 4.6.4 (7 mẫu, có 1 ngoại lai) | suggested = 43.000, low = 43.000, high = 47.000, confidence = MEDIUM, sampleSize = 6 |
| UT-02 | 12 mẫu cùng thành phố | confidence = HIGH, scope = CITY |
| UT-03 | Tổng 4 mẫu, trong đó 2 mẫu cùng thành phố | scope = NATIONAL, confidence = LOW |
| UT-04 | 1 mẫu, sách có giá bìa 80.000, tình trạng Tốt | suggested = 40.000, confidence = ESTIMATE |
| UT-05 | 0 mẫu, laptop giá gốc 15 triệu, mua 2024, năm 2026, Tốt | suggested = 7.650.000 |
| UT-06 | 0 mẫu, không có giá gốc | confidence = NONE |
| UT-07 | Mẫu cũ hơn 90 ngày | Không được tính |
| UT-08 | Mẫu có tương đồng tiêu đề < 50% | Không được tính |
| UT-09 | Làm tròn: 42.500 / 342.000 / 7.630.000 | 43.000 / 340.000 / 7.650.000 |
| UT-10 | Tất cả mẫu cùng giá (IQR = 0) | Không loại mẫu nào, suggested = giá đó × f |

### 6.3.2b. Unit test cho hồ sơ & lịch hẹn
| Mã | Tình huống | Kỳ vọng |
|---|---|---|
| UT-20 | `ProfileChecker` với hồ sơ thiếu SĐT và điểm giao dịch | `complete = false`, `missing = [phone, defaultSpot]` |
| UT-21 | Điểm giao dịch tự nhập 6 ký tự | Không hợp lệ (BR-34) |
| UT-22 | `AppointmentRules`: người đề xuất tự xác nhận đề xuất của mình | Bị từ chối (chỉ bên kia được xác nhận) |
| UT-23 | `PENDING_SELLER` → người bán đổi địa điểm | `PENDING_BUYER`, `proposedBy = seller`, history + 1 |
| UT-24 | `CONFIRMED` → huỷ không có lý do | Bị từ chối (BR-38) |
| UT-25 | Giờ hẹn = hiện tại + 10 phút / + 8 ngày | Không hợp lệ (BR-37) |
| UT-26 | Người mua đã có 5 yêu cầu chờ, gửi yêu cầu thứ 6 | Bị chặn (BR-33) |
| UT-27 | Yêu cầu tạo lúc T, kiểm tra lúc T + 25 giờ | `CANCELLED` do quá hạn (BR-15) |

### 6.3.3. Test case chức năng (trích – các luồng mức M)
| Mã | UC | Bước | Kết quả mong đợi |
|---|---|---|---|
| TC-01 | UC-01 | Đăng ký email hợp lệ, mật khẩu "abc12345" | Tạo tài khoản, chuyển SCR-05 |
| TC-02 | UC-01 | Mật khẩu "1234" | Báo lỗi độ dài, không tạo tài khoản |
| TC-03 | UC-02 | Đăng nhập sai mật khẩu | "Email hoặc mật khẩu không đúng" |
| TC-04 | UC-02 | Đăng nhập tài khoản bị khoá | Bị đăng xuất, thông báo bị khoá |
| TC-05 | UC-04 | Gõ "bach khoa" trong ô chọn trường | Hiện "Đại học Bách khoa Hà Nội" |
| TC-06 | UC-05 | Hồ sơ thiếu SĐT, bấm ➕ Đăng tin | Hiện SCR-29 liệt kê "Số điện thoại"; bổ sung xong quay lại bước 1 đăng tin |
| TC-07 | UC-05 | Hồ sơ thiếu, bấm "Đặt mua" ở chi tiết tin | Hiện SCR-29; không tạo lịch hẹn |
| TC-10 | UC-10 | Đăng tin đủ thông tin, 3 ảnh | Tin ACTIVE, xuất hiện đầu "Tin mới" |
| TC-11 | UC-10 | Không chọn ảnh | Nút "Tiếp" bị vô hiệu |
| TC-12 | UC-10 | Tiêu đề 5 ký tự | Báo lỗi tại ô tiêu đề |
| TC-13 | UC-10 | Tiêu đề chứa "đề thi" | Bị chặn với thông báo từ khoá cấm |
| TC-14 | UC-10 | Đã có 20 tin đang bán, bấm ➕ | Thông báo vượt giới hạn |
| TC-15 | UC-10 | Tắt mạng khi bấm Đăng | Báo lỗi, dữ liệu form còn nguyên, Thử lại được |
| TC-16 | UC-11 | Đổi tình trạng Tốt → Cũ | Giá gợi ý giảm theo hệ số |
| TC-17 | UC-11 | Bấm "Dùng giá này" | Ô giá được điền giá đề xuất |
| TC-18 | UC-19 | Ở Xem trước bấm Đăng tin, không tích ô xác nhận | Nút "Xác nhận & đăng tin" bị vô hiệu |
| TC-19 | UC-19 | Tích xác nhận và đăng | Tin ACTIVE có `placeConfirmedAt`; thông báo "Tin đã đăng" kèm địa điểm |
| TC-20 | UC-21 | Tìm "kinh te vi mo" (không dấu) | Ra tin "Giáo trình Kinh tế vi mô" |
| TC-21 | UC-22 | Lọc giá 0–50.000, tình trạng Tốt | Chỉ hiện tin thoả cả hai điều kiện |
| TC-22 | UC-22 | Sắp xếp giá tăng dần | Thứ tự giá không giảm |
| TC-23 | UC-27 | Đổi phạm vi sang Toàn quốc | Hiện tin ở thành phố khác |
| TC-24 | UC-21 | Tìm từ khoá không tồn tại | Trạng thái rỗng + gợi ý mở rộng |
| TC-30 | UC-14 | Đánh dấu đã bán, giá thực tế 40.000 | Tin vào tab Đã bán, biến mất khỏi tìm kiếm, `soldPrice = 40000` |
| TC-31 | UC-12 | Người khác cố sửa tin (qua Rules test) | PERMISSION_DENIED |
| TC-32 | UC-60 | Tin có `expiresAt` ở quá khứ | Không hiện ở trang chủ; vào tab Hết hạn của chủ tin |
| TC-33 | UC-15 | Gia hạn lần 3 | Nút Gia hạn bị ẩn |
| TC-40 | UC-30 | Khách bấm "Gọi" | Yêu cầu đăng nhập |
| TC-41 | UC-30 | Thành viên bấm "Zalo" | Mở Zalo/trình duyệt tới `zalo.me/{sđt}` |
| TC-42 | UC-34 | Người mua (máy A) đặt mua từ chi tiết tin, giữ điểm của người bán, chọn 17:00 hôm nay | Máy A mở thẳng phòng chat, ghim "Chờ người bán xác nhận", có thẻ "Bạn đã gửi yêu cầu mua"; máy B nhận banner ≤ 5 giây |
| TC-43 | UC-35 | Máy B bấm banner → phòng chat → "Xác nhận" trên thanh ghim | Lịch hẹn CONFIRMED, tin RESERVED; cả hai phòng chat có thẻ "đã chốt"; máy A nhận "Đã chốt hẹn" |
| TC-44 | UC-35 | Máy B bấm "Đổi" trên thanh ghim, chọn Thư viện 17:30 | Máy A: ghim "Người bán đề xuất…" với nút Đồng ý; máy B: ghim "Chờ người mua đồng ý", không có nút Xác nhận |
| TC-45 | UC-36 | Máy A huỷ lịch hẹn đã chốt, chọn lý do | CANCELLED; thẻ sự kiện "đã huỷ: <lý do>" ở cả hai bên; máy B nhận thông báo; tin về ACTIVE |
| TC-49 | UC-31 | Từ phòng chat vừa tạo bằng Đặt mua, bấm quay lại | Về chi tiết tin, nút chính là "Xem trao đổi" |
| TC-50a | UC-38 | Người bán bấm "2 người đang hỏi mua" ở Tin của tôi | Tab Tin nhắn lọc theo tin đó, hiện đúng 2 cuộc trao đổi kèm nhãn trạng thái |
| TC-50b | BR-41 | Thử sửa / xoá một message `event` (Rules test) | PERMISSION_DENIED |
| TC-46 | UC-62 | Lịch hẹn đã chốt lúc 17:00 | 16:00 cả hai máy nhận thông báo nhắc (kể cả khi app đóng) |
| TC-47 | UC-14 | Người bán đánh dấu Đã bán khi tin có 2 yêu cầu chờ khác | Lịch hẹn đã chốt → DONE; 2 yêu cầu còn lại CANCELLED, người gửi nhận thông báo |
| TC-48 | UC-37 | Bấm một thông báo giao dịch chưa đọc | Mở đúng phòng chat, cuộn tới thẻ sự kiện; số trên chuông giảm 1 |
| TC-50 | UC-40 | 3 tài khoản khác nhau báo cáo một tin | Tin chuyển HIDDEN |
| TC-51 | UC-40 | Một tài khoản báo cáo 2 lần | Lần 2 bị từ chối/thông báo đã báo cáo |
| TC-52 | UC-51 | Admin chọn "Giữ tin" | Tin về ACTIVE, báo cáo DISMISSED |

Kết quả kiểm thử ghi theo mẫu: `Mã TC | Ngày | Thiết bị | Kết quả (Pass/Fail) | Ghi chú/ảnh chụp`.

### 6.3.4. Đo hiệu năng (NFR-01, 02)
Mỗi chỉ số đo 10 lần trên máy thật, mạng 4G, ghi trung bình & lớn nhất vào bảng trong báo cáo.

## 6.4. Dữ liệu gieo (seed data)
- 30–50 trường đại học phổ biến ở Hà Nội, TP.HCM, Đà Nẵng (file `seed/schools.json`).
- Toàn bộ danh mục ở 03, mục 3.7 (`seed/categories.json`).
- 100–200 tin mẫu cho giai đoạn pilot: lấy từ đồ thật của bạn bè (có xin phép) hoặc tham khảo mức giá công khai; đánh dấu `isSeed = true` để **loại khỏi số liệu KPI** khi phân tích.

## 6.5. Kế hoạch chạy thử (pilot)

| Hạng mục | Nội dung |
|---|---|
| Thời gian | 2 tuần |
| Người tham gia | 20–30 sinh viên, ưu tiên 1–2 trường, cả người bán và người mua |
| Phân phối app | APK qua Google Drive hoặc Firebase App Distribution (hoặc Google Play kênh thử nghiệm nội bộ) |
| Nhiệm vụ tuần 1 | Mỗi người đăng ≥ 2 món đồ thật muốn bán; dùng thử tìm kiếm, lưu tin, chat |
| Nhiệm vụ tuần 2 | Đặt mua qua app, hẹn địa điểm và xác nhận; giao dịch thật nếu được; đánh dấu "Đã bán" và nhập giá thực tế |
| Kiểm thử khả dụng | Với 5 người: giao 4 nhiệm vụ có bấm giờ (đăng tin, tìm & lưu 1 món, đặt mua & chốt hẹn, đánh dấu đã bán), ghi lại tỷ lệ hoàn thành và thời gian |
| Thu thập | KPI từ dashboard; khảo sát sau pilot (phụ lục B: SUS + câu hỏi mở); log lỗi |
| Đầu ra cho báo cáo | So sánh KPI thực tế với mục tiêu O1–O5 (01, mục 1.3); MAPE gợi ý giá; điểm SUS; trích dẫn phản hồi |

## 6.6. Rủi ro dự án

| Rủi ro | Khả năng | Ảnh hưởng | Biện pháp |
|---|---|---|---|
| Ước lượng sai khối lượng (làm 1 mình) | Cao | Cao | Ưu tiên MoSCoW, quy tắc cắt giảm ở 6.2 |
| Chưa quen Firebase / Compose | Trung bình | Trung bình | Làm bài thử nhỏ ở S2, dùng codelab chính thức |
| Rules viết sai gây lỗ hổng | Trung bình | Cao | Test Rules bằng Emulator trước pilot |
| Ít người tham gia pilot | Trung bình | Cao | Mời sớm từ S7, có quà nhỏ, nhờ CLB/lớp hỗ trợ |
| Firebase thay đổi chính sách miễn phí | Thấp | Trung bình | Đặt cảnh báo ngân sách; phương án Cloudinary cho ảnh |
| Mất dữ liệu/mã nguồn | Thấp | Cao | Đẩy GitHub thường xuyên; export Firestore trước khi bảo vệ |

## 6.7. Định nghĩa hoàn thành (Definition of Done) cho mỗi chức năng
- [ ] Đúng các tiêu chí chấp nhận / BR liên quan
- [ ] Có xử lý trạng thái Đang tải / Rỗng / Lỗi
- [ ] Đã chạy các TC tương ứng và ghi kết quả
- [ ] Rules liên quan đã có test (nếu có ghi dữ liệu)
- [ ] Đã commit với mã UC/FR; có ảnh chụp màn hình cho báo cáo

## 6.8. Ánh xạ tài liệu → báo cáo

| Chương báo cáo | Lấy từ | Phần |
|---|---|---|
| 1. Tổng quan | 01 | toàn bộ |
| 2. Phân tích thị trường & khảo sát | 02 (2.1–2.4), phụ lục A, kết quả S0 | Kinh tế |
| 3. Mô hình kinh doanh & tài chính | 02 (2.5–2.12) | Kinh tế |
| 4. Phân tích yêu cầu | 03 | Kỹ thuật |
| 5. Thiết kế hệ thống & giao diện | 04, 05, ảnh Figma | Kỹ thuật |
| 6. Cài đặt & kiểm thử | 06 (6.3), ảnh chụp app, kết quả TC/UT | Kỹ thuật |
| 7. Đánh giá & kết luận | 06 (6.5), KPI, SUS, MAPE, so sánh với 2.9 | Cả hai |

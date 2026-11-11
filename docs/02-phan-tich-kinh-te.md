# 02 – Phân tích kinh tế

> ⚠️ Các con số có nhãn **[GĐ]** là **giả định minh hoạ**, phải thay bằng số liệu từ khảo sát (phụ lục) hoặc nguồn chính thức trước khi đưa vào báo cáo. Các con số có nhãn **[NGUỒN]** cần trích dẫn nguồn (Bộ GD&ĐT, Tổng cục Thống kê, báo cáo ngành…).

## 2.1. Cơ sở lý thuyết và cách áp dụng

| Lý thuyết | Nội dung tóm tắt | Áp dụng vào UniMarket | Tính năng tương ứng |
|---|---|---|---|
| **Thông tin bất cân xứng** – Akerlof (1970), "The Market for Lemons" | Người bán biết rõ chất lượng hàng hơn người mua → người mua chỉ sẵn sàng trả giá trung bình → hàng tốt rút khỏi thị trường, thị trường thu hẹp | Đồ cũ là thị trường "lemons" điển hình. App giảm bất cân xứng bằng tín hiệu (signaling) | Hồ sơ đầy đủ (SĐT, trường), đánh giá người bán, tình trạng chuẩn hoá, ảnh bắt buộc, gợi ý giá |
| **Chi phí giao dịch** – Coase, Williamson | Chi phí tìm kiếm, thương lượng, giám sát – thực thi làm giảm số giao dịch diễn ra | Giảm chi phí tìm kiếm (lọc, danh mục, phạm vi gần), chi phí thương lượng (giá tham khảo) và **chi phí thực thi** (thoả thuận trước địa điểm, giờ hẹn; nhắc lịch để giảm bùng hẹn) | Tìm kiếm, bộ lọc, gợi ý giá, chat, **đặt mua – lịch hẹn – thông báo** |
| **Thị trường hai mặt & hiệu ứng mạng lưới** – Rochet & Tirole | Nền tảng phục vụ 2 nhóm; giá trị cho nhóm này tăng theo quy mô nhóm kia | Nhiều người bán → nhiều lựa chọn → hút người mua → hút thêm người bán. Có bài toán "con gà – quả trứng" | Chiến lược ra mắt (mục 2.7), miễn phí đăng tin |
| **Kinh tế tuần hoàn** | Kéo dài vòng đời sản phẩm, giảm khai thác tài nguyên & rác thải | Mỗi giao dịch = 1 sản phẩm được tái sử dụng thay vì mua mới | Chỉ số tác động (KPI-08, KPI-09) |
| **Khấu hao tài sản** | Giá trị tài sản giảm theo thời gian sử dụng | Làm cơ sở công thức định giá khi chưa có dữ liệu | Gợi ý giá – chế độ "ước tính" |
| **Thặng dư người tiêu dùng / người sản xuất** | Chênh lệch giữa mức sẵn sàng trả/bán và giá giao dịch | Giá hợp lý giúp cả hai bên có thặng dư → giao dịch xảy ra nhiều hơn | Gợi ý giá dạng khoảng |

**Luận điểm chính cho báo cáo:** UniMarket không chỉ là "nơi đăng tin", mà là công cụ **giảm thông tin bất cân xứng và chi phí giao dịch** trong thị trường đồ cũ sinh viên, từ đó làm tăng số giao dịch hiệu quả và tạo giá trị kinh tế – môi trường.

### 2.1.1. Đánh đổi khi bỏ xác thực sinh viên (quyết định D8)

| | Có xác thực sinh viên (v1.0) | Hồ sơ đầy đủ + xác nhận địa điểm (v1.1) |
|---|---|---|
| Rào cản tham gia | Cao: phải chụp thẻ / có email trường, chờ duyệt | Thấp: tự nhập thông tin là dùng được ngay |
| Tác động lên hiệu ứng mạng lưới | Tăng chậm, mất người dùng ở bước duyệt | Tăng nhanh hơn, quan trọng ở giai đoạn cold start |
| Tín hiệu tin cậy | Mạnh (danh tính được kiểm tra) | Yếu hơn → bù bằng SĐT công khai cho thành viên, đánh giá, báo cáo, điểm hẹn công cộng trong trường |
| Rủi ro khi gặp mặt | Không giải quyết trực tiếp | Giảm: địa điểm và giờ được hai bên xác nhận, có nhắc lịch, khuyến nghị nơi công cộng |
| Chi phí vận hành | Admin duyệt ảnh thẻ thủ công, lưu dữ liệu nhạy cảm | Không có; chỉ quản lý danh sách điểm giao dịch |

**Lập luận:** ở giai đoạn đầu, giá trị nền tảng phụ thuộc chủ yếu vào **quy mô** (hiệu ứng mạng lưới), nên hạ rào cản gia nhập có lợi hơn. Rủi ro thực tế của mua bán đồ cũ sinh viên nằm nhiều ở **khâu gặp mặt** (bùng hẹn, gặp nơi vắng) hơn là ở danh tính. Vì vậy chuyển nguồn lực từ "xác minh người" sang "xác nhận giao dịch" là hợp lý. Pilot sẽ kiểm chứng bằng KPI-11 (tỷ lệ tin bị báo cáo), KPI-13 và KPI-14 (tỷ lệ hẹn được xác nhận / thành giao dịch).

## 2.2. Phân tích thị trường

### 2.2.1. Quy mô thị trường (TAM – SAM – SOM)

Công thức:

```
GMV (tổng giá trị giao dịch/năm) = Số sinh viên × Tỷ lệ có mua/bán đồ cũ × Giá trị giao dịch đồ cũ bình quân/người/năm
```

| Tầng | Định nghĩa | Số người | Ghi chú |
|---|---|---|---|
| **TAM** | Toàn bộ sinh viên đại học, cao đẳng Việt Nam | ~2.000.000+ **[NGUỒN]** | Trích số liệu Bộ GD&ĐT năm gần nhất |
| **SAM** | Sinh viên ở các thành phố lớn (Hà Nội, TP.HCM, Đà Nẵng, Huế, Cần Thơ…) có dùng Android và có nhu cầu đồ cũ | TAM × tỷ lệ ở TP lớn × tỷ lệ Android × tỷ lệ có nhu cầu **[GĐ]** | Tỷ lệ "có nhu cầu" lấy từ câu B1 khảo sát |
| **SOM** | Thị phần có thể đạt trong 1–3 năm | 0,5–2% SAM **[GĐ]** | Dựa trên chiến lược ra mắt từng thành phố |

Ví dụ tính minh hoạ **[GĐ]**: SAM = 2.000.000 × 60% × 70% × 60% ≈ 504.000 người; SOM năm 1 (1%) ≈ 5.000 người dùng hoạt động/tháng (MAU). Với giá trị đồ cũ bình quân 1.000.000đ/người/năm, GMV năm 1 ≈ 5 tỷ đồng.

### 2.2.2. Đặc điểm thị trường
- **Tính mùa vụ rõ rệt**: cung tăng mạnh tháng 5–7 (tốt nghiệp, hết năm học), cầu tăng mạnh tháng 8–10 (nhập học). → Marketing nên làm trước 2 đỉnh này; app nên có chiến dịch "Thanh lý mùa tốt nghiệp" và "Tân sinh viên săn đồ".
- **Tính địa phương**: giao dịch chủ yếu trong bán kính một thành phố → mạng lưới hình thành theo từng cụm trường.
- **Khách hàng nhạy cảm giá**: thu nhập thấp → phí phải rất nhỏ hoặc miễn phí cho tính năng cơ bản.

### 2.2.3. Phân tích đối thủ

| Tiêu chí | Group Facebook | Chợ Tốt | Shopee/Lazada (đồ cũ) | **UniMarket** |
|---|---|---|---|---|
| Đối tượng | Hỗn hợp | Đại trà | Đại trà, thiên hàng mới | **Chỉ sinh viên** |
| Chi phí đăng | Miễn phí | Miễn phí + phí đẩy tin | Phí sàn | Miễn phí + đẩy tin (tuỳ chọn) |
| Phân loại, lọc | Không | Có | Có | Có + **lọc theo trường** |
| Định giá | Không | Không | Không | **Có gợi ý giá** |
| Thông tin người dùng | Không bắt buộc | SĐT | Có (người bán) | **Hồ sơ đầy đủ bắt buộc** (SĐT, trường, điểm giao dịch) |
| Thoả thuận địa điểm gặp | Tự nhắn tin | Tự nhắn tin | Không áp dụng (giao hàng) | **Đặt mua – xác nhận địa điểm – nhắc lịch** |
| Giáo trình theo môn/trường | Không | Không | Không | **Có** |
| Lượng người dùng | Rất lớn | Rất lớn | Rất lớn | Nhỏ (điểm yếu) |

**Kết luận:** UniMarket cạnh tranh bằng **ngách** (sinh viên) và **tính năng chuyên biệt** (gợi ý giá, lọc theo trường, hẹn giao dịch có xác nhận địa điểm), không cạnh tranh trực diện về quy mô.

## 2.3. Khảo sát người dùng

- **Mục tiêu:** kiểm chứng vấn đề, đo nhu cầu, xác định mức sẵn sàng chi trả.
- **Phương pháp:** bảng hỏi Google Form (phụ lục A), chọn mẫu thuận tiện qua các group sinh viên nhiều trường.
- **Cỡ mẫu mục tiêu:** ≥ 100 phiếu hợp lệ, ít nhất 3 trường, 2 thành phố.
- **Giả thuyết cần kiểm định:**

| Mã | Giả thuyết | Câu hỏi đo | Ngưỡng chấp nhận |
|---|---|---|---|
| H1 | Đa số sinh viên từng mua hoặc bán đồ cũ | B1 | ≥ 50% |
| H2 | "Không biết định giá" là khó khăn đáng kể của người bán | C2 | Điểm Likert TB ≥ 3,5/5 |
| H3 | "Sợ bị lừa / không tin chất lượng" là khó khăn lớn nhất của người mua | C4, C5 | Nằm trong top 2 |
| H4 | Sinh viên muốn lọc theo trường/khu vực | D1 | ≥ 60% chọn "quan trọng" trở lên |
| H5 | Có một tỷ lệ sẵn sàng trả phí đẩy tin nhỏ | E1, E2 | ≥ 15% sẵn sàng trả ≥ 5.000đ |

- **Phân tích:** thống kê mô tả (tỷ lệ %, trung bình, độ lệch chuẩn), bảng chéo theo năm học/thành phố; có thể kiểm định Chi-square cho các bảng chéo nếu đủ mẫu.

## 2.4. Phân tích SWOT

| | Tích cực | Tiêu cực |
|---|---|---|
| **Nội bộ** | **S – Điểm mạnh**<br>• Tập trung ngách sinh viên<br>• Gợi ý giá – khác biệt rõ<br>• Lọc theo trường, giáo trình theo môn<br>• Chi phí vận hành rất thấp (Firebase) | **W – Điểm yếu**<br>• Chưa có người dùng (cold start)<br>• Một người phát triển, nguồn lực hạn chế<br>• Gợi ý giá kém chính xác khi ít dữ liệu<br>• Không có thanh toán đảm bảo<br>• Không xác minh danh tính → có thể có tài khoản ảo |
| **Bên ngoài** | **O – Cơ hội**<br>• Hơn 2 triệu sinh viên, nhu cầu lặp lại hàng năm<br>• Xu hướng tiêu dùng bền vững, đồ second-hand<br>• Có thể hợp tác Đoàn – Hội sinh viên, CLB<br>• Quảng cáo địa phương quanh trường | **T – Thách thức**<br>• Group Facebook miễn phí, quen thuộc<br>• Chợ Tốt có thể làm tính năng tương tự<br>• Rủi ro lừa đảo ảnh hưởng uy tín<br>• Tính mùa vụ gây biến động người dùng |

**Chiến lược rút ra:** S-O: dùng gợi ý giá + "hẹn gặp có xác nhận" làm thông điệp marketing mùa tốt nghiệp/nhập học. W-O: hợp tác CLB để giải bài toán cold start. S-T: tạo cộng đồng tin cậy (hồ sơ đầy đủ, đánh giá, điểm hẹn công cộng trong trường) – thứ group Facebook không có. W-T: khởi đầu hẹp ở 1 thành phố, đo KPI trước khi mở rộng.

## 2.5. Business Model Canvas

| Khối | Nội dung |
|---|---|
| **1. Phân khúc khách hàng** | Sinh viên người bán; sinh viên người mua; (phụ) cửa hàng/dịch vụ quanh trường – khách quảng cáo |
| **2. Giá trị đề xuất** | Bán nhanh – mua rẻ – đúng giá; hẹn gặp có xác nhận địa điểm; lọc theo trường |
| **3. Kênh** | Google Play; group Facebook/Zalo trường; CLB, Đoàn – Hội; poster QR tại ký túc xá |
| **4. Quan hệ khách hàng** | Tự phục vụ; cộng đồng (đánh giá); thông báo đẩy; chiến dịch theo mùa |
| **5. Dòng doanh thu** | Đẩy tin, tin nổi bật; quảng cáo địa phương; gói "Shop sinh viên" (tương lai) |
| **6. Nguồn lực chính** | Ứng dụng & dữ liệu giá; cộng đồng người dùng; thương hiệu "dành cho sinh viên" |
| **7. Hoạt động chính** | Phát triển app; kiểm duyệt nội dung; marketing theo mùa; phát triển cộng đồng |
| **8. Đối tác chính** | Google Firebase; CLB/Đoàn – Hội sinh viên; cửa hàng quanh trường |
| **9. Cơ cấu chi phí** | Phí Google Play; hạ tầng Firebase; marketing; thời gian phát triển & kiểm duyệt |

## 2.6. Mô hình doanh thu

Mô hình **Freemium**: tính năng cốt lõi miễn phí để tối đa hoá hiệu ứng mạng lưới; thu phí các tính năng tăng hiển thị.

| Nguồn thu | Mô tả | Giá đề xuất **[GĐ]** | Trạng thái trong app |
|---|---|---|---|
| Đẩy tin | Tin lên đầu mục "Nổi bật" trong 3 ngày | 10.000đ/lượt | **Mô phỏng** (UC-17) |
| Tin nổi bật theo mùa | Gói 7 ngày mùa tốt nghiệp/nhập học | 20.000đ/gói | Ý tưởng |
| Quảng cáo địa phương | Banner của quán ăn, nhà trọ, tiệm photo quanh trường | 500.000–1.000.000đ/tháng/đối tác | Ý tưởng |
| Gói Shop sinh viên | Cho người bán thường xuyên: tăng giới hạn tin, thống kê | 50.000đ/tháng | Ý tưởng |

**Định giá phí đẩy tin**: dùng câu hỏi độ nhạy giá (phụ lục, mục E – phương pháp Van Westendorp rút gọn) để xác định khoảng giá chấp nhận được.

**Lý do không thu phí đăng tin:** ở giai đoạn đầu, giá trị nền tảng phụ thuộc số lượng tin. Thu phí đăng tin sẽ làm giảm cung → giảm cầu → vòng xoáy ngược của hiệu ứng mạng lưới.

## 2.7. Chiến lược ra mắt (giải bài toán cold start)

| Giai đoạn | Thời điểm gợi ý | Hành động | Mục tiêu |
|---|---|---|---|
| 0. Gieo cung | Trước ra mắt | Tự tạo 100–200 tin mẫu từ dữ liệu thật (xin phép bạn bè); mời 10 người bán đầu tiên | Không để app "trống" |
| 1. Một cụm trường | Pilot 2 tuần | Ra mắt ở 1 trường/1 thành phố, đại sứ sinh viên, poster QR | Đạt O1–O5 |
| 2. Theo mùa | Tháng 5–7 | Chiến dịch "Thanh lý mùa tốt nghiệp" – tập trung người bán | Tăng cung |
| 3. Theo mùa | Tháng 8–10 | Chiến dịch "Tân sinh viên săn đồ" – tập trung người mua | Tăng cầu, giao dịch |
| 4. Mở rộng | Năm 2 | Nhân rộng sang thành phố khác theo mô hình đã chứng minh | Tăng MAU |

Nguyên tắc: **ưu tiên phía cung trước** (người bán), vì người mua chỉ ở lại khi có hàng.

## 2.8. Cơ cấu chi phí

### Chi phí ban đầu
| Khoản | Số tiền | Ghi chú |
|---|---|---|
| Tài khoản Google Play Developer | 25 USD (≈ 650.000đ) | Trả một lần |
| Thiết kế logo, nhận diện | 0đ | Tự làm |
| Phát triển app | 0đ tiền mặt | Chi phí cơ hội: thời gian của người làm |

### Chi phí vận hành hàng tháng **[GĐ]**
| Khoản | Bi quan | Cơ sở | Lạc quan |
|---|---|---|---|
| Firebase (vượt hạn mức miễn phí) | 0đ | 300.000đ | 1.500.000đ |
| Marketing (in poster, quảng cáo group, quà tặng) | 2.000.000đ | 3.000.000đ | 5.000.000đ |
| **Tổng** | **2.000.000đ** | **3.300.000đ** | **6.500.000đ** |

Chưa tính lương nhân sự (giả định người làm tự vận hành). Báo cáo nên nêu rõ giả định này và thêm một kịch bản có tính lương nếu muốn đánh giá thận trọng.

## 2.9. Dự báo tài chính năm 1 (theo tháng, ở trạng thái ổn định cuối năm)

### Công thức
```
Doanh thu đẩy tin = MAU × Tỷ lệ người bán × Tỷ lệ mua đẩy tin × Số lượt/tháng × Giá/lượt
Doanh thu quảng cáo = Số đối tác × Phí/tháng
Lợi nhuận = Tổng doanh thu − Tổng chi phí
```

### Tham số **[GĐ]**
| Tham số | Giá trị |
|---|---|
| Tỷ lệ người bán / MAU | 30% |
| Tỷ lệ người bán mua đẩy tin | 10% |
| Số lượt đẩy / người / tháng | 2 |
| Giá / lượt | 10.000đ |
| ⇒ Doanh thu đẩy tin trên mỗi MAU | 600đ/tháng |

### Ba kịch bản
| Chỉ tiêu (đ/tháng) | Bi quan | Cơ sở | Lạc quan |
|---|---|---|---|
| MAU | 1.000 | 5.000 | 15.000 |
| Doanh thu đẩy tin | 600.000 | 3.000.000 | 9.000.000 |
| Số đối tác quảng cáo × phí | 1 × 500.000 | 5 × 500.000 | 10 × 1.000.000 |
| Doanh thu quảng cáo | 500.000 | 2.500.000 | 10.000.000 |
| **Tổng doanh thu** | **1.100.000** | **5.500.000** | **19.000.000** |
| Tổng chi phí | 2.000.000 | 3.300.000 | 6.500.000 |
| **Lợi nhuận** | **−900.000** | **+2.200.000** | **+12.500.000** |

### Điểm hoà vốn (kịch bản cơ sở)
- ARPU (doanh thu bình quân/MAU/tháng) = 5.500.000 / 5.000 = **1.100đ**.
- MAU hoà vốn ≈ Chi phí / ARPU = 3.300.000 / 1.100 ≈ **3.000 MAU**.
- Diễn giải: cần ~3.000 người dùng hoạt động mỗi tháng để bù chi phí vận hành (chưa tính lương).

### Phân tích độ nhạy (gợi ý làm trong báo cáo)
Thay đổi từng tham số ±20% (tỷ lệ mua đẩy tin, giá/lượt, số đối tác quảng cáo) và quan sát lợi nhuận → chỉ ra tham số ảnh hưởng mạnh nhất. Có thể vẽ biểu đồ "tornado" bằng Excel.

## 2.10. Hệ thống chỉ số (KPI)

**North Star Metric:** `KPI-01` – Số giao dịch thành công mỗi tháng (tin chuyển sang "Đã bán").

| Mã | Chỉ số | Công thức | Nguồn dữ liệu | Nhóm (AARRR) |
|---|---|---|---|---|
| KPI-01 | Giao dịch thành công/tháng | Đếm tin `SOLD` theo `soldAt` | `listings` | Doanh thu/giá trị |
| KPI-02 | Người dùng mới/tuần | Đếm `users` theo `createdAt` | `users` | Acquisition |
| KPI-03 | Tỷ lệ kích hoạt | Người đăng ≥1 tin hoặc lưu ≥1 tin trong 7 ngày đầu / người mới | `listings`, `favorites` | Activation |
| KPI-04 | MAU | Người có `lastActiveAt` trong 30 ngày | `users` | Retention |
| KPI-05 | Tỷ lệ bán được | Tin `SOLD` / tổng tin đã đăng | `listings` | Giá trị |
| KPI-06 | Thời gian bán trung bình | TB (`soldAt` − `createdAt`) | `listings` | Giá trị |
| KPI-07 | Tỷ lệ dùng giá gợi ý | Tin có \|giá rao − giá gợi ý\| ≤ 10% / tin có gợi ý | `listings.suggestedPrice` | Tính năng |
| KPI-08 | Sai số gợi ý giá (MAPE) | TB \|giá bán thực tế − giá gợi ý\| / giá bán thực tế | `listings` | Tính năng |
| KPI-09 | Tiền sinh viên tiết kiệm | Σ (giá mua mới ước tính − giá bán thực tế) | `listings.originalPrice`, `soldPrice` | Tác động |
| KPI-10 | Số sản phẩm được tái sử dụng | = KPI-01 luỹ kế | `listings` | Tác động môi trường |
| KPI-11 | Tỷ lệ tin bị báo cáo | Tin có báo cáo / tổng tin | `reports` | An toàn |
| KPI-12 | Doanh thu mô phỏng | Σ lượt đẩy tin × giá | `boosts` | Revenue |
| KPI-13 | Tỷ lệ yêu cầu mua được xác nhận | Lịch hẹn từng đạt `CONFIRMED` / tổng yêu cầu mua | `appointments` | Tính năng |
| KPI-14 | Tỷ lệ hẹn thành giao dịch | Lịch hẹn `DONE` / lịch hẹn `CONFIRMED` | `appointments` | Giá trị |
| KPI-15 | Thời gian phản hồi TB của người bán | TB (`respondedAt` − `createdAt`) của yêu cầu mua | `appointments` | Tính năng |

KPI-01, 04, 05, 06, 07, 12, 13, 14 được hiển thị trên màn hình Admin Dashboard (SCR-24).

## 2.11. Đánh giá tác động kinh tế – xã hội

- **Kinh tế cá nhân:** sinh viên mua tiết kiệm (KPI-09), người bán thu hồi một phần vốn.
- **Hiệu quả thị trường:** giảm thời gian bán (KPI-06), giảm chênh lệch giá (KPI-08) → minh chứng cho việc giảm chi phí giao dịch và bất cân xứng thông tin.
- **Môi trường:** mỗi sản phẩm tái sử dụng là một sản phẩm không phải sản xuất mới (KPI-10). Có thể quy đổi minh hoạ sang lượng rác thải tránh được nếu tìm được hệ số tham chiếu **[NGUỒN]**.

## 2.12. Rủi ro kinh doanh

| Rủi ro | Khả năng | Ảnh hưởng | Biện pháp |
|---|---|---|---|
| Không đủ người dùng (cold start) | Cao | Cao | Gieo tin mẫu, ra mắt hẹp, chiến dịch theo mùa |
| Lừa đảo, hàng kém chất lượng | Trung bình | Cao | Hồ sơ đầy đủ bắt buộc, điểm hẹn công cộng được hai bên xác nhận, đánh giá, báo cáo |
| Tài khoản ảo, thông tin khai sai (do không xác thực) | Trung bình | Trung bình | SĐT hiển thị cho thành viên, khoá tài khoản khi bị báo cáo, giới hạn số yêu cầu mua đang chờ |
| Bùng hẹn, không đến điểm giao dịch | Cao | Trung bình | Xác nhận địa điểm – giờ, nhắc trước 1 giờ, huỷ phải nêu lý do; theo dõi KPI-14 |
| Người dùng quay lại group Facebook | Cao | Trung bình | Khác biệt bằng gợi ý giá, lọc trường, chia sẻ tin ra Facebook bằng link |
| Doanh thu thấp do sinh viên nhạy cảm giá | Cao | Trung bình | Dựa nhiều vào quảng cáo địa phương, phí đẩy tin rất thấp |
| Đối thủ lớn sao chép tính năng | Thấp | Cao | Xây cộng đồng & thương hiệu sinh viên |
| Chi phí hạ tầng tăng khi người dùng tăng | Thấp | Trung bình | Nén ảnh, phân trang, đặt cảnh báo ngân sách |

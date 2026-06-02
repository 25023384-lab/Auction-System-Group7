# Hệ thống Đấu giá Trực tuyến (Auction System)

## 1. Mô tả bài toán và Phạm vi hệ thống

Dự án xây dựng một hệ thống đấu giá trực tuyến client-server, cho phép người dùng tham gia vào các phiên đấu giá vật phẩm trong thời gian thực.

**Phạm vi hệ thống:**
- **Người dùng:** Hệ thống hỗ trợ 3 vai trò chính: Người bán (Seller), Người đấu giá (Bidder), và Quản trị viên (Admin).
- **Vật phẩm:** Người bán có thể đăng bán các vật phẩm với giá khởi điểm và thời gian đấu giá cụ thể.
- **Đấu giá:** Người đấu giá có thể xem danh sách vật phẩm, theo dõi và đặt giá. Hệ thống tự động cập nhật giá cao nhất và thông báo cho những người dùng khác.
- **Kiến trúc:** Hệ thống được xây dựng theo mô hình Client-Server, sử dụng giao thức TCP/IP để giao tiếp. Server chịu trách nhiệm xử lý toàn bộ logic nghiệp vụ và quản lý trạng thái, trong khi Client cung cấp giao diện đồ họa cho người dùng tương tác.

---

## 2. Công nghệ sử dụng và Yêu cầu cài đặt

### Công nghệ

- **Agent hỗ trợ: ** Chat gpt, Claude, Copilot, Gemini
- **Ngôn ngữ lập trình:** Java 11+
- **Giao diện người dùng (GUI):** JavaFX
- **Cơ sở dữ liệu:** SQLite
- **Giao tiếp mạng:** Java Sockets (TCP)
- **Xử lý JSON:** Jackson Databind
- **Mã hóa mật khẩu:** jBCrypt
- **Build tool:** Maven

### Môi trường chạy và Cài đặt

1.  **JDK (Java Development Kit):** Yêu cầu cài đặt JDK phiên bản 11 trở lên.
2.  **Maven:** Yêu cầu cài đặt Apache Maven để build dự án.
3.  **IDE (Tùy chọn):** IntelliJ IDEA hoặc Eclipse để phát triển và chạy ứng dụng từ mã nguồn.

---

## 3. Cấu trúc thư mục và Module chính

Dự án được tổ chức theo các package có trách nhiệm rõ ràng:

```
src/main/java/com/auction/
├── app/
│   └── Main.java                         # Hàm main chính, điều hướng chạy Server hoặc Client tùy thuộc tham số đầu vào
├── client/
│   ├── api/
│   │   ├── AuctionApiClient.java         # Gọi các API liên quan đến sản phẩm & phiên đấu giá từ Client
│   │   └── AuthApiClient.java            # Gọi các API liên quan đến đăng ký & đăng nhập từ Client
│   ├── AuctionClient.java                # Khởi chạy ứng dụng giao diện JavaFX phía Client
│   └── ClientConnection.java             # Quản lý luồng gửi/nhận gói tin qua Socket TCP với Server
├── controller/
│   ├── admin/
│   │   └── AdminDashboardController.java # Điều khiển màn hình quản trị của Admin
│   ├── auction/
│   │   ├── AuctionController.java        # Điều khiển giao diện phòng đấu giá chính
│   │   ├── CreateItemController.java     # Điều khiển giao diện đăng bán sản phẩm mới
│   │   ├── ItemDetailController.java     # Điều khiển giao diện chi tiết sản phẩm và đặt giá
│   │   └── MyItemsController.java        # Điều khiển giao diện danh sách vật phẩm của bản thân
│   ├── auth/
│   │   ├── LoginController.java          # Điều khiển màn hình đăng nhập
│   │   └── RegisterController.java       # Điều khiển màn hình đăng ký
│   ├── component/
│   │   └── DateTimePicker.java           # Thành phần UI chọn ngày/giờ tuỳ chỉnh
│   └── navigation/
│       ├── MainLayoutController.java     # Quản lý bố cục ứng dụng và thanh menu điều hướng
│       └── WelcomeController.java        # Điều khiển màn hình chào mừng ban đầu
├── dao/
│   ├── AutoBidDAO.java                   # Thao tác CSDL SQLite cho cấu hình tự động đấu giá
│   ├── BidTransactionDAO.java            # Thao tác CSDL SQLite lưu vết lịch sử các lượt đặt giá
│   ├── ItemDAO.java                      # Thao tác CSDL SQLite cho thông tin các vật phẩm
│   └── UserDAO.java                      # Thao tác CSDL SQLite cho thông tin người dùng
├── dto/
│   ├── auth/
│   │   ├── LoginRequest.java             # Gói dữ liệu gửi yêu cầu đăng nhập
│   │   └── RegisterRequest.java          # Gói dữ liệu gửi yêu cầu đăng ký
│   └── bid/
│       ├── AutoBidRequest.java           # Gói dữ liệu thiết lập tự động đặt giá
│       └── BidRequest.java               # Gói dữ liệu gửi lượt đặt giá
├── entity/
│   ├── bid/
│   │   ├── AutoBid.java                  # Thực thể cấu hình tự động đấu giá (giá trần tối đa)
│   │   └── BidTransaction.java           # Thực thể chi tiết một lượt đặt giá cụ thể
│   ├── factory/
│   │   └── ItemFactory.java              # Factory khởi tạo các loại sản phẩm (Art, Electronics,...)
│   ├── items/
│   │   ├── Art.java                      # Lớp vật phẩm nghệ thuật kế thừa từ Item
│   │   ├── Electronics.java              # Lớp vật phẩm điện tử kế thừa từ Item
│   │   ├── Item.java                     # Lớp thực thể cơ sở trừu tượng cho mọi vật phẩm đấu giá
│   │   └── Vehicle.java                  # Lớp vật phẩm phương tiện kế thừa từ Item
│   ├── message/
│   │   └── Message.java                  # Định dạng gói tin giao tiếp qua Socket (JSON) giữa Client-Server
│   ├── user/
│   │   ├── Admin.java                    # Thực thể người dùng quản trị viên
│   │   ├── Bidder.java                   # Thực thể người dùng đấu giá
│   │   ├── Seller.java                   # Thực thể người dùng bán hàng
│   │   └── User.java                     # Thực thể cơ sở cho người dùng (ID, username, balance)
│   └── Entity.java                       # Interface hoặc lớp cơ sở cho mọi thực thể trong CSDL
├── event/
│   ├── BidNotificationListener.java      # Lớp lắng nghe sự kiện thay đổi giá để gửi thông báo
│   └── BidObserver.java                  # Interface Observer định nghĩa hành vi quan sát lượt đấu giá
├── exception/
│   ├── AuctionClosedException.java       # Ngoại lệ khi phiên đấu giá đã kết thúc nhưng vẫn đặt giá
│   ├── AuctionException.java             # Ngoại lệ cơ sở cho các lỗi nghiệp vụ trong hệ thống
│   ├── AuthenticationException.java      # Ngoại lệ xảy ra khi đăng nhập thất bại
│   └── InvalidBidException.java          # Ngoại lệ đặt giá không hợp lệ (thấp hơn giá tối thiểu yêu cầu)
├── server/
│   ├── handler/
│   │   ├── AdminHandler.java             # Xử lý các yêu cầu quản lý người dùng của Admin
│   │   ├── AuthHandler.java              # Xử lý các yêu cầu đăng nhập/đăng ký từ client
│   │   ├── BidHandler.java               # Xử lý yêu cầu đặt giá trực tiếp và tự động đấu giá
│   │   ├── ItemHandler.java              # Xử lý các nghiệp vụ liên quan đến vật phẩm đấu giá
│   │   └── PaymentHandler.java           # Xử lý nạp tiền, thanh toán đơn hàng thành công, huỷ đơn
│   └── AuctionServer.java                # Khởi chạy socket server, quản lý kết nối và xử lý yêu cầu
├── service/
│   ├── analytics/
│   │   └── AnalyticsService.java         # Dịch vụ tính toán số liệu thống kê phiên đấu giá
│   ├── auction/
│   │   ├── AntiSniping.java              # Logic tự động gia hạn thời gian nếu có bid ở những giây cuối
│   │   └── AuctionManager.java           # Bộ quản lý trung tâm điều phối trạng thái mọi phiên đấu giá
│   ├── auth/
│   │   └── AuthService.java              # Dịch vụ xử lý đăng nhập, đăng ký và bảo mật tài khoản
│   ├── bidding/
│   │   ├── AutoBidder.java               # Xử lý logic tự động khớp đặt giá thay người dùng (Proxy Bidding)
│   │   ├── BidAnalytics.java             # Dịch vụ phân tích lịch sử và bước giá của sản phẩm
│   │   └── BiddingService.java           # Nghiệp vụ xử lý đặt giá, so khớp và kiểm tra tính hợp lệ
│   ├── item/
│   │   └── ItemService.java              # Nghiệp vụ quản lý, truy xuất và thay đổi thông tin sản phẩm
│   ├── network/
│   │   └── AuctionNetworkService.java    # Đóng gói và điều hướng thông tin truyền tải qua mạng
│   ├── notification/
│   │   └── NotificationService.java      # Quản lý danh sách người theo dõi và gửi thông báo cập nhật
│   ├── realtime/
│   │   └── RealtimeNotifier.java         # Phát thông báo thay đổi thời gian thực tới tất cả client
│   ├── scheduler/
│   │   ├── AuctionScheduler.java         # Lập lịch chạy ngầm để đóng/mở phiên đấu giá tự động
│   │   └── BidTask.java                  # Nhiệm vụ lập lịch đóng/mở định kỳ cho một sản phẩm cụ thể
│   └── strategy/
│       ├── BiddingStrategy.java          # Chiến lược đặt giá (Strategy Pattern)
│       └── DefaultBiddingStrategy.java   # Chiến lược đặt giá mặc định của hệ thống
└── util/
    ├── AuctionUIHelper.java              # Hỗ trợ hiển thị và định dạng các thành phần JavaFX
    ├── DBHelper.java                     # Quản lý kết nối Database SQLite (Singleton Pattern)
    ├── DialogManager.java                # Hiển thị các hộp thoại Alert/Confirm/Error thống nhất
    └── HashGen.java                      # Tiện ích băm và xác thực mật khẩu người dùng (BCrypt)
```

---

## 4. Vị trí file .jar

Sau khi build dự án bằng Maven, file `.jar` thực thi sẽ nằm tại thư mục:

`target/auction-system-1.0-SNAPSHOT.jar`

*(Lưu ý: Tên file có thể thay đổi tùy theo phiên bản trong file `pom.xml`)*

---

## 5. Hướng dẫn chạy Server/Client

**Quan trọng:** Luôn luôn phải khởi động **Server trước** khi khởi động Client.

### Bước 1: Build dự án

Mở Terminal hoặc Command Prompt tại thư mục gốc của dự án và chạy lệnh sau để build file `.jar`:

```bash
mvn clean package
```

### Bước 2: Chạy Server

Trong Terminal, chạy lệnh sau:

```bash
java -jar target/auction-system-1.0-SNAPSHOT.jar server
```

Khi Server khởi động thành công, bạn sẽ thấy thông báo tương tự như: `Server is listening on port 12345`.

### Bước 3: Chạy Client

Mở một hoặc nhiều cửa sổ Terminal mới và chạy lệnh sau để khởi động mỗi Client:

```bash
java -jar target/auction-system-1.0-SNAPSHOT.jar
```

Mỗi lệnh sẽ mở một cửa sổ giao diện đăng nhập của ứng dụng.

---

## 6. Danh sách chức năng đã hoàn thành

- [x] **Quản lý người dùng:** Đăng nhập, Đăng ký với 3 vai trò (Bidder, Seller, Admin).
- [x] **Bảo mật:** Mật khẩu người dùng được mã hóa an toàn bằng BCrypt.
- [x] **Quản lý vật phẩm:** Seller có thể đăng bán vật phẩm với đầy đủ thông tin (giá, thời gian, mô tả).
- [x] **Đấu giá thời gian thực:** Bidder có thể đặt giá, giá cao nhất được cập nhật ngay lập tức cho tất cả người dùng đang xem.
- [x] **Tự động hóa phiên đấu giá:** Hệ thống tự động chuyển trạng thái vật phẩm từ `Sắp diễn ra` -> `Đang diễn ra` -> `Đã kết thúc` dựa trên thời gian đã lên lịch.
- [x] **Tính năng nâng cao:**
    - [x] **Chống "Sniping":** Tự động gia hạn thời gian đấu giá nếu có người đặt giá vào những giây cuối cùng.
    - [x] **Đấu giá tự động (Proxy Bidding):** Người dùng có thể thiết lập một mức giá tối đa, hệ thống sẽ tự động đặt giá giúp họ một cách thông minh.
    - [x] **Phân tích dữ liệu:** Cung cấp các số liệu thống kê cơ bản về một phiên đấu giá (tổng số lượt, giá trung bình, v.v.).
    - [x] **Hệ thống thông báo:** Người dùng có thể "theo dõi" vật phẩm để nhận thông báo.

---

## 7. Link báo cáo và Video Demo

- **Báo cáo PDF:** https://drive.google.com/file/d/1Z7cm6GqY02FkX8Bp_SLiM6VKlN0zI2v6/view?usp=sharing
- **Video Demo:** https://drive.google.com/file/d/1ApIZNU2bBMHPWzsSSx8LlIzEpjEJY5G3/view?usp=sharing

---

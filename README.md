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
│   └── MainApp.java             # Khởi chạy JavaFX và thiết lập kết nối ban đầu
├── client/
│   ├── network/
│   │   └── ServerProxy.java     # Đại diện kết nối phía server (Socket/RMI)
│   └── controller/
│       ├── LoginController.java
│       ├── DashboardController.java
│       └── AuctionRoomController.java
├── server/
│   ├── AuctionServer.java       # Lắng nghe kết nối từ các Client
│   ├── ClientHandler.java       # Quản lý từng luồng (Thread) cho mỗi Client
│   └── ServerConfig.java        # Cấu hình Port, Thread Pool, Database URL
├── dao/
│   ├── BaseDAO.java             # Interface chung cho CRUD
│   ├── UserDAO.java             # Thực thi truy vấn cho bảng User
│   ├── ItemDAO.java             # Thực thi truy vấn cho bảng Item
│   └── BidDAO.java              # Lưu lịch sử đặt giá
├── entity/
│   ├── User.java                # Thông tin người dùng (id, username, balance)
│   ├── Item.java                # Thông tin sản phẩm đấu giá
│   ├── Bid.java                 # Lịch sử một lần đặt giá
│   └── AuctionSession.java      # Trạng thái phiên (đang diễn ra, kết thúc)
├── exception/
│   ├── InsufficientBalanceException.java  # Lỗi không đủ tiền đặt giá
│   ├── InvalidBidException.java           # Lỗi đặt giá thấp hơn giá hiện tại
│   └── AuctionClosedException.java        # Lỗi đặt giá khi phiên đã kết thúc
├── service/
│   ├── auction/
│   │   ├── AuctionManager.java      # Điều phối toàn bộ phiên đấu giá
│   │   └── SnipingProtector.java    # Logic tự động gia hạn thời gian nếu có bid phút cuối
│   ├── auth/
│   │   ├── AuthService.java         # Xác thực tài khoản
│   │   └── PasswordHasher.java      # Mã hóa mật khẩu (BCrypt)
│   ├── bidding/
│   │   ├── BidProcessor.java        # Xử lý logic so sánh giá, cập nhật Winner
│   │   └── AutoBidAgent.java        # Logic cho Bot hoặc tính năng tự động đặt giá
│   ├── factory/
│   │   ├── ItemFactory.java         # Khởi tạo các loại Item (Electronics, Art,...)
│   │   └── AuctionFactory.java      # Tạo các kiểu phiên (English, Dutch Auction)
│   ├── notification/
│   │   ├── NotificationService.java # Gửi thông báo đến Client
│   │   └── Subject.java             # Interface trong Observer Pattern
│   ├── realtime/
│   │   └── CountdownTimer.java      # Quản lý đồng hồ đếm ngược cho mỗi Item
│   └── scheduler/
│       └── AuctionTask.java         # Task chạy ngầm để đóng/mở phiên theo giờ
└── util/                        # (Bổ sung) Các tiện ích dùng chung
    ├── DatabaseConnection.java  # Singleton quản lý kết nối SQLite
    └── Validator.java           # Kiểm tra dữ liệu đầu vào
```

---

## 4. Vị trí file .jar

Sau khi build dự án bằng Maven, file `.jar` thực thi sẽ nằm tại thư mục:

`target/Auction-System-Group7-pi-1.0-SNAPSHOT.jar`

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

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
├── app/                # Chứa lớp Main, điểm khởi đầu của ứng dụng.
├── client/             # Các lớp liên quan đến phía Client (GUI Controllers, networking).
├── server/             # Các lớp liên quan đến phía Server (AuctionServer, ClientHandler).
├── dao/                # Data Access Objects - Các lớp giao tiếp trực tiếp với CSDL.
├── entity/             # Các lớp thực thể (POJO) như User, Item, BidTransaction.
├── exception/          # Các lớp ngoại lệ tùy chỉnh của ứng dụng.
├── service/            # Lõi nghiệp vụ của hệ thống.
│   ├── auction/        # Quản lý phiên đấu giá, chống sniping.
│   ├── auth/           # Xử lý đăng nhập, đăng ký.
│   ├── bidding/        # Xử lý logic đặt giá, auto-bid, phân tích.
│   ├── factory/        # Factory Pattern để tạo các loại Item.
│   ├── notification/   # Dịch vụ thông báo (Observer Pattern).
│   ├── realtime/       # Xử lý các tác vụ thời gian thực, đếm ngược.
│   └── scheduler/      # Tự động bắt đầu/kết thúc phiên đấu giá.
└── resources/
    ├── fxml/           # Các file FXML định nghĩa giao diện.
    └── auction.db      # File cơ sở dữ liệu SQLite.
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
java -jar target/Auction-System-Group7-pi-1.0-SNAPSHOT.jar server
```

Khi Server khởi động thành công, bạn sẽ thấy thông báo tương tự như: `Server is listening on port 12345`.

### Bước 3: Chạy Client

Mở một hoặc nhiều cửa sổ Terminal mới và chạy lệnh sau để khởi động mỗi Client:

```bash
java -jar target/Auction-System-Group7-pi-1.0-SNAPSHOT.jar
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

- **Báo cáo PDF:** `[Link đến file báo cáo của bạn]`
- **Video Demo:** `[Link đến video trên YouTube, Google Drive, v.v.]`

---
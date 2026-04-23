package src;

public class Main {
    public static void main(String[] args) {
        // 1. Lấy thực thể duy nhất của hệ thống quản lý đấu giá (Singleton Pattern)
        AuctionManager manager = AuctionManager.getInstance();

        // 2. Tạo sản phẩm đấu giá (Dùng các lớp con của Item mà Nhật/Tùng đã viết)
        // Ví dụ: Laptop thuộc lớp Electronics (nếu Nhật đã tạo lớp này)
        Item laptop = new Electronics("L01", "MacBook Pro M3", 1500.0, 12);
        manager.addItem(laptop);

        // 3. Tạo các người tham gia đấu giá (Dùng logic của Tùng & Phúc đã hợp nhất)
        Bidder nhat = new Bidder("U01", "Nhat_Leader", 3000.0);
        Bidder tung = new Bidder("U02", "Tung_Design", 2500.0);
        Bidder phuc = new Bidder("U03", "Phuc_Thread", 2000.0);

        // 4. Đăng ký nhận thông báo (Observer Pattern)
        // Khi có bất kỳ ai trả giá, cả 3 ông này đều nhận được tin nhắn
        manager.addObserver(nhat);
        manager.addObserver(tung);
        manager.addObserver(phuc);

        System.out.println("--- BẮT ĐẦU PHIÊN ĐẤU GIÁ ---");
        laptop.printInfo();

        // 5. Giả lập đấu giá đa luồng (Multi-threading)
        // Tạo các "luồng" đấu giá đồng thời để xem tính an toàn (Synchronized)
        Thread thread1 = new Thread(() -> {
            manager.placeBid("L01", "Tung_Design", 1600.0);
        });

        Thread thread2 = new Thread(() -> {
            manager.placeBid("L01", "Phuc_Thread", 1700.0);
        });

        Thread thread3 = new Thread(() -> {
            manager.placeBid("L01", "Nhat_Leader", 1800.0);
        });

        // Chạy các luồng cùng lúc
        thread1.start();
        thread2.start();
        thread3.start();

        // Đợi các luồng chạy xong để in kết quả cuối cùng
        try {
            thread1.join();
            thread2.join();
            thread3.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("--- KẾT THÚC PHIÊN ĐẤU GIÁ ---");
        System.out.println("Giá cuối cùng của " + laptop.getName() + " là: $" + laptop.getCurrentHighestBid());
        System.out.println("Người thắng cuộc: " + laptop.getHighestBidderId());
    }
}
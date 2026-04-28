package src;

public class Main {
    public static void main(String[] args) {
        AuctionManager manager = AuctionManager.getInstance();

        // Tạo sản phẩm
        Item laptop = new Electronics("L01", "MacBook Pro M3", 1500.0, 12);
        manager.addItem(laptop);

        // Tạo bidder
        Bidder nhat = new Bidder("U01", "Nhat_Leader", 3000.0);
        Bidder tung = new Bidder("U02", "Tung_Design", 2500.0);
        Bidder phuc = new Bidder("U03", "Phuc_Thread", 2000.0);

        // Đăng ký observer
        manager.addObserver(nhat);
        manager.addObserver(tung);
        manager.addObserver(phuc);

        // === THÊM VÀO TRƯỚC KHI ĐẤU GIÁ ===
        manager.startCountdown("L01", 60);  // Đếm ngược 60 giây
        manager.watchItem("L01", nhat);     // Đăng ký theo dõi realtime
        manager.watchItem("L01", tung);

        System.out.println("--- BẮT ĐẦU PHIÊN ĐẤU GIÁ ---");
        laptop.printInfo();

        // Đấu giá đa luồng
        Thread thread1 = new Thread(() -> manager.placeBid("L01", "Tung_Design", 1600.0));
        Thread thread2 = new Thread(() -> manager.placeBid("L01", "Phuc_Thread", 1700.0));
        Thread thread3 = new Thread(() -> manager.placeBid("L01", "Nhat_Leader", 1800.0));

        thread1.start();
        thread2.start();
        thread3.start();

        try {
            thread1.join();
            thread2.join();
            thread3.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("--- KẾT THÚC PHIÊN ĐẤU GIÁ ---");
        System.out.println("Giá cuối cùng: $" + laptop.getCurrentHighestBid());
        System.out.println("Người thắng: " + laptop.getHighestBidderId());

        // === SAU KHI ĐẤU GIÁ XONG, XEM THỐNG KÊ ===
        manager.showAnalytics("L01");
        System.out.println("Time remaining: " + manager.getRemainingTime("L01") + "s");
    }
}

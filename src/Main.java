public class Main {
    public static void main(String[] args) {
        // 1. Lấy AuctionManager (Singleton)
        AuctionManager manager = AuctionManager.getInstance();

        // 2. Tạo thử một món đồ điện tử (Dùng Constructor của Tùng)
        // ID: "E01", Tên: "iPhone 15", Giá gốc: 1000.0, Bảo hành: 12 tháng
        Electronics iphone = new Electronics("E01", "iPhone 15", 1000.0, 12);

        // 3. Đưa vào hệ thống quản lý
        manager.addItem(iphone);
        System.out.println("--- Đã thêm món hàng: " + iphone.getName() + " ---");
        System.out.println("Giá hiện tại: " + iphone.getCurrentHighestBid());

        // 4. Thử đặt giá (Test logic của Tùng)
        System.out.println("\n--- Thử đặt giá 900 (Thấp hơn giá gốc) ---");
        manager.placeBid("E01", "Nhat_User", 900.0);

        System.out.println("\n--- Thử đặt giá 1200 (Hợp lệ) ---");
        manager.placeBid("E01", "Tung_User", 1200.0);

        // 5. Kiểm tra lại giá cuối cùng
        System.out.println("\nGiá sau khi đấu giá: " + iphone.getCurrentHighestBid());
    }
}
package src; // Sửa lại tên package cho đúng

public abstract class Item extends Entity {
    private String name;           // Chuyển sang private
    private String description;
    private double startingPrice;
    private double currentHighestBid;

    public Item(String id, String name, double startingPrice) {
        super(id);
        this.name = name;
        this.startingPrice = startingPrice;
        this.currentHighestBid = startingPrice;
    }

    // Getter cho name (Alt+Insert)
    public String getName() {
        return name;
    }

    // Getter cho giá hiện tại - CỰC KỲ QUAN TRỌNG để AuctionManager dùng
    public double getCurrentHighestBid() {
        return currentHighestBid;
    }
    // Getter cho startingPrice
    public double getStartingPrice() {
        return startingPrice;

    }

    public abstract void printInfo();

    // Tối ưu hàm cập nhật giá
    public synchronized void updateHighestBid(double newBid) {
        if (newBid > currentHighestBid) {
            this.currentHighestBid = newBid;
        } else {
            // Có thể thêm thông báo lỗi ở đây nếu cần
        }
    }
}
public abstract class Entity {
    protected String id;

    public Entity(String id) {
        this.id = id;
    }

    public String getId() { return id; }
}

public abstract class Item extends Entity {
    protected String name;
    protected String description;
    protected double startingPrice;
    protected double currentHighestBid;

    public Item(String id, String name, double startingPrice) {
        super(id);
        this.name = name;
        this.startingPrice = startingPrice;
        this.currentHighestBid = startingPrice; // Ban đầu giá cao nhất là giá khởi điểm
    }

    // Tính đa hình (Polymorphism): Các lớp con sẽ ghi đè phương thức này
    public abstract void printInfo();

    // Encapsulation: Getter/Setter
    public synchronized void updateHighestBid(double newBid) {
        if (newBid > currentHighestBid) {
            this.currentHighestBid = newBid;
        }
    }
}
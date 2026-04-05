import java.util.HashMap;
import java.util.Map;

public class AuctionManager {
    private static AuctionManager instance;
    private Map<String, Item> activeAuctions;

    private AuctionManager() {
        activeAuctions = new HashMap<>();
    }

    public static synchronized AuctionManager getInstance() {
        if (instance == null) {
            instance = new AuctionManager();
        }
        return instance;
    }

    public void addItem(Item item) {
        activeAuctions.put(item.getId(), item);
    }

    // Xử lý đồng thời (Concurrency) để đảm bảo an toàn luồng khi nhiều người bid cùng lúc
    public synchronized boolean placeBid(String itemId, String bidderId, double bidAmount) {
        Item item = activeAuctions.get(itemId);
        if (item != null && bidAmount > item.currentHighestBid) {
            item.updateHighestBid(bidAmount);
            System.out.println("Bid successful for item " + itemId + " by " + bidderId);
            // TODO: Gọi Observer Pattern ở đây để thông báo realtime cho các Client khác
            return true;
        }
        System.out.println("Bid failed! Amount must be higher than current highest bid.");
        return false;
    }
}
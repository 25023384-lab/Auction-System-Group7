<<<<<<< HEAD
=======
package src;

import java.util.concurrent.*;

>>>>>>> 2bf0db4 (Tuần 7: Update về xử lý luồng)
public class Main {
    public static void main(String[] args) throws InterruptedException {

        AuctionManager manager = AuctionManager.getInstance();

        // Tạo item
        Item item = ItemFactory.createItem("electronics", "E01", "Laptop", 1000, 12);
        manager.addItem(item);

        // Observer
        manager.addObserver(new Bidder("User A"));
        manager.addObserver(new Bidder("User B"));

        // Thread Pool
        ExecutorService executor = Executors.newFixedThreadPool(3);

        executor.submit(new BidTask("E01", "User1", 1100));
        executor.submit(new BidTask("E01", "User2", 1200));
        executor.submit(new BidTask("E01", "User3", 1150));

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        System.out.println("Final price: " + item.getCurrentHighestBid());
    }
}
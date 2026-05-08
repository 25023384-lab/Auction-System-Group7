package src.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import src.Art;
import src.AuctionManager;
import src.Item;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuctionManagerConcurrencyTest {

    private AuctionManager manager;

    @BeforeEach
    public void setUp() {
        // Lấy instance của Singleton
        manager = AuctionManager.getInstance();
    }

    @Test
    public void testConcurrentBidding_NoLostUpdate() throws InterruptedException {
        // 1. Chuẩn bị dữ liệu
        String itemId = "ITEM_TEST_" + System.currentTimeMillis(); // ID duy nhất để không đụng data cũ
        Art testArt = new Art(itemId, "Bức tranh thử nghiệm", 100.0, "Picasso");
        manager.addItem(testArt);

        // Mở phiên đấu giá trong 60 giây để chắc chắn không bị hết hạn khi đang test
        manager.startCountdown(itemId, 60);

        // 2. Thiết lập môi trường đa luồng
        int numberOfBidders = 100;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfBidders);
        // Dùng CountDownLatch để chặn các luồng lại, bắt chúng phải đợi nhau
        CountDownLatch latch = new CountDownLatch(1);
        // Dùng để đợi tất cả các luồng chạy xong mới kiểm tra kết quả
        CountDownLatch doneLatch = new CountDownLatch(numberOfBidders);

        // 3. Tạo 100 luồng, mỗi luồng đại diện cho 1 người đặt giá
        for (int i = 0; i < numberOfBidders; i++) {
            final double bidAmount = 101.0 + i; // Giá tăng dần: 101, 102, ... 200
            final String bidderId = "Bidder_" + i;

            executor.submit(() -> {
                try {
                    latch.await(); // Tất cả các luồng sẽ dừng ở đây, chờ lệnh "Go!"
                    manager.placeBid(itemId, bidderId, bidAmount);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown(); // Báo hiệu luồng này đã chạy xong
                }
            });
        }

        // 4. Kích hoạt cho 100 luồng chạy CÙNG MỘT LÚC (Mô phỏng concurrent bidding)
        latch.countDown();

        // Chờ tất cả 100 luồng thực thi xong
        doneLatch.await();
        executor.shutdown();

        // 5. Kiểm tra kết quả
        Item itemInManager = manager.getItem(itemId);

        // Vì khối lượng đặt giá cao nhất là 100 + 100 = 200,
        // nếu hệ thống xử lý đồng bộ tốt (nhờ synchronized), giá cuối cùng CHẮC CHẮN phải là 200.0
        assertEquals(200.0, itemInManager.getCurrentHighestBid(),
                "Lỗi Lost Update: Giá cao nhất không chính xác do xung đột luồng!");
    }
}

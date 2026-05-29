package com.auction.event;

import com.auction.entity.user.Bidder;

/**
 * Lắng nghe sự kiện bid mới và thông báo tới Bidder tương ứng.
 * Tách biệt logic thông báo khỏi entity Bidder — tuân thủ Single Responsibility Principle.
 */
public class BidNotificationListener implements BidObserver {
    private final Bidder bidder;

    public BidNotificationListener(Bidder bidder) {
        this.bidder = bidder;
    }

    @Override
    public void update(String itemId, double newBid, String bidderId) {
        System.out.println("🔔 [Thông báo tới " + bidder.getUsername() + "]: Vật phẩm "
                + itemId + " vừa được trả giá mới: $" + newBid + " bởi " + bidderId);

        if (newBid > bidder.getBalance()) {
            System.out.println("⚠️ Cảnh báo: Số dư của bạn ($"
                    + bidder.getBalance() + ") không còn đủ để theo thầu!");
        }
    }
}

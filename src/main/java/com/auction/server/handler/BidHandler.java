package com.auction.server.handler;

import com.auction.dao.BidTransactionDAO;
import com.auction.dao.UserDAO;
import com.auction.dto.bid.AutoBidRequest;
import com.auction.dto.bid.BidRequest;
import com.auction.entity.bid.BidTransaction;
import com.auction.entity.message.Message;
import com.auction.exception.AuctionClosedException;
import com.auction.exception.InvalidBidException;
import com.auction.service.auction.AuctionManager;
import com.auction.service.bidding.AutoBidder;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Xử lý các yêu cầu liên quan đến Bid: Đặt giá, Auto-Bid, Lịch sử, Analytics.
 */
public class BidHandler {
    private final AuctionManager auctionManager;
    private final AutoBidder autoBidder;
    private final UserDAO userDAO;
    private final BidTransactionDAO bidDAO;
    private final ObjectMapper objectMapper;

    public BidHandler(AuctionManager auctionManager, AutoBidder autoBidder,
                      UserDAO userDAO, BidTransactionDAO bidDAO, ObjectMapper objectMapper) {
        this.auctionManager = auctionManager;
        this.autoBidder = autoBidder;
        this.userDAO = userDAO;
        this.bidDAO = bidDAO;
        this.objectMapper = objectMapper;
    }

    public void handleBid(Message msg, PrintWriter out, Consumer<Message> broadcast) {
        try {
            BidRequest req = objectMapper.readValue(msg.getData(), BidRequest.class);
            boolean success = auctionManager.placeBid(req.getItemId(), req.getBidderId(), req.getAmount());
            out.println(objectMapper.writeValueAsString(
                    new Message("BID_RESULT", String.valueOf(success))));
        } catch (InvalidBidException | AuctionClosedException e) {
            try {
                out.println(objectMapper.writeValueAsString(
                        new Message("BID_RESULT", e.getMessage())));
            } catch (Exception ignored) {}
        } catch (Exception e) {
            try {
                out.println(objectMapper.writeValueAsString(new Message("ERROR", e.getMessage())));
            } catch (Exception ignored) {}
        }
    }

    public void handleRegisterAutoBid(Message msg, PrintWriter out) {
        try {
            AutoBidRequest req = objectMapper.readValue(msg.getData(), AutoBidRequest.class);
            autoBidder.register(req.getBidderId(), req.getItemId(), req.getMaxBid(), req.getIncrement());

            Map<String, Object> resp = new HashMap<>();
            resp.put("itemId", req.getItemId());
            resp.put("maxBid", req.getMaxBid());
            resp.put("increment", req.getIncrement());
            out.println(objectMapper.writeValueAsString(
                    new Message("AUTO_BID_REGISTERED", objectMapper.writeValueAsString(resp))));
            System.out.println("AutoBid registered: bidder=" + req.getBidderId()
                    + " item=" + req.getItemId()
                    + " max=$" + req.getMaxBid() + " step=$" + req.getIncrement());
        } catch (Exception e) {
            try { out.println(objectMapper.writeValueAsString(new Message("ERROR", e.getMessage()))); }
            catch (Exception ignored) {}
        }
    }

    public void handleGetBidHistory(Message msg, PrintWriter out) {
        try {
            String itemId = msg.getData();
            List<BidTransaction> bids = bidDAO.getBidsByItem(itemId);

            // Tạo list có kèm username
            List<Map<String, Object>> richBids = new java.util.ArrayList<>();
            for (BidTransaction bid : bids) {
                Map<String, Object> entry = new HashMap<>();
                entry.put("bidderId", bid.getBidderId());
                entry.put("amount", bid.getBidAmount());
                entry.put("status", bid.getStatus());
                entry.put("timestamp", bid.getTimestamp() != null
                        ? bid.getTimestamp().toString() : "");
                // Resolve username
                String username = bid.getBidderId();
                try {
                    UserDAO.UserRecord rec = userDAO.findById(bid.getBidderId());
                    if (rec != null) username = rec.username;
                } catch (Exception ignored) {}
                entry.put("bidderName", username);
                richBids.add(entry);
            }
            out.println(objectMapper.writeValueAsString(
                    new Message("BID_HISTORY", objectMapper.writeValueAsString(richBids))));
        } catch (Exception e) {
            try { out.println(objectMapper.writeValueAsString(new Message("ERROR", e.getMessage()))); }
            catch (Exception ignored) {}
        }
    }

    public void handleGetAnalytics(Message msg, PrintWriter out) {
        try {
            String itemId = msg.getData();
            int count = bidDAO.countBids(itemId);
            BidTransaction highest = bidDAO.getHighestBid(itemId);
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalBids", count);
            stats.put("highestBid", highest != null ? highest.getBidAmount() : 0.0);
            out.println(objectMapper.writeValueAsString(
                    new Message("ANALYTICS", objectMapper.writeValueAsString(stats))));
        } catch (Exception e) {
            try { out.println(objectMapper.writeValueAsString(new Message("ERROR", e.getMessage()))); }
            catch (Exception ignored) {}
        }
    }
}

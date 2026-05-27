package com.auction.app;

import com.auction.server.AuctionServer;
import com.auction.client.AuctionClient;

public class Main {

    public static void main(String[] args) {
        if (args.length > 0 && "server".equals(args[0])) {
            AuctionServer.main(args);
        } else {
            AuctionClient.main(args);
        }
    }
}
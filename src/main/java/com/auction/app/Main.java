package com.auction.app;

import com.auction.client.AuctionClient;
import com.auction.server.AuctionServer;

public class Main {
    public static class Launcher {
        public static void main(String[] args) {
            Main.main(args);
        }
    }

    public static void main(String[] args) {
        if (args.length > 0 && "server".equalsIgnoreCase(args[0])) {

            AuctionServer.main(args);
        } else {

            AuctionClient.main(args);
        }
    }
}
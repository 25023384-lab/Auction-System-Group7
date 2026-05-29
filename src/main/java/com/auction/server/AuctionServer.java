package com.auction.server;

import com.auction.entity.items.Item;
import com.auction.entity.message.Message;
import com.auction.service.scheduler.AuctionScheduler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.auction.service.auction.AuctionManager;
import com.auction.service.bidding.AutoBidder;
import com.auction.util.DBHelper;
import com.auction.dao.BidTransactionDAO;
import com.auction.dao.ItemDAO;
import com.auction.dao.UserDAO;
import com.auction.service.auth.AuthService;
import com.auction.server.handler.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuctionServer {
    private static final int PORT = 12345;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private static final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    
    private final AuctionManager auctionManager = new AuctionManager();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AuthService authService = new AuthService();
    private final ItemDAO itemDAO = new ItemDAO();
    private final UserDAO userDAO = new UserDAO();
    private final BidTransactionDAO bidDAO = new BidTransactionDAO();
    private final AutoBidder autoBidder;
    private final AuctionScheduler auctionScheduler;

    // Handlers
    private final AuthHandler authHandler;
    private final ItemHandler itemHandler;
    private final BidHandler bidHandler;
    private final AdminHandler adminHandler;
    private final PaymentHandler paymentHandler;

    public AuctionServer() {
        objectMapper.registerModule(new JavaTimeModule());
        // Khởi tạo AutoBidder và gán vào AuctionManager
        autoBidder = new AutoBidder(auctionManager);
        auctionManager.setAutoBidder(autoBidder);
        auctionScheduler = new AuctionScheduler(auctionManager);

        // Khởi tạo các handlers
        authHandler = new AuthHandler(authService, auctionManager, objectMapper);
        
        com.auction.service.item.ItemService itemService = new com.auction.service.item.ItemService(itemDAO, auctionManager);
        itemHandler = new ItemHandler(itemService, objectMapper);
        
        bidHandler = new BidHandler(auctionManager, autoBidder, userDAO, bidDAO, objectMapper);
        adminHandler = new AdminHandler(userDAO, objectMapper);
        paymentHandler = new PaymentHandler(userDAO, itemDAO, auctionManager, objectMapper);
    }

    public void start() {
        DBHelper.initializeDatabase();
        loadItemsIntoManager();
        auctionScheduler.start();
        System.out.println("Server started on port " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket);
                clients.add(handler);
                executor.submit(handler);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadItemsIntoManager() {
        try {
            List<Item> items = itemDAO.findAll();
            for (Item item : items) {
                auctionManager.addItem(item);
            }
            System.out.println("Loaded " + items.size() + " items into AuctionManager.");
        } catch (Exception e) {
            System.err.println("Failed to load items: " + e.getMessage());
        }
    }

    public static void broadcast(Message msg) {
        for (ClientHandler client : clients) {
            client.sendMessage(msg);
        }
    }

    private class ClientHandler implements Runnable {
        private final Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private String currentUserId; // track user for cleanup

        public ClientHandler(Socket socket) {
            this.socket = socket;
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void sendMessage(Message msg) {
            try {
                out.println(objectMapper.writeValueAsString(msg));
            } catch (Exception e) {
                System.err.println("Error sending message to client: " + e.getMessage());
            }
        }

        @Override
        public void run() {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    Message msg = objectMapper.readValue(line, Message.class);
                    handleMessage(msg);
                }
            } catch (Exception e) {
                System.out.println("Client disconnected: " + socket.getRemoteSocketAddress());
            } finally {
                clients.remove(this);
                try { socket.close(); } catch (IOException e) { e.printStackTrace(); }
            }
        }

        private void handleMessage(Message msg) throws Exception {
            switch (msg.getType()) {
                case "LOGIN":
                    String userId = authHandler.handleLogin(msg, out);
                    if (userId != null) {
                        currentUserId = userId;
                    }
                    break;
                case "REGISTER":           authHandler.handleRegister(msg, out); break;
                case "GET_ITEMS":          itemHandler.handleListItems(out); break;
                case "CREATE_ITEM":        itemHandler.handleCreateItem(msg, out, AuctionServer::broadcast); break;
                case "UPDATE_ITEM":        itemHandler.handleUpdateItem(msg, out, AuctionServer::broadcast); break;
                case "BID":                bidHandler.handleBid(msg, out, AuctionServer::broadcast); break;
                case "DELETE_ITEM":        itemHandler.handleDeleteItem(msg, out, AuctionServer::broadcast); break;
                case "REGISTER_AUTO_BID":  bidHandler.handleRegisterAutoBid(msg, out); break;
                case "GET_BID_HISTORY":    bidHandler.handleGetBidHistory(msg, out); break;
                case "GET_ANALYTICS":      bidHandler.handleGetAnalytics(msg, out); break;
                case "GET_SELLER_ITEMS":   itemHandler.handleGetSellerItems(msg, out); break;
                case "GET_ITEM_DETAILS":   itemHandler.handleGetItemDetails(msg, out, userDAO, bidDAO); break;
                case "TOP_UP":             paymentHandler.handleTopUp(msg, out); break;
                case "PAY_ITEM":           paymentHandler.handlePayItem(msg, out, currentUserId, AuctionServer::broadcast); break;
                case "CANCEL_ORDER":       paymentHandler.handleCancelOrder(msg, out, currentUserId, AuctionServer::broadcast); break;
                case "GET_ALL_USERS":      adminHandler.handleGetAllUsers(out); break;
                case "DELETE_USER":        adminHandler.handleDeleteUser(msg, out); break;
                case "LOGOUT":             handleLogout(); break;
                default:
                    System.err.println("Unknown message type: " + msg.getType());
            }
        }

        private void handleLogout() {
            clients.remove(this);
            System.out.println("Client logged out: " + currentUserId);
        }
    }

    public static void main(String[] args) {
        new AuctionServer().start();
    }
}
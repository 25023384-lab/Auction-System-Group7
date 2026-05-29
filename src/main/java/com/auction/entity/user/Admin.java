package com.auction.entity.user;

import com.auction.dao.ItemDAO;
import com.auction.dao.UserDAO;
import com.auction.entity.items.Item;

import java.sql.SQLException;
import java.util.List;

public class Admin extends User {

    private final UserDAO userDAO = new UserDAO();
    private final ItemDAO itemDAO = new ItemDAO();

    // Constructor không tham số cho Jackson
    public Admin() {
        super();
    }

    public Admin(String id, String username) {
        super(id, username, "ADMIN");
    }

    @Override
    public void displayProfile() {
        System.out.println("--- Admin Dashboard ---");
        System.out.println("Username : " + username);
        System.out.println("Quyền hạn: Toàn quyền hệ thống (CRUD User & Item).");
    }

    /**
     * Xóa vĩnh viễn người dùng khỏi DB.
     * Dùng khi tài khoản vi phạm quy định.
     */
    public void banUser(String userId) {
        try {
            UserDAO.UserRecord rec = userDAO.findById(userId);
            if (rec == null) {
                System.out.println("⚠️  Không tìm thấy người dùng: " + userId);
                return;
            }
            userDAO.delete(userId);
            System.out.println("🚫 [Admin] Đã xóa tài khoản: " + rec.username + " (" + rec.role + ")");
        } catch (SQLException e) {
            System.out.println("❌ Lỗi khi xóa người dùng: " + e.getMessage());
        }
    }

    /**
     * Xóa cưỡng bức một phiên đấu giá (item) khỏi DB.
     * Dùng khi item vi phạm chính sách hoặc dữ liệu sai.
     */
    public void removeItem(String itemId) {
        try {
            Item item = itemDAO.findById(itemId);
            if (item == null) {
                System.out.println("⚠️  Không tìm thấy item: " + itemId);
                return;
            }
            itemDAO.delete(itemId);
            System.out.println("🗑️  [Admin] Đã xóa item: " + item.getName() + " (trạng thái: " + item.getStatus() + ")");
        } catch (SQLException e) {
            System.out.println("❌ Lỗi khi xóa item: " + e.getMessage());
        }
    }

    /**
     * In danh sách tất cả người dùng ra console.
     */
    public void listAllUsers() {
        try {
            List<UserDAO.UserRecord> users = userDAO.findAll();
            System.out.println("👥 [Admin] Danh sách người dùng (" + users.size() + "):");
            System.out.printf("  %-36s %-20s %-10s %s%n", "ID", "Username", "Role", "Balance");
            System.out.println("  " + "-".repeat(80));
            for (UserDAO.UserRecord u : users) {
                System.out.printf("  %-36s %-20s %-10s $%.2f%n",
                        u.id, u.username, u.role, u.balance);
            }
        } catch (SQLException e) {
            System.out.println("❌ Lỗi khi lấy danh sách người dùng: " + e.getMessage());
        }
    }

    /**
     * In danh sách tất cả phiên đấu giá (item) ra console.
     */
    public void listAllItems() {
        try {
            List<Item> items = itemDAO.findAll();
            System.out.println("📦 [Admin] Danh sách phiên đấu giá (" + items.size() + "):");
            System.out.printf("  %-36s %-25s %-12s %s%n", "ID", "Tên", "Trạng thái", "Giá cao nhất");
            System.out.println("  " + "-".repeat(90));
            for (Item item : items) {
                System.out.printf("  %-36s %-25s %-12s $%.2f%n",
                        item.getId(), item.getName(), item.getStatus(), item.getCurrentHighestBid());
            }
        } catch (SQLException e) {
            System.out.println("❌ Lỗi khi lấy danh sách item: " + e.getMessage());
        }
    }

    /**
     * In thống kê tổng quan hệ thống: số lượng user theo role và tổng số phiên.
     */
    public void printSystemStats() {
        try {
            List<UserDAO.UserRecord> allUsers = userDAO.findAll();
            List<Item> allItems = itemDAO.findAll();

            long bidders  = allUsers.stream().filter(u -> "BIDDER".equals(u.role)).count();
            long sellers  = allUsers.stream().filter(u -> "SELLER".equals(u.role)).count();
            long admins   = allUsers.stream().filter(u -> "ADMIN".equals(u.role)).count();
            long running  = allItems.stream().filter(i -> i.getStatus() == Item.Status.RUNNING).count();
            long finished = allItems.stream().filter(i -> i.getStatus() == Item.Status.FINISHED).count();
            long pending  = allItems.stream().filter(i -> i.getStatus() == Item.Status.OPEN).count();

            System.out.println("📊 [Admin] Thống kê hệ thống:");
            System.out.println("  Người dùng  → Bidders: " + bidders
                    + " | Sellers: " + sellers + " | Admins: " + admins);
            System.out.println("  Phiên đấu   → Đang chạy: " + running
                    + " | Kết thúc: " + finished + " | Chờ: " + pending);
            System.out.println("  Tổng cộng   → " + allUsers.size() + " user | " + allItems.size() + " item");
        } catch (SQLException e) {
            System.out.println("❌ Lỗi khi lấy thống kê: " + e.getMessage());
        }
    }
}

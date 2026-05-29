package com.auction.server.handler;

import com.auction.dao.UserDAO;
import com.auction.entity.message.Message;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.PrintWriter;
import java.util.List;

/**
 * Xử lý các yêu cầu của Admin: Lấy danh sách người dùng, xóa người dùng.
 */
public class AdminHandler {
    private final UserDAO userDAO;
    private final ObjectMapper objectMapper;

    public AdminHandler(UserDAO userDAO, ObjectMapper objectMapper) {
        this.userDAO = userDAO;
        this.objectMapper = objectMapper;
    }

    public void handleGetAllUsers(PrintWriter out) {
        try {
            List<UserDAO.UserRecord> users = userDAO.findAll();
            out.println(objectMapper.writeValueAsString(
                    new Message("ALL_USERS", objectMapper.writeValueAsString(users))));
        } catch (Exception e) {
            try { out.println(objectMapper.writeValueAsString(new Message("ERROR", e.getMessage()))); }
            catch (Exception ignored) {}
        }
    }

    public void handleDeleteUser(Message msg, PrintWriter out) {
        try {
            String userId = msg.getData();
            userDAO.delete(userId);
            out.println(objectMapper.writeValueAsString(
                    new Message("DELETE_USER_SUCCESS", userId)));
        } catch (Exception e) {
            try { out.println(objectMapper.writeValueAsString(new Message("ERROR", e.getMessage()))); }
            catch (Exception ignored) {}
        }
    }
}

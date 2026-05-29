package com.auction.client.api;

import com.auction.client.ClientConnection;
import com.auction.dto.auth.LoginRequest;
import com.auction.dto.auth.RegisterRequest;
import com.auction.entity.message.Message;
import com.auction.entity.user.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Đóng gói logic giao tiếp mạng cho chức năng Xác thực (Đăng nhập, Đăng ký).
 */
public class AuthApiClient {
    private final ClientConnection connection;
    private final ObjectMapper objectMapper;

    public AuthApiClient(ClientConnection connection) {
        this.connection = connection;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public User login(String username, String password) throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername(username);
        req.setPassword(password);

        Message msg = new Message("LOGIN", objectMapper.writeValueAsString(req));
        connection.sendMessage(msg);

        Message response = connection.receiveMessage();
        if ("LOGIN_SUCCESS".equals(response.getType())) {
            return objectMapper.readValue(response.getData(), User.class);
        } else {
            throw new Exception(response.getData());
        }
    }

    public void register(String username, String email, String password, String role) throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUsername(username);
        req.setEmail(email);
        req.setPassword(password);
        req.setRole(role);

        Message msg = new Message("REGISTER", objectMapper.writeValueAsString(req));
        connection.sendMessage(msg);

        Message response = connection.receiveMessage();
        if (!"REGISTER_SUCCESS".equals(response.getType())) {
            throw new Exception(response.getData());
        }
    }
}

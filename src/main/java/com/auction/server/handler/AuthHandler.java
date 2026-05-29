package com.auction.server.handler;

import com.auction.dto.auth.LoginRequest;
import com.auction.dto.auth.RegisterRequest;
import com.auction.entity.message.Message;
import com.auction.entity.user.Bidder;
import com.auction.entity.user.User;
import com.auction.exception.AuthenticationException;
import com.auction.service.auth.AuthService;
import com.auction.service.auction.AuctionManager;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.PrintWriter;

/**
 * Xử lý các yêu cầu xác thực: Đăng nhập (LOGIN) và Đăng ký (REGISTER).
 */
public class AuthHandler {
    private final AuthService authService;
    private final AuctionManager auctionManager;
    private final ObjectMapper objectMapper;

    public AuthHandler(AuthService authService, AuctionManager auctionManager, ObjectMapper objectMapper) {
        this.authService = authService;
        this.auctionManager = auctionManager;
        this.objectMapper = objectMapper;
    }

    /**
     * Xử lý yêu cầu đăng nhập.
     * @return userId nếu đăng nhập thành công, null nếu thất bại.
     */
    public String handleLogin(Message msg, PrintWriter out) {
        try {
            LoginRequest req = objectMapper.readValue(msg.getData(), LoginRequest.class);
            User user = authService.login(req.getUsername(), req.getPassword());
            if (user != null) {
                if (user instanceof Bidder) {
                    auctionManager.addBidder((Bidder) user);
                }
                out.println(objectMapper.writeValueAsString(
                        new Message("LOGIN_SUCCESS", objectMapper.writeValueAsString(user))));
                return user.getId();
            } else {
                out.println(objectMapper.writeValueAsString(
                        new Message("LOGIN_FAILED", "Invalid credentials")));
            }
        } catch (AuthenticationException e) {
            try {
                out.println(objectMapper.writeValueAsString(
                        new Message("LOGIN_FAILED", e.getMessage())));
            } catch (Exception ignored) {}
        } catch (Exception e) {
            try {
                out.println(objectMapper.writeValueAsString(new Message("ERROR", e.getMessage())));
            } catch (Exception ignored) {}
        }
        return null;
    }

    /**
     * Xử lý yêu cầu đăng ký tài khoản mới.
     */
    public void handleRegister(Message msg, PrintWriter out) {
        try {
            RegisterRequest req = objectMapper.readValue(msg.getData(), RegisterRequest.class);
            boolean success = authService.register(req.getUsername(), req.getEmail(),
                    req.getPassword(), req.getRole());
            if (success) {
                out.println(objectMapper.writeValueAsString(
                        new Message("REGISTER_SUCCESS", "Account created successfully.")));
            } else {
                out.println(objectMapper.writeValueAsString(
                        new Message("REGISTER_FAILED", "Username already exists.")));
            }
        } catch (Exception e) {
            try {
                out.println(objectMapper.writeValueAsString(new Message("ERROR", e.getMessage())));
            } catch (Exception ignored) {}
        }
    }
}

package com.auction.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class UserDAOTest {
    private UserDAO userDAO;

    @BeforeEach
    void setUp() {
        userDAO = new UserDAO();
    }

    @Test
    void testSaveAndFindById() throws Exception {
        String uniqueId = "testDAO_" + System.currentTimeMillis();

        // Save
        userDAO.insert(uniqueId, "daoUser", "dao@email.com", "hashPass", "BIDDER", 0.0);

        // Find
        UserDAO.UserRecord record = userDAO.findById(uniqueId);
        assertNotNull(record);
        assertEquals("daoUser", record.username);
        assertEquals("BIDDER", record.role);
    }

    @Test
    void testUpdateBalance() throws Exception {
        String uniqueId = "testDAO_bal_" + System.currentTimeMillis();
        userDAO.insert(uniqueId, "daoUserBal", "bal@email.com", "hashPass", "BIDDER", 0.0);

        userDAO.updateBalance(uniqueId, 1500.50);

        UserDAO.UserRecord record = userDAO.findById(uniqueId);
        assertNotNull(record);
        assertEquals(1500.50, record.balance, 0.01);
    }

    @Test
    void testFindByRole() throws Exception {
        String uniqueId = "testDAO_role_" + System.currentTimeMillis();
        userDAO.insert(uniqueId, "daoUserRole", "role@email.com", "hashPass", "ADMIN", 0.0);

        List<UserDAO.UserRecord> list = userDAO.findByRole("ADMIN");
        assertNotNull(list);
        assertTrue(list.size() > 0);
    }
}

package com.firma.dao;

import com.firma.db.ConnectionManager;
import com.firma.model.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomerDao {

    private static final String BASE_SELECT =
            "SELECT customer_id, organization_name, representative, contact_info FROM customers";

    public List<Customer> findAll() throws SQLException {
        List<Customer> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(BASE_SELECT + " ORDER BY customer_id")) {
            while (rs.next()) result.add(mapRow(rs));
        }
        return result;
    }

    public Optional<Customer> findById(int id) throws SQLException {
        String sql = BASE_SELECT + " WHERE customer_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    public int insert(Customer c) throws SQLException {
        String sql = "INSERT INTO customers (organization_name, representative, contact_info) VALUES (?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getOrganizationName());
            ps.setString(2, c.getRepresentative());
            ps.setString(3, c.getContactInfo());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    c.setId(id);
                    return id;
                }
            }
            throw new SQLException("Не удалось получить ключ");
        }
    }

    public boolean update(Customer c) throws SQLException {
        String sql = "UPDATE customers SET organization_name = ?, representative = ?, contact_info = ? WHERE customer_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getOrganizationName());
            ps.setString(2, c.getRepresentative());
            ps.setString(3, c.getContactInfo());
            ps.setInt(4, c.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM customers WHERE customer_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Customer mapRow(ResultSet rs) throws SQLException {
        return new Customer(
                rs.getInt("customer_id"),
                rs.getString("organization_name"),
                rs.getString("representative"),
                rs.getString("contact_info")
        );
    }
}

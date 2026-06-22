package com.firma.dao;

import com.firma.db.ConnectionManager;
import com.firma.model.SalesAgent;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SalesAgentDao {

    private static final String BASE_SELECT =
            "SELECT agent_id, full_name, email, pager_number, phone_number, pbx_code FROM sales_agents";

    public List<SalesAgent> findAll() throws SQLException {
        List<SalesAgent> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(BASE_SELECT + " ORDER BY agent_id")) {
            while (rs.next()) result.add(mapRow(rs));
        }
        return result;
    }

    public Optional<SalesAgent> findById(int id) throws SQLException {
        String sql = BASE_SELECT + " WHERE agent_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    // Поиск агентов по коду АТС для 3 запроса
    public List<SalesAgent> findByPbxCode(String pbxCode) throws SQLException {
        String sql = BASE_SELECT + " WHERE pbx_code = ? ORDER BY full_name";
        List<SalesAgent> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pbxCode);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapRow(rs));
            }
        }
        return result;
    }

    public int insert(SalesAgent a) throws SQLException {
        String sql = "INSERT INTO sales_agents (full_name, email, pager_number, phone_number, pbx_code) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, a.getFullName());
            ps.setString(2, a.getEmail());
            ps.setString(3, a.getPagerNumber());
            ps.setString(4, a.getPhoneNumber());
            ps.setString(5, a.getPbxCode());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    a.setId(id);
                    return id;
                }
            }
            throw new SQLException("Не удалось получить ключ");
        }
    }

    public boolean update(SalesAgent a) throws SQLException {
        String sql = "UPDATE sales_agents SET full_name = ?, email = ?, pager_number = ?, phone_number = ?, pbx_code = ? WHERE agent_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, a.getFullName());
            ps.setString(2, a.getEmail());
            ps.setString(3, a.getPagerNumber());
            ps.setString(4, a.getPhoneNumber());
            ps.setString(5, a.getPbxCode());
            ps.setInt(6, a.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM sales_agents WHERE agent_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private SalesAgent mapRow(ResultSet rs) throws SQLException {
        return new SalesAgent(
                rs.getInt("agent_id"),
                rs.getString("full_name"),
                rs.getString("email"),
                rs.getString("pager_number"),
                rs.getString("phone_number"),
                rs.getString("pbx_code")
        );
    }
}

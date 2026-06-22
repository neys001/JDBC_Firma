package com.firma.dao;

import com.firma.db.ConnectionManager;
import com.firma.model.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO для таблицы components.
 * Демонстрирует CRUD + batchInsert (по образцу SeatDao).
 */
public class ComponentDao {

    private static final String BASE_SELECT =
            "SELECT component_id, name, manufacturer, current_quantity, min_stock FROM components";

    public List<Component> findAll() throws SQLException {
        List<Component> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(BASE_SELECT + " ORDER BY component_id")) {
            while (rs.next()) result.add(mapRow(rs));
        }
        return result;
    }

    public Optional<Component> findById(int id) throws SQLException {
        String sql = BASE_SELECT + " WHERE component_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    //Поиск компонентов фирмы-изготовителя для 2 запроса
    public List<Component> findByManufacturer(String manufacturer) throws SQLException {
        String sql = BASE_SELECT + " WHERE manufacturer = ? ORDER BY name";
        List<Component> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, manufacturer);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapRow(rs));
            }
        }
        return result;
    }

    public int insert(Component c) throws SQLException {
        String sql = "INSERT INTO components (name, manufacturer, current_quantity, min_stock) VALUES (?, ?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getName());
            ps.setString(2, c.getManufacturer());
            ps.setInt(3, c.getCurrentQuantity());
            ps.setInt(4, c.getMinStock());
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

    //Пакетная вставка - демонстрация batch-операций.
    public int batchInsert(List<Component> components) throws SQLException {
        String sql = "INSERT INTO components (name, manufacturer, current_quantity, min_stock) VALUES (?, ?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Component c : components) {
                ps.setString(1, c.getName());
                ps.setString(2, c.getManufacturer());
                ps.setInt(3, c.getCurrentQuantity());
                ps.setInt(4, c.getMinStock());
                ps.addBatch();
            }
            int[] counts = ps.executeBatch();
            int total = 0;
            for (int n : counts) total += n;
            return total;
        }
    }

    public boolean update(Component c) throws SQLException {
        String sql = "UPDATE components SET name = ?, manufacturer = ?, current_quantity = ?, min_stock = ? WHERE component_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getName());
            ps.setString(2, c.getManufacturer());
            ps.setInt(3, c.getCurrentQuantity());
            ps.setInt(4, c.getMinStock());
            ps.setInt(5, c.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM components WHERE component_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Component mapRow(ResultSet rs) throws SQLException {
        return new Component(
                rs.getInt("component_id"),
                rs.getString("name"),
                rs.getString("manufacturer"),
                rs.getInt("current_quantity"),
                rs.getInt("min_stock")
        );
    }
}

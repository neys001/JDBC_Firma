package com.firma.dao;

import com.firma.db.ConnectionManager;
import com.firma.model.Component;
import com.firma.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


//DAO для таблицы products + связь product_components (состав изделия)

public class ProductDao {

    private static final String BASE_SELECT =
            "SELECT product_id, name, description, assembly_days FROM products";

    public List<Product> findAll() throws SQLException {
        List<Product> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(BASE_SELECT + " ORDER BY product_id")) {
            while (rs.next()) result.add(mapRow(rs));
        }
        return result;
    }

    public Optional<Product> findById(int id) throws SQLException {
        String sql = BASE_SELECT + " WHERE product_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    public int insert(Product p) throws SQLException {
        String sql = "INSERT INTO products (name, description, assembly_days) VALUES (?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getName());
            ps.setString(2, p.getDescription());
            ps.setInt(3, p.getAssemblyDays());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    p.setId(id);
                    return id;
                }
            }
            throw new SQLException("Не удалось получить ключ");
        }
    }

    public boolean update(Product p) throws SQLException {
        String sql = "UPDATE products SET name = ?, description = ?, assembly_days = ? WHERE product_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getName());
            ps.setString(2, p.getDescription());
            ps.setInt(3, p.getAssemblyDays());
            ps.setInt(4, p.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM products WHERE product_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    //Работа с составом изделия (product_components)
    // Добавить компонент в состав изделия с указанием количества
    public void addComponent(int productId, int componentId, int quantity) throws SQLException {
        String sql = "INSERT INTO product_components (product_id, component_id, quantity) VALUES (?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ps.setInt(2, componentId);
            ps.setInt(3, quantity);
            ps.executeUpdate();
        }
    }

    //Получаем компоненты, входящие в изделие
    public List<Component> findComponentsByProduct(int productId) throws SQLException {
        String sql = """
                SELECT c.component_id, c.name, c.manufacturer, c.current_quantity, c.min_stock
                FROM   product_components pc
                JOIN   components c ON c.component_id = pc.component_id
                WHERE  pc.product_id = ?
                ORDER  BY c.name
                """;
        List<Component> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new Component(
                            rs.getInt("component_id"),
                            rs.getString("name"),
                            rs.getString("manufacturer"),
                            rs.getInt("current_quantity"),
                            rs.getInt("min_stock")
                    ));
                }
            }
        }
        return result;
    }

    private Product mapRow(ResultSet rs) throws SQLException {
        return new Product(
                rs.getInt("product_id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getInt("assembly_days")
        );
    }
}

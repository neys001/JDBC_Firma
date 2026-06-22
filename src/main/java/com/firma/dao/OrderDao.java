package com.firma.dao;

import com.firma.db.ConnectionManager;
import com.firma.model.Order;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO для таблицы orders + строки заказа (order_products, order_components).
 */
public class OrderDao {

    private static final String BASE_SELECT =
            "SELECT order_id, customer_id, agent_id, order_date, total_sum FROM orders";

    public List<Order> findAll() throws SQLException {
        List<Order> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(BASE_SELECT + " ORDER BY order_date DESC")) {
            while (rs.next()) result.add(mapRow(rs));
        }
        return result;
    }

    public Optional<Order> findById(int id) throws SQLException {
        String sql = BASE_SELECT + " WHERE order_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    public int insert(Order o) throws SQLException {
        String sql = "INSERT INTO orders (customer_id, agent_id, order_date, total_sum) VALUES (?, ?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, o.getCustomerId());
            if (o.getAgentId() != null) ps.setInt(2, o.getAgentId());
            else                        ps.setNull(2, Types.INTEGER);
            ps.setDate(3, Date.valueOf(o.getOrderDate()));
            ps.setBigDecimal(4, o.getTotalSum() != null ? o.getTotalSum() : BigDecimal.ZERO);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    o.setId(id);
                    return id;
                }
            }
            throw new SQLException("Не удалось получить ключ");
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM orders WHERE order_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // Добавить изделие в заказ
    public void addProductLine(int orderId, int productId, int quantity, BigDecimal price) throws SQLException {
        String sql = "INSERT INTO order_products (order_id, product_id, quantity, price) VALUES (?, ?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setInt(2, productId);
            ps.setInt(3, quantity);
            ps.setBigDecimal(4, price);
            ps.executeUpdate();
        }
    }

    // Добавить компонент в заказ
    public void addComponentLine(int orderId, int componentId, int quantity, BigDecimal price) throws SQLException {
        String sql = "INSERT INTO order_components (order_id, component_id, quantity, price) VALUES (?, ?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setInt(2, componentId);
            ps.setInt(3, quantity);
            ps.setBigDecimal(4, price);
            ps.executeUpdate();
        }
    }

//  создаём запись в orders
//  создаём строку в order_products
//  обновляем total_sum заказа
//  любая ошибка — rollback
    public int placeOrderForProduct(int customerId, Integer agentId, int productId,
                                    int quantity, BigDecimal price) throws SQLException {
        BigDecimal total = price.multiply(BigDecimal.valueOf(quantity));
        try (Connection conn = ConnectionManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int orderId;
                String insOrder = "INSERT INTO orders (customer_id, agent_id, order_date, total_sum) VALUES (?, ?, CURRENT_DATE, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insOrder, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, customerId);
                    if (agentId != null) ps.setInt(2, agentId);
                    else                 ps.setNull(2, Types.INTEGER);
                    ps.setBigDecimal(3, total);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("Не удалось получить ключ заказа");
                        orderId = keys.getInt(1);
                    }
                }

                String insLine = "INSERT INTO order_products (order_id, product_id, quantity, price) VALUES (?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insLine)) {
                    ps.setInt(1, orderId);
                    ps.setInt(2, productId);
                    ps.setInt(3, quantity);
                    ps.setBigDecimal(4, price);
                    ps.executeUpdate();
                }

                conn.commit();
                return orderId;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private Order mapRow(ResultSet rs) throws SQLException {
        int agent = rs.getInt("agent_id");
        Integer agentId = rs.wasNull() ? null : agent;
        return new Order(
                rs.getInt("order_id"),
                rs.getInt("customer_id"),
                agentId,
                rs.getDate("order_date").toLocalDate(),
                rs.getBigDecimal("total_sum")
        );
    }
}

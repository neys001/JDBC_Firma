package com.firma.dao;

import com.firma.db.ConnectionManager;
import com.firma.model.Supply;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

//DAO для таблицы supplies (поставки)
public class SupplyDao {

    private static final String BASE_SELECT =
            "SELECT supply_id, supplier_id, component_id, supply_date, volume, purchase_price, debt FROM supplies";

    public List<Supply> findAll() throws SQLException {
        List<Supply> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(BASE_SELECT + " ORDER BY supply_date DESC")) {
            while (rs.next()) result.add(mapRow(rs));
        }
        return result;
    }

    public Optional<Supply> findById(int id) throws SQLException {
        String sql = BASE_SELECT + " WHERE supply_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    public int insert(Supply s) throws SQLException {
        String sql = """
                INSERT INTO supplies (supplier_id, component_id, supply_date, volume, purchase_price, debt)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, s.getSupplierId());
            ps.setInt(2, s.getComponentId());
            ps.setDate(3, Date.valueOf(s.getSupplyDate()));
            ps.setInt(4, s.getVolume());
            ps.setBigDecimal(5, s.getPurchasePrice());
            ps.setBigDecimal(6, s.getDebt() != null ? s.getDebt() : BigDecimal.ZERO);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    s.setId(id);
                    return id;
                }
            }
            throw new SQLException("Не удалось получить ключ");
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM supplies WHERE supply_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }


// вставляем запись в supplies
// увеличиваем components.current_quantity на volume
// Если ошибка то rollback
    public int registerSupply(Supply s) throws SQLException {
        String insertSql = """
                INSERT INTO supplies (supplier_id, component_id, supply_date, volume, purchase_price, debt)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        String updateStockSql = "UPDATE components SET current_quantity = current_quantity + ? WHERE component_id = ?";

        try (Connection conn = ConnectionManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int supplyId;
                try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, s.getSupplierId());
                    ps.setInt(2, s.getComponentId());
                    ps.setDate(3, Date.valueOf(s.getSupplyDate()));
                    ps.setInt(4, s.getVolume());
                    ps.setBigDecimal(5, s.getPurchasePrice());
                    ps.setBigDecimal(6, s.getDebt() != null ? s.getDebt() : BigDecimal.ZERO);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("Не удалось получить ключ поставки");
                        supplyId = keys.getInt(1);
                    }
                }

                try (PreparedStatement ps = conn.prepareStatement(updateStockSql)) {
                    ps.setInt(1, s.getVolume());
                    ps.setInt(2, s.getComponentId());
                    int updated = ps.executeUpdate();
                    if (updated == 0) throw new SQLException("Компонент id=" + s.getComponentId() + " не найден");
                }

                conn.commit();
                s.setId(supplyId);
                return supplyId;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private Supply mapRow(ResultSet rs) throws SQLException {
        return new Supply(
                rs.getInt("supply_id"),
                rs.getInt("supplier_id"),
                rs.getInt("component_id"),
                rs.getDate("supply_date").toLocalDate(),
                rs.getInt("volume"),
                rs.getBigDecimal("purchase_price"),
                rs.getBigDecimal("debt")
        );
    }
}

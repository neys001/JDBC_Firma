package com.firma.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.stream.Collectors;

/**
 * Инициализация схемы БД и загрузка тестовых данных.
 */
public class SchemaInitializer {

    private static final Logger log = LoggerFactory.getLogger(SchemaInitializer.class);

    public static void initialize() throws SQLException {
        log.info("Инициализация схемы БД...");
        executeSqlFile("schema.sql");
        seedTestData();
        log.info("Схема БД создана и заполнена тестовыми данными");
    }

    private static void executeSqlFile(String fileName) throws SQLException {
        String sql;
        try (InputStream is = SchemaInitializer.class.getClassLoader().getResourceAsStream(fileName)) {
            if (is == null) throw new RuntimeException("SQL-файл не найден: " + fileName);
            sql = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                    .lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            throw new RuntimeException("Ошибка чтения SQL-файла: " + fileName, e);
        }
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            log.info("Выполнен SQL-файл: {}", fileName);
        }
    }

    private static void seedTestData() throws SQLException {
        try (Connection conn = ConnectionManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                seedSuppliers(conn);
                seedComponents(conn);
                seedProducts(conn);
                seedProductComponents(conn);
                seedCustomers(conn);
                seedSalesAgents(conn);
                seedSupplies(conn);
                seedOrders(conn);
                seedOrderProducts(conn);
                seedOrderComponents(conn);
                conn.commit();
                log.info("Тестовые данные загружены");
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private static void seedSuppliers(Connection conn) throws SQLException {
        String sql = "INSERT INTO suppliers (name, address, phone) VALUES (?, ?, ?)";
        Object[][] data = {
                {"ООО 'Электроком'",  "Москва, ул. Ленина, 5",         "+7-495-111-22-33"},
                {"ChipSupply Ltd",      "Санкт-Петербург, Невский, 22",  "+7-812-444-55-66"},
                {"АО 'Радиодеталь'",  "Новосибирск, Красный пр-т, 10", "+7-383-777-88-99"},
                {"Ростелеком", "Москва, Красная площадь", "+7-333-444-55-55"},
                {"РосАтом", "Норильск, ул. Ленина, 16", "+7-123-123-11-22"}
        };
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Object[] row : data) {
                ps.setString(1, (String) row[0]);
                ps.setString(2, (String) row[1]);
                ps.setString(3, (String) row[2]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void seedComponents(Connection conn) throws SQLException {
        String sql = "INSERT INTO components (name, manufacturer, current_quantity, min_stock) VALUES (?, ?, ?, ?)";
        Object[][] data = {
                {"Резистор 10 кОм", "Vishay", 500, 100},
                {"Конденсатор 10 мкФ", "Murata", 300, 50},
                {"Микроконтроллер ATmega328", "Atmel", 80, 20},
                {"Транзистор BC547", "NXP", 220,  50},
                {"Процессор БАЙКАЛ", "РосИНТЕЛ", 1000, 50},
                {"Медный провод 10 мм", "RusProvod", 300, 200},
                {"Диод 1N4148", "Vishay",  40,  60}  // ниже min_stock для проверки
        };
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Object[] row : data) {
                ps.setString(1, (String) row[0]);
                ps.setString(2, (String) row[1]);
                ps.setInt(3, (int) row[2]);
                ps.setInt(4, (int) row[3]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void seedProducts(Connection conn) throws SQLException {
        String sql = "INSERT INTO products (name, description, assembly_days) VALUES (?, ?, ?)";
        Object[][] data = {
                {"Плата управления", "Универсальная плата", 3},
                {"Блок реле", "Модуль на 4 реле", 1},
                {"Контроллер X3000", "Промышленный контроллер", 14},
                {"Квантовый компьютер", "Лучший пк в мире", 5}
        };
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Object[] row : data) {
                ps.setString(1, (String) row[0]);
                ps.setString(2, (String) row[1]);
                ps.setInt(3, (int) row[2]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void seedProductComponents(Connection conn) throws SQLException {
        String sql = "INSERT INTO product_components (product_id, component_id, quantity) VALUES (?, ?, ?)";
        int[][] data = {
                {1, 1, 8}, {1, 2, 4}, {1, 3, 1}, {1, 4, 4},
                {2, 1, 4}, {2, 4, 4},
                {3, 1, 200}, {3, 2, 100}, {3, 3, 4}, {3, 4, 50},
                {4, 1, 50}, {4, 2, 100}, {4, 3, 30}, {4, 4, 500}, {4, 5, 100}

        };
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int[] row : data) {
                ps.setInt(1, row[0]);
                ps.setInt(2, row[1]);
                ps.setInt(3, row[2]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void seedCustomers(Connection conn) throws SQLException {
        String sql = "INSERT INTO customers (organization_name, representative, contact_info) VALUES (?, ?, ?)";
        Object[][] data = {
                {"АО 'Прогресс'", "Иванов И.И.", "+7-495-100-20-30"},
                {"ООО 'ТехноПлюс'", "Петров П.П.", "tech@plus.example"},
                {"ПАО 'Энергия'", "Васильев В.В.", "info@energy.example"}
        };
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Object[] row : data) {
                ps.setString(1, (String) row[0]);
                ps.setString(2, (String) row[1]);
                ps.setString(3, (String) row[2]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void seedSalesAgents(Connection conn) throws SQLException {
        String sql = "INSERT INTO sales_agents (full_name, email, pager_number, phone_number, pbx_code) VALUES (?, ?, ?, ?, ?)";
        Object[][] data = {
                {"Сидоров А.А.", "sidorov@firma.example", "12345", "+7-495-555-22-11", "495"},
                {"Кузнецов В.В.", "kuznetsov@firma.example", null, "+7-495-555-22-12", "495"},
                {"Морозов Д.Д.", "morozov@firma.example", null, "+7-812-100-33-44", "812"}
        };
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Object[] row : data) {
                ps.setString(1, (String) row[0]);
                ps.setString(2, (String) row[1]);
                ps.setString(3, (String) row[2]);
                ps.setString(4, (String) row[3]);
                ps.setString(5, (String) row[4]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void seedSupplies(Connection conn) throws SQLException {
        String sql = "INSERT INTO supplies (supplier_id, component_id, supply_date, volume, purchase_price, debt) VALUES (?, ?, ?::DATE, ?, ?, ?)";
        Object[][] data = {
                {1, 1, "2026-05-10", 200, "2.50", "0"},
                {1, 2, "2026-05-10", 100, "5.00", "250"},
                {2, 3, "2026-05-15", 50, "180.00", "9000"},
                {3, 4, "2026-05-20", 100, "3.20", "0"},
                {2, 1, "2026-06-01", 100, "2.40", "0"}   // тот же компонент от другого поставщика
        };
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Object[] row : data) {
                ps.setInt(1, (int) row[0]);
                ps.setInt(2, (int) row[1]);
                ps.setString(3, (String) row[2]);
                ps.setInt(4, (int) row[3]);
                ps.setBigDecimal(5, new BigDecimal((String) row[4]));
                ps.setBigDecimal(6, new BigDecimal((String) row[5]));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void seedOrders(Connection conn) throws SQLException {
        String sql = "INSERT INTO orders (customer_id, agent_id, order_date, total_sum) VALUES (?, ?, ?::DATE, ?)";
        // (customer_id, agent_id (или null), date, total)
        Object[][] data = {
                {1, 1,    "2026-06-01", "12500.00"},
                {2, null, "2026-06-05",  "3500.00"},   // напрямую без агента
                {3, 3,    "2026-06-10",  "5000.00"},
                {1, 2,    "2025-12-15",  "8000.00"}    // прошлый год для проверки
        };
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Object[] row : data) {
                ps.setInt(1, (int) row[0]);
                if (row[1] != null) ps.setInt(2, (int) row[1]);
                else                ps.setNull(2, Types.INTEGER);
                ps.setString(3, (String) row[2]);
                ps.setBigDecimal(4, new BigDecimal((String) row[3]));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void seedOrderProducts(Connection conn) throws SQLException {
        String sql = "INSERT INTO order_products (order_id, product_id, quantity, price) VALUES (?, ?, ?, ?)";
        Object[][] data = {
                {1, 1, 5, "2000.00"},
                {1, 2, 1, "2500.00"},
                {3, 2, 2, "2500.00"},
                {4, 1, 4, "2000.00"}
        };
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Object[] row : data) {
                ps.setInt(1, (int) row[0]);
                ps.setInt(2, (int) row[1]);
                ps.setInt(3, (int) row[2]);
                ps.setBigDecimal(4, new BigDecimal((String) row[3]));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void seedOrderComponents(Connection conn) throws SQLException {
        String sql = "INSERT INTO order_components (order_id, component_id, quantity, price) VALUES (?, ?, ?, ?)";
        Object[][] data = {
                {2, 1, 100, "5.00"},
                {2, 3, 5, "600.00"}
        };
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Object[] row : data) {
                ps.setInt(1, (int) row[0]);
                ps.setInt(2, (int) row[1]);
                ps.setInt(3, (int) row[2]);
                ps.setBigDecimal(4, new BigDecimal((String) row[3]));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
}

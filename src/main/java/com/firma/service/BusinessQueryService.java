package com.firma.service;

import com.firma.db.ConnectionManager;

import java.sql.*;
import java.time.LocalDate;

//Изделия со сроком сборки больше N дней
//Поставщики, поставляющие компоненты определённой фирмы-изготовителя
//Торговые агенты с определённым кодом АТС
//Клиенты с покупками за период

public class BusinessQueryService {

    public void productsByAssemblyDays(int minDays) throws SQLException {
        System.out.println("Изделия со сроком сборки > " + minDays + " дн.");
        String sql = """
                SELECT product_id, name, assembly_days, description
                FROM   products
                WHERE  assembly_days > ?
                ORDER  BY assembly_days DESC
                """;
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, minDays);
            try (ResultSet rs = ps.executeQuery()) {
                System.out.printf("%-5s %-26s %-10s %-30s%n", "ID", "Название", "Дней", "Описание");
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    System.out.printf("%-5d %-26s %-10d %-30s%n",
                            rs.getInt("product_id"),
                            truncate(rs.getString("name"), 25),
                            rs.getInt("assembly_days"),
                            truncate(rs.getString("description"), 29));
                }
                if (!found) System.out.println("(ничего не найдено)");
            }
        }
        System.out.println();
    }

    public void suppliersByManufacturer(String manufacturer) throws SQLException {
        System.out.println("Поставщики компонентов фирмы '" + manufacturer + "' ===");
        String sql = """
                SELECT DISTINCT s.supplier_id, s.name, s.address, s.phone
                FROM   suppliers   s
                JOIN   supplies   sp ON sp.supplier_id = s.supplier_id
                JOIN   components c  ON c.component_id = sp.component_id
                WHERE  c.manufacturer = ?
                ORDER  BY s.name
                """;
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, manufacturer);
            try (ResultSet rs = ps.executeQuery()) {
                System.out.printf("%-5s %-30s %-30s %-20s%n", "ID", "Поставщик", "Адрес", "Телефон");
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    System.out.printf("%-5d %-30s %-30s %-20s%n",
                            rs.getInt("supplier_id"),
                            truncate(rs.getString("name"), 29),
                            truncate(rs.getString("address"), 29),
                            rs.getString("phone"));
                }
                if (!found) System.out.println("(ничего не найдено)");
            }
        }
        System.out.println();
    }

    public void agentsByPbxCode(String pbxCode) throws SQLException {
        System.out.println("Агенты с АТС '" + pbxCode + "' ===");
        String sql = """
                SELECT agent_id, full_name, phone_number, pbx_code, email
                FROM   sales_agents
                WHERE  pbx_code = ?
                ORDER  BY full_name
                """;
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pbxCode);
            try (ResultSet rs = ps.executeQuery()) {
                System.out.printf("%-5s %-25s %-22s %-8s %-25s%n",
                        "ID", "ФИО", "Телефон", "АТС", "Email");
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    System.out.printf("%-5d %-25s %-22s %-8s %-25s%n",
                            rs.getInt("agent_id"),
                            truncate(rs.getString("full_name"), 24),
                            rs.getString("phone_number"),
                            rs.getString("pbx_code"),
                            truncate(rs.getString("email"), 24));
                }
                if (!found) System.out.println("(ничего не найдено)");
            }
        }
        System.out.println();
    }

    public void customersWithPurchasesInPeriod(LocalDate from, LocalDate to) throws SQLException {
        System.out.println("Клиенты с покупками в [" + from + " .. " + to + "]");
        String sql = """
                SELECT DISTINCT c.customer_id, c.organization_name, c.representative, c.contact_info
                FROM   customers c
                JOIN   orders    o ON o.customer_id = c.customer_id
                WHERE  o.order_date BETWEEN ? AND ?
                  AND  ( EXISTS (SELECT 1 FROM order_products   op WHERE op.order_id = o.order_id)
                      OR EXISTS (SELECT 1 FROM order_components oc WHERE oc.order_id = o.order_id) )
                ORDER  BY c.organization_name
                """;
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(from));
            ps.setDate(2, Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                System.out.printf("%-5s %-30s %-25s %-30s%n",
                        "ID", "Организация", "Представитель", "Контакты");
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    System.out.printf("%-5d %-30s %-25s %-30s%n",
                            rs.getInt("customer_id"),
                            truncate(rs.getString("organization_name"), 29),
                            truncate(rs.getString("representative"), 24),
                            truncate(rs.getString("contact_info"), 29));
                }
                if (!found) System.out.println("(ничего не найдено)");
            }
        }
        System.out.println();
    }


    //компоненты ниже минимального запаса
    public void componentsBelowMinStock() throws SQLException {
        System.out.println("=== Доп: компоненты с запасом ниже минимума ===");
        String sql = """
                SELECT component_id, name, manufacturer, current_quantity, min_stock,
                       (min_stock - current_quantity) AS shortage
                FROM   components
                WHERE  current_quantity < min_stock
                ORDER  BY shortage DESC
                """;
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.printf("%-5s %-30s %-16s %-12s %-10s %-10s%n",
                    "ID", "Название", "Изготовитель", "На складе", "Мин.", "Дефицит");
            while (rs.next()) {
                System.out.printf("%-5d %-30s %-16s %-12d %-10d %-10d%n",
                        rs.getInt("component_id"),
                        truncate(rs.getString("name"), 29),
                        rs.getString("manufacturer"),
                        rs.getInt("current_quantity"),
                        rs.getInt("min_stock"),
                        rs.getInt("shortage"));
            }
        }
        System.out.println();
    }

    //общая сумма закупок у каждого поставщика
    public void supplierTotalSpend() throws SQLException {
        System.out.println("=== Доп: общая сумма закупок по поставщикам ===");
        String sql = """
                SELECT s.name,
                COUNT(sp.supply_id) AS supplies_count,
                SUM(sp.volume * sp.purchase_price) AS total_spend,
                SUM(sp.debt) AS total_debt
                FROM   suppliers s
                LEFT JOIN supplies sp ON sp.supplier_id = s.supplier_id
                GROUP BY s.supplier_id, s.name
                ORDER  BY total_spend DESC NULLS LAST
                """;
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.printf("%-32s %-10s %-15s %-15s%n",
                    "Поставщик", "Поставок", "Закуплено (₽)", "Задолж. (₽)");
            while (rs.next()) {
                System.out.printf("%-32s %-10d %-15.2f %-15.2f%n",
                        truncate(rs.getString("name"), 31),
                        rs.getInt("supplies_count"),
                        rs.getBigDecimal("total_spend") != null ? rs.getBigDecimal("total_spend") : java.math.BigDecimal.ZERO,
                        rs.getBigDecimal("total_debt")  != null ? rs.getBigDecimal("total_debt")  : java.math.BigDecimal.ZERO);
            }
        }
        System.out.println();
    }


    public void runAll() throws SQLException {
        productsByAssemblyDays(5);
        suppliersByManufacturer("Vishay");
        agentsByPbxCode("495");
        customersWithPurchasesInPeriod(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));
        componentsBelowMinStock();
        supplierTotalSpend();
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max - 1) + "…" : s;
    }
}

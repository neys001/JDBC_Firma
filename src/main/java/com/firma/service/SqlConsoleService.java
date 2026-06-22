package com.firma.service;

import com.firma.db.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

//Выполнение произвольного SQL-запроса и вывод результата в виде таблицы
public class SqlConsoleService {

    private static final int MAX_COL_WIDTH = 40;

    public void execute(String sql) {
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement()) {

            boolean hasResultSet = stmt.execute(sql);
            if (hasResultSet) {
                try (ResultSet rs = stmt.getResultSet()) {
                    printAsTable(rs);
                }
            } else {
                int affected = stmt.getUpdateCount();
                System.out.printf("Запрос выполнен. Затронуто строк: %d%n", affected);
            }
        } catch (SQLException e) {
            System.err.println("Ошибка SQL: " + e.getMessage());
        }
    }

    private void printAsTable(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int cols = meta.getColumnCount();

        //Заголовки
        String[] headers = new String[cols];
        for (int i = 0; i < cols; i++) {
            headers[i] = meta.getColumnLabel(i + 1);
        }

        //Собираем все строки в память (для расчёта ширины колонок)
        List<String[]> rows = new ArrayList<>();
        while (rs.next()) {
            String[] row = new String[cols];
            for (int i = 0; i < cols; i++) {
                Object v = rs.getObject(i + 1);
                row[i] = (v == null) ? "NULL" : v.toString();
            }
            rows.add(row);
        }

        //Считаем ширину каждой колонки
        int[] widths = new int[cols];
        for (int i = 0; i < cols; i++) widths[i] = headers[i].length();
        for (String[] row : rows) {
            for (int i = 0; i < cols; i++) {
                widths[i] = Math.max(widths[i], row[i].length());
            }
        }
        for (int i = 0; i < cols; i++) widths[i] = Math.min(widths[i], MAX_COL_WIDTH);

        //вывод
        String separator = buildSeparator(widths);
        System.out.println(separator);
        System.out.println(buildRow(headers, widths));
        System.out.println(separator);
        for (String[] row : rows) {
            System.out.println(buildRow(row, widths));
        }
        System.out.println(separator);
        System.out.printf("Всего строк: %d%n", rows.size());
    }

    private String buildSeparator(int[] widths) {
        StringBuilder sb = new StringBuilder("+");
        for (int w : widths) sb.append("-".repeat(w + 2)).append("+");
        return sb.toString();
    }

    private String buildRow(String[] values, int[] widths) {
        StringBuilder sb = new StringBuilder("|");
        for (int i = 0; i < values.length; i++) {
            String v = truncate(values[i], widths[i]);
            sb.append(" ").append(pad(v, widths[i])).append(" |");
        }
        return sb.toString();
    }

    private String pad(String s, int width) {
        return s.length() >= width ? s : s + " ".repeat(width - s.length());
    }

    private String truncate(String s, int max) {
        if (s == null) return "NULL";
        return s.length() > max ? s.substring(0, max - 1) + "…" : s;
    }
}

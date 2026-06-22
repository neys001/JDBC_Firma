package com.firma;

import com.firma.db.ConnectionManager;
import com.firma.db.SchemaInitializer;
import com.firma.service.BusinessQueryService;
import com.firma.service.CrudDemoService;
import com.firma.service.SqlConsoleService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Scanner;

public class Main {

    private static final CrudDemoService crudDemo = new CrudDemoService();
    private static final BusinessQueryService bizQuery = new BusinessQueryService();
    private static final SqlConsoleService sqlConsole = new SqlConsoleService();

    public static void main(String[] args) {
        System.out.println("JDBC Фирма-сборщик\n");

        try {
            SchemaInitializer.initialize();
            System.out.println("БД готова.\n");
        } catch (SQLException e) {
            System.err.println("Ошибка инициализации: " + e.getMessage());
            return;
        }

        Scanner scanner = new Scanner(System.in);
        boolean running = true;
        while (running) {
            System.out.print("""
                    [1] CRUD  [2] Запросы  [3] Всё  [4] SQL-консоль  [5] Сбросить БД  [0] Выход
                    > """);
            try {
                switch (scanner.nextLine().trim()) {
                    case "1" -> runCrudMenu(scanner);
                    case "2" -> runBusinessMenu(scanner);
                    case "3" -> runAllDemo();
                    case "4" -> runSqlConsole(scanner);
                    case "5" -> {
                        SchemaInitializer.initialize();
                        System.out.println("БД пересоздана и заполнена тестовыми данными.\n");
                    }
                    case "0" -> running = false;
                    default  -> System.out.println("Неверный выбор.");
                }
            } catch (SQLException e) {
                System.err.println("Ошибка SQL: " + e.getMessage());
            }
        }
        System.out.println("До свидания!");
        ConnectionManager.close();
    }

    //CRUD
    private static void runCrudMenu(Scanner scanner) throws SQLException {
        while (true) {
            System.out.print("""
                    [1] Create  [2] Read  [3] Update  [4] Delete
                    [5] Batch   [6] Транзакция-поставка  [7] Транзакция-заказ  [8] Всё  [0] Назад
                    """);
            switch (scanner.nextLine().trim()) {
                case "1" -> crudDemo.demoCreate();
                case "2" -> crudDemo.demoRead();
                case "3" -> crudDemo.demoUpdate();
                case "4" -> crudDemo.demoDelete();
                case "5" -> crudDemo.demoBatchInsert();
                case "6" -> crudDemo.demoTransactionSupply();
                case "7" -> crudDemo.demoTransactionOrder();
                case "8" -> runAllCrud();
                case "0" -> { return; }
                default  -> System.out.println("Неверный выбор.");
            }
        }
    }

    private static void runAllCrud() throws SQLException {
        crudDemo.demoCreate();
        crudDemo.demoRead();
        crudDemo.demoUpdate();
        crudDemo.demoDelete();
        crudDemo.demoBatchInsert();
        crudDemo.demoTransactionSupply();
        crudDemo.demoTransactionOrder();
    }

    //Бизнес-запросы
    private static void runBusinessMenu(Scanner scanner) throws SQLException {
        while (true) {
            System.out.print("""
                    [1] Изделия по сроку сборки
                    [2] Поставщики по фирме-изготовителю
                    [3] Агенты по АТС
                    [4] Клиенты с покупками за период
                    [5] компоненты ниже минимума
                    [6] закупки по поставщикам
                    [7] Всё  [0] Назад
                    > """);
            switch (scanner.nextLine().trim()) {
                case "1" -> {
                    System.out.print("Минимум дней [5]: ");
                    String v = scanner.nextLine().trim();
                    bizQuery.productsByAssemblyDays(v.isEmpty() ? 5 : Integer.parseInt(v));
                }
                case "2" -> {
                    System.out.print("Фирма-изготовитель [Vishay]: ");
                    String v = scanner.nextLine().trim();
                    bizQuery.suppliersByManufacturer(v.isEmpty() ? "Vishay" : v);
                }
                case "3" -> {
                    System.out.print("Код АТС [495]: ");
                    String v = scanner.nextLine().trim();
                    bizQuery.agentsByPbxCode(v.isEmpty() ? "495" : v);
                }
                case "4" -> {
                    System.out.print("С даты [2026-01-01]: ");
                    String f = scanner.nextLine().trim();
                    System.out.print("По дату [2026-12-31]: ");
                    String t = scanner.nextLine().trim();
                    LocalDate from = LocalDate.parse(f.isEmpty() ? "2026-01-01" : f);
                    LocalDate to   = LocalDate.parse(t.isEmpty() ? "2026-12-31" : t);
                    bizQuery.customersWithPurchasesInPeriod(from, to);
                }
                case "5" -> bizQuery.componentsBelowMinStock();
                case "6" -> bizQuery.supplierTotalSpend();
                case "7" -> bizQuery.runAll();
                case "0" -> { return; }
                default  -> System.out.println("Неверный выбор.");
            }
        }
    }

    //всё
    private static void runAllDemo() throws SQLException {
        System.out.println("\n--- CRUD ---");
        runAllCrud();
        System.out.println("\n--- Бизнес-запросы ---");
        bizQuery.runAll();
        System.out.println("\nГотово.");
    }

    private static void runSqlConsole(Scanner scanner) {
        System.out.println("Введи SQL-запрос (одну или несколько строк) 'exit' — выйти из консоли");
        while (true) {
            System.out.print("sql> ");
            StringBuilder sb = new StringBuilder();
            while (true) {
                String line = scanner.nextLine();
                if (line.isEmpty()) break;
                if ("exit".equalsIgnoreCase(line.trim())) return;
                sb.append(line).append('\n');
            }
            String sql = sb.toString().trim();
            if (!sql.isEmpty()) sqlConsole.execute(sql);
        }
    }
}
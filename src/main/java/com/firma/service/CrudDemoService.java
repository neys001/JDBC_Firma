package com.firma.service;

import com.firma.dao.*;
import com.firma.model.*;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


//Демонстрация CRUD-операций через JDBC.
public class CrudDemoService {

    private final SupplierDao   supplierDao   = new SupplierDao();
    private final ComponentDao  componentDao  = new ComponentDao();
    private final ProductDao    productDao    = new ProductDao();
    private final CustomerDao   customerDao   = new CustomerDao();
    private final SalesAgentDao agentDao      = new SalesAgentDao();
    private final OrderDao      orderDao      = new OrderDao();
    private final SupplyDao     supplyDao     = new SupplyDao();

    // CREATE
    public void demoCreate() throws SQLException {
        System.out.println(" Создание записей ");
        cleanupDemoCreateData();

        Supplier sup = new Supplier("ООО 'ТестСнаб'", "Казань, ул. Тестовая 1", "+7-843-111-22-33");
        int sId = supplierDao.insert(sup);
        System.out.printf("Создан поставщик: id=%d, '%s'%n", sId, sup.getName());

        Component comp = new Component("Тестовый резистор", "TestCorp", 100, 30);
        int cId = componentDao.insert(comp);
        System.out.printf("Создан компонент: id=%d, '%s'%n", cId, comp.getName());

        Product prod = new Product("Тестовое изделие", "Для проверки", 7);
        int pId = productDao.insert(prod);
        System.out.printf("Создано изделие: id=%d, '%s', сборка %d дн.%n",
                pId, prod.getName(), prod.getAssemblyDays());

        productDao.addComponent(pId, cId, 5);
        System.out.printf("Состав изделия id=%d пополнен: +5 шт. компонента id=%d%n", pId, cId);

        System.out.println();
    }

    // READ
    public void demoRead() throws SQLException {
        System.out.println(" Чтение данных ");

        System.out.println("Все поставщики:");
        System.out.printf("%-5s %-32s %-30s %-20s%n", "ID", "Название", "Адрес", "Телефон");
        for (Supplier s : supplierDao.findAll()) {
            System.out.printf("%-5d %-32s %-30s %-20s%n",
                    s.getId(), truncate(s.getName(), 31),
                    truncate(s.getAddress(), 29), s.getPhone());
        }

        System.out.println("\nВсе компоненты:");
        System.out.printf("%-5s %-30s %-16s %-10s %-10s%n", "ID", "Название", "Изготовитель", "На складе", "Мин.");
        for (Component c : componentDao.findAll()) {
            System.out.printf("%-5d %-30s %-16s %-10d %-10d%n",
                    c.getId(), truncate(c.getName(), 29), c.getManufacturer(),
                    c.getCurrentQuantity(), c.getMinStock());
        }

        System.out.println("\nИзделия (с составом):");
        for (Product p : productDao.findAll()) {
            System.out.printf("  id=%d  '%s'  (сборка %d дн.)%n",
                    p.getId(), p.getName(), p.getAssemblyDays());
            List<Component> parts = productDao.findComponentsByProduct(p.getId());
            for (Component c : parts) {
                System.out.printf(" %s (%s)%n", c.getName(), c.getManufacturer());
            }
        }

        System.out.println("\nПоиск поставщика id=1:");
        supplierDao.findById(1).ifPresentOrElse(System.out::println, () -> System.out.println("Не найден"));

        System.out.println();
    }

    // UPDATE
    public void demoUpdate() throws SQLException {
        System.out.println("=== UPDATE — Обновление данных ===");

        supplierDao.findById(1).ifPresent(s -> {
            String old = s.getPhone();
            s.setPhone("+7-495-999-99-99");
            try {
                boolean ok = supplierDao.update(s);
                System.out.printf("Обновлён телефон поставщика id=1: '%s' → '%s' (успех=%b)%n",
                        old, s.getPhone(), ok);
                s.setPhone(old);
                supplierDao.update(s);
            } catch (SQLException e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        });

        componentDao.findById(1).ifPresent(c -> {
            int old = c.getCurrentQuantity();
            c.setCurrentQuantity(old + 10);
            try {
                componentDao.update(c);
                System.out.printf("Запас компонента id=1: %d → %d%n", old, c.getCurrentQuantity());
                c.setCurrentQuantity(old);
                componentDao.update(c);
            } catch (SQLException e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        });

        System.out.println();
    }

    // DELETE
    public void demoDelete() throws SQLException {
        System.out.println(" Удаление данных ");

        Customer temp = new Customer("Удали ООО", "Никто", "никак");
        int id = customerDao.insert(temp);
        System.out.printf("Создан временный клиент id=%d%n", id);

        boolean deleted = customerDao.delete(id);
        System.out.printf("Удалён клиент id=%d (успех=%b)%n", id, deleted);

        boolean notFound = customerDao.delete(99999);
        System.out.printf("Удаление несуществующего id=99999 (успех=%b)%n", notFound);

        System.out.println();
    }

    // BATCH INSERT
    public void demoBatchInsert() throws SQLException {
        System.out.println(" Пакетная вставка ");

        List<Component> batch = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            batch.add(new Component("Тест-компонент " + i, "BatchCorp", 50, 10));
        }

        long start = System.nanoTime();
        int count = componentDao.batchInsert(batch);
        long elapsed = (System.nanoTime() - start) / 1_000_000;

        System.out.printf("Вставлено %d компонентов за %d мс (batch)%n", count, elapsed);

        //удалим всё, что создали
        for (Component c : componentDao.findAll()) {
            if (c.getManufacturer().equals("BatchCorp")) componentDao.delete(c.getId());
        }
        System.out.println("Тестовые компоненты удалены");
        System.out.println();
    }

    //регистрация поставки = INSERT в supplies + UPDATE склада
    public void demoTransactionSupply() throws SQLException {
        System.out.println("Регистрация поставки ");

        Component before = componentDao.findById(1).orElseThrow();
        System.out.printf("Запас компонента id=1 ДО поставки: %d%n", before.getCurrentQuantity());

        Supply s = new Supply(1, 1, LocalDate.now(), 50, new BigDecimal("2.30"), BigDecimal.ZERO);
        int supplyId = supplyDao.registerSupply(s);
        System.out.printf("Зарегистрирована поставка id=%d (50 шт.)%n", supplyId);

        Component after = componentDao.findById(1).orElseThrow();
        System.out.printf("Запас компонента id=1 ПОСЛЕ поставки: %d%n", after.getCurrentQuantity());

        // Откатываем для повторного запуска
        supplyDao.delete(supplyId);
        Component rollback = componentDao.findById(1).orElseThrow();
        rollback.setCurrentQuantity(before.getCurrentQuantity());
        componentDao.update(rollback);

        System.out.println();
    }

    //оформление заказа
    public void demoTransactionOrder() throws SQLException {
        System.out.println("Оформление заказа ");
        System.out.println("Клиент=1, агент=1, изделие=1, кол-во=2, цена=2000.00");

        int orderId = orderDao.placeOrderForProduct(1, 1, 1, 2, new BigDecimal("2000.00"));
        System.out.printf("Заказ создан: id=%d%n", orderId);

        // Cleanup
        orderDao.delete(orderId);
        System.out.printf("Заказ id=%d удалён (CASCADE убрал и позиции)%n", orderId);

        System.out.println();
    }

    public static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max - 1) + "…" : s;
    }

    //Удаляет записи, которые создаёт demoCreate(), чтобы повторный вызов работал
    private void cleanupDemoCreateData() throws SQLException {
        for (Product p : productDao.findAll()) {
            if ("Тестовое изделие".equals(p.getName())) productDao.delete(p.getId());
        }
        for (Component c : componentDao.findAll()) {
            if ("Тестовый резистор".equals(c.getName()) && "TestCorp".equals(c.getManufacturer())) {
                componentDao.delete(c.getId());
            }
        }
        for (Supplier s : supplierDao.findAll()) {
            if ("ООО \"ТестСнаб\"".equals(s.getName())) supplierDao.delete(s.getId());
        }
    }
}

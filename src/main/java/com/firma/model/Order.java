package com.firma.model;

import java.math.BigDecimal;
import java.time.LocalDate;

//Заказ клиента
public class Order {
    private int id;
    private int customerId;
    private Integer agentId;       // nullable
    private LocalDate orderDate;
    private BigDecimal totalSum;

    private String customerName;
    private String agentName;

    public Order() {}

    public Order(int id, int customerId, Integer agentId, LocalDate orderDate, BigDecimal totalSum) {
        this.id = id;
        this.customerId = customerId;
        this.agentId = agentId;
        this.orderDate = orderDate;
        this.totalSum = totalSum;
    }

    public Order(int customerId, Integer agentId, LocalDate orderDate, BigDecimal totalSum) {
        this(0, customerId, agentId, orderDate, totalSum);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public Integer getAgentId() { return agentId; }
    public void setAgentId(Integer agentId) { this.agentId = agentId; }
    public LocalDate getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDate orderDate) { this.orderDate = orderDate; }
    public BigDecimal getTotalSum() { return totalSum; }
    public void setTotalSum(BigDecimal totalSum) { this.totalSum = totalSum; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getAgentName() { return agentName; }
    public void setAgentName(String agentName) { this.agentName = agentName; }

    @Override
    public String toString() {
        return String.format("Заказ{id=%d, клиент=%d, агент=%s, дата=%s, сумма=%s}",
                id, customerId, agentId, orderDate, totalSum);
    }
}

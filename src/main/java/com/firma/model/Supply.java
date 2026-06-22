package com.firma.model;

import java.math.BigDecimal;
import java.time.LocalDate;

//Поставка
public class Supply {
    private int id;
    private int supplierId;
    private int componentId;
    private LocalDate supplyDate;
    private int volume;
    private BigDecimal purchasePrice;
    private BigDecimal debt;

    public Supply() {}

    public Supply(int id, int supplierId, int componentId, LocalDate supplyDate,
                  int volume, BigDecimal purchasePrice, BigDecimal debt) {
        this.id = id;
        this.supplierId = supplierId;
        this.componentId = componentId;
        this.supplyDate = supplyDate;
        this.volume = volume;
        this.purchasePrice = purchasePrice;
        this.debt = debt;
    }

    public Supply(int supplierId, int componentId, LocalDate supplyDate,
                  int volume, BigDecimal purchasePrice, BigDecimal debt) {
        this(0, supplierId, componentId, supplyDate, volume, purchasePrice, debt);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getSupplierId() { return supplierId; }
    public void setSupplierId(int supplierId) { this.supplierId = supplierId; }
    public int getComponentId() { return componentId; }
    public void setComponentId(int componentId) { this.componentId = componentId; }
    public LocalDate getSupplyDate() { return supplyDate; }
    public void setSupplyDate(LocalDate supplyDate) { this.supplyDate = supplyDate; }
    public int getVolume() { return volume; }
    public void setVolume(int volume) { this.volume = volume; }
    public BigDecimal getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(BigDecimal purchasePrice) { this.purchasePrice = purchasePrice; }
    public BigDecimal getDebt() { return debt; }
    public void setDebt(BigDecimal debt) { this.debt = debt; }

    @Override
    public String toString() {
        return String.format("Поставка{id=%d, поставщик=%d, компонент=%d, дата=%s, объём=%d, цена=%s}",
                id, supplierId, componentId, supplyDate, volume, purchasePrice);
    }
}

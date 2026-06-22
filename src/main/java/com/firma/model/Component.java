package com.firma.model;

public class Component {
    private int id;
    private String name;
    private String manufacturer;
    private int currentQuantity;
    private int minStock;

    public Component() {}

    public Component(int id, String name, String manufacturer, int currentQuantity, int minStock) {
        this.id = id;
        this.name = name;
        this.manufacturer = manufacturer;
        this.currentQuantity = currentQuantity;
        this.minStock = minStock;
    }

    public Component(String name, String manufacturer, int currentQuantity, int minStock) {
        this(0, name, manufacturer, currentQuantity, minStock);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }
    public int getCurrentQuantity() { return currentQuantity; }
    public void setCurrentQuantity(int currentQuantity) { this.currentQuantity = currentQuantity; }
    public int getMinStock() { return minStock; }
    public void setMinStock(int minStock) { this.minStock = minStock; }

    @Override
    public String toString() {
        return String.format("Компонент{id=%d, '%s' (%s), на складе=%d, мин=%d}",
                id, name, manufacturer, currentQuantity, minStock);
    }
}

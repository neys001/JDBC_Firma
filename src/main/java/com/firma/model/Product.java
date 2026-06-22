package com.firma.model;

//изделие
public class Product {
    private int id;
    private String name;
    private String description;
    private int assemblyDays;

    public Product() {}

    public Product(int id, String name, String description, int assemblyDays) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.assemblyDays = assemblyDays;
    }

    public Product(String name, String description, int assemblyDays) {
        this(0, name, description, assemblyDays);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getAssemblyDays() { return assemblyDays; }
    public void setAssemblyDays(int assemblyDays) { this.assemblyDays = assemblyDays; }

    @Override
    public String toString() {
        return String.format("Изделие{id=%d, '%s', сборка=%d дн.}", id, name, assemblyDays);
    }
}

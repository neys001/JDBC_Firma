package com.firma.model;

// Клиент-организация в лице представителя
public class Customer {
    private int id;
    private String organizationName;
    private String representative;
    private String contactInfo;

    public Customer() {}

    public Customer(int id, String organizationName, String representative, String contactInfo) {
        this.id = id;
        this.organizationName = organizationName;
        this.representative = representative;
        this.contactInfo = contactInfo;
    }

    public Customer(String organizationName, String representative, String contactInfo) {
        this(0, organizationName, representative, contactInfo);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }
    public String getRepresentative() { return representative; }
    public void setRepresentative(String representative) { this.representative = representative; }
    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }

    @Override
    public String toString() {
        return String.format("Клиент{id=%d, '%s', представитель='%s'}",
                id, organizationName, representative);
    }
}

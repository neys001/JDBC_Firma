package com.firma.model;

// Торговый агент
public class SalesAgent {
    private int id;
    private String fullName;
    private String email;
    private String pagerNumber;
    private String phoneNumber;
    private String pbxCode;

    public SalesAgent() {}

    public SalesAgent(int id, String fullName, String email, String pagerNumber,
                      String phoneNumber, String pbxCode) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.pagerNumber = pagerNumber;
        this.phoneNumber = phoneNumber;
        this.pbxCode = pbxCode;
    }

    public SalesAgent(String fullName, String email, String pagerNumber,
                      String phoneNumber, String pbxCode) {
        this(0, fullName, email, pagerNumber, phoneNumber, pbxCode);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPagerNumber() { return pagerNumber; }
    public void setPagerNumber(String pagerNumber) { this.pagerNumber = pagerNumber; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getPbxCode() { return pbxCode; }
    public void setPbxCode(String pbxCode) { this.pbxCode = pbxCode; }

    @Override
    public String toString() {
        return String.format("Агент{id=%d, '%s', тел='%s', АТС=%s}",
                id, fullName, phoneNumber, pbxCode);
    }
}

package com.pc.mediclaim.model;

public class Policy {
    private int id;
    private int userId;
    private String company;
    private String type;
    private String policyNo;
    private String validFrom;
    private String validTo;
    private String status;
    private double sumInsured;
    private double utilized;

    public Policy(int id, int userId, String company, String type, String policyNo, String validFrom, String validTo, String status, double sumInsured, double utilized) {
        this.id = id;
        this.userId = userId;
        this.company = company;
        this.type = type;
        this.policyNo = policyNo;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.status = status;
        this.sumInsured = sumInsured;
        this.utilized = utilized;
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getCompany() { return company; }
    public String getType() { return type; }
    public String getPolicyNo() { return policyNo; }
    public String getValidFrom() { return validFrom; }
    public String getValidTo() { return validTo; }
    public String getStatus() { return status; }
    public double getSumInsured() { return sumInsured; }
    public double getUtilized() { return utilized; }
    public double getAvailable() { return sumInsured - utilized; }
}

package com.pc.mediclaim.model;

public class Claim {
    private int id;
    private int userId;
    private String patientName;
    private String doa;
    private String dod;
    private double amount;
    private String claimNo;
    private String status;
    private String documentUri;

    public Claim(int id, int userId, String patientName, String doa, String dod, double amount, String claimNo, String status, String documentUri) {
        this.id = id;
        this.userId = userId;
        this.patientName = patientName;
        this.doa = doa;
        this.dod = dod;
        this.amount = amount;
        this.claimNo = claimNo;
        this.status = status;
        this.documentUri = documentUri;
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getPatientName() { return patientName; }
    public String getDoa() { return doa; }
    public String getDod() { return dod; }
    public double getAmount() { return amount; }
    public String getClaimNo() { return claimNo; }
    public String getStatus() { return status; }
    public String getDocumentUri() { return documentUri; }
}

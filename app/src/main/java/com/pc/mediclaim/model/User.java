package com.pc.mediclaim.model;

public class User {
    private int id;
    private String name;
    private String email;
    private String mobile;
    private String role;
    private String memberId;

    public User(int id, String name, String email, String mobile, String role, String memberId) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.mobile = mobile;
        this.role = role;
        this.memberId = memberId;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getMobile() { return mobile; }
    public String getRole() { return role; }
    public String getMemberId() { return memberId; }
}

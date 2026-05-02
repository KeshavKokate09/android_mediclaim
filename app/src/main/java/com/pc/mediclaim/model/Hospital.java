package com.pc.mediclaim.model;

public class Hospital {
    private int id;
    private String name;
    private String address;
    private String contact;
    private String city;
    private String state;

    public Hospital(int id, String name, String address, String contact, String city, String state) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.contact = contact;
        this.city = city;
        this.state = state;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getContact() { return contact; }
    public String getCity() { return city; }
    public String getState() { return state; }
}

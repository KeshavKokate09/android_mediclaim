package com.pc.mediclaim.model;

public class Dependent {
    private int id;
    private int userId;
    private String name;
    private String relation;

    public Dependent(int id, int userId, String name, String relation) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.relation = relation;
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getName() { return name; }
    public String getRelation() { return relation; }
}

package com.clover.applearnjava;

public class Contact {
    private String name;
    private String description;

    public Contact(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
package com.example.demoapp.entry_db;

public class CategoryName {
    public String category;
    public String name;

    public CategoryName() {} // required

    public CategoryName(String category, String name) {
        this.category = category;
        this.name = name;
    }

    public String getCategory(){
        return category;
    }

    public String getName(){
        return name;
    }
}

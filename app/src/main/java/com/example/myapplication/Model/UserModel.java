package com.example.myapplication.Model;

public class UserModel {
    private int id;
    private String name;
    private String email;
    private String password;
    private String img;

    public UserModel() {} // Firestore cần constructor rỗng

    public UserModel(int id, String name, String email, String password, String img) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.img = img;
    }

    // Getters và Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getImg() { return img; }
    public void setImg(String img) { this.img = img; }
}

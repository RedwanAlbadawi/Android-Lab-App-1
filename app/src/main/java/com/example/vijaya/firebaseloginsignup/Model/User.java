package com.example.vijaya.firebaseloginsignup.Model;

import java.io.Serializable;


// user pojo to store user information.,
// implements serializable interface so that we can pass it in different activities using intent
public class User implements Serializable {

    private String username;
    private String univeristy;
    private String branch;
    private String gender;
    private String imageUrl;

    public User(){}

    public User(String username, String univeristy, String branch, String gender, String imageUrl) {
        this.username = username;
        this.univeristy = univeristy;
        this.branch = branch;
        this.gender = gender;
        this.imageUrl = imageUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUniveristy() {
        return univeristy;
    }

    public void setUniveristy(String univeristy) {
        this.univeristy = univeristy;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}

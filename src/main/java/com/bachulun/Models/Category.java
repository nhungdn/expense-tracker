package com.bachulun.Models;

import java.time.LocalDateTime;

public class Category {
    private int id;
    private int userId;
    private String name;
    private LocalDateTime createdAt;
    private boolean deleteBan;

    public Category(int userId, String name, LocalDateTime createdAt, boolean deleteBan) {
        this.userId = userId;
        this.name = name;
        this.createdAt = createdAt;
        this.deleteBan = deleteBan;
    }

    public Category(int id, int userId, String name, LocalDateTime createdAt, boolean deleteBan) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.createdAt = createdAt;
        this.deleteBan = deleteBan;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean getDeleteBan() {
        return deleteBan;
    }

    public void setDeleteBan(boolean deleteBan) {
        this.deleteBan = deleteBan;
    }
}
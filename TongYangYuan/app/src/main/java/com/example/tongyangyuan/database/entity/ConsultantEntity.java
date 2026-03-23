package com.example.tongyangyuan.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.tongyangyuan.database.converter.StringListConverter;

import java.util.List;

@Entity(tableName = "consultants")
@TypeConverters(StringListConverter.class)
public class ConsultantEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private long userId;
    private long serverId;

    private String name;
    private String title;
    private String specialty;
    private double rating;
    private String servedCount;
    private String avatarColor;
    private String avatarUrl;
    private String intro;
    private List<String> reviews;
    private List<String> identityTags;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getServedCount() {
        return servedCount;
    }

    public void setServedCount(String servedCount) {
        this.servedCount = servedCount;
    }

    public String getAvatarColor() {
        return avatarColor;
    }

    public void setAvatarColor(String avatarColor) {
        this.avatarColor = avatarColor;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public List<String> getReviews() {
        return reviews;
    }

    public void setReviews(List<String> reviews) {
        this.reviews = reviews;
    }

    public List<String> getIdentityTags() {
        return identityTags;
    }

    public void setIdentityTags(List<String> identityTags) {
        this.identityTags = identityTags;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getServerId() {
        return serverId;
    }

    public void setServerId(long serverId) {
        this.serverId = serverId;
    }
}

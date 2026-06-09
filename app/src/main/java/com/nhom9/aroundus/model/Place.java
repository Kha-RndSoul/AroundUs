package com.nhom9.aroundus.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Place implements Serializable {
    private static final long serialVersionUID = 1L;

    private String placeId;
    private String name;
    private String address;
    private String category;
    private String description;
    private String openingHour;
    private String closingHour;
    private double lat; // Vĩ độ
    private double lng; // Kinh độ
    private List<String> imageUrls = new ArrayList<>();
    private String createdBy;
    private double avgRating;

    public Place() {}

    // Getters & Setters
    public String getPlaceId() { return placeId; }
    public void setPlaceId(String placeId) { this.placeId = placeId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getOpeningHour() { return openingHour; }
    public void setOpeningHour(String openingHour) { this.openingHour = openingHour; }

    public String getClosingHour() { return closingHour; }
    public void setClosingHour(String closingHour) { this.closingHour = closingHour; }

    public double getLat() { return lat; }
    public void setLat(double lat) { this.lat = lat; }

    public double getLng() { return lng; }
    public void setLng(double lng) { this.lng = lng; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public double getAvgRating() { return avgRating; }
    public void setAvgRating(double avgRating) { this.avgRating = avgRating; }
}
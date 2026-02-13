package com.example.cors.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class CourseDto {
    
    /**
     * Уникальный идентификатор курса с сервера
     */
    @SerializedName("id")
    private int id;
    
    /**
     * Название курса
     */
    @SerializedName("title")
    private String title;
    
    /**
     * Провайдер курса (платформа)
     */
    @SerializedName("provider")
    private String provider;
    
    /**
     * Длительность курса в часах
     */
    @SerializedName("duration")
    private int duration;
    
    /**
     * Уровень сложности
     */
    @SerializedName("level")
    private String level;
    
    /**
     * URL изображения (snake_case в JSON -> camelCase в Java)
     */
    @SerializedName("image_url")
    private String imageUrl;
    
    /**
     * Описание курса
     */
    @SerializedName("description")
    private String description;

    /**
     * Конструктор по умолчанию - требуется Gson для десериализации JSON
     */
    public CourseDto() {
    }

    /**
     * Полный конструктор для создания DTO вручную (например, в тестах)
     */
    public CourseDto(int id, String title, String provider, int duration, 
                    String level, String imageUrl, String description) {
        this.id = id;
        this.title = title;
        this.provider = provider;
        this.duration = duration;
        this.level = level;
        this.imageUrl = imageUrl;
        this.description = description;
    }

    // Геттеры и сеттеры для всех полей
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

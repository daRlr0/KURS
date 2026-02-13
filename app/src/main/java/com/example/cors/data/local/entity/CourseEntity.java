package com.example.cors.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Room Entity - представляет таблицу курсов в локальной базе данных.
 * Используется для хранения информации о курсах в offline режиме.
 * Каждое поле соответствует колонке в таблице "courses".
 */
@Entity(tableName = "courses")
public class CourseEntity {
    
    /**
     * идентификатор курсаа
     */
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    /**
     * Название курса
     */
    private String title;
    
    /**
     * Провайдер/платформа курса
     */
    private String provider;
    
    /**
     * Длительность курса в часах
     */
    private int duration;
    
    /**
     * Уровень сложности курса
     */
    private String level;
    
    /**
     * URL изображения
     */
    private String imageUrl;
    
    /**
     * Полное описание курса
     */
    private String description;
    
    /**
     * Комментарий пользователя к курсу
     */
    private String comment;
    
    /**
     * Оценка пользователя (от 0 до 5)
     */
    private float userRating;
    
    /**
     * Флаг добавлен ли курс в избранное
     */
    private boolean isFavorite;

    /**
     * Конструктор по умолчанию - требуется Room для создания объектов при чтении из БД
     */
    public CourseEntity() {
    }

    /**
     * Полный конструктор для создания нового объекта курса
     */
    public CourseEntity(int id, String title, String provider, int duration, 
                       String level, String imageUrl, String description, 
                       String comment, float userRating, boolean isFavorite) {
        this.id = id;
        this.title = title;
        this.provider = provider;
        this.duration = duration;
        this.level = level;
        this.imageUrl = imageUrl;
        this.description = description;
        this.comment = comment;
        this.userRating = userRating;
        this.isFavorite = isFavorite;
    }

    // Геттеры и сеттеры - необходимы Room для маппинга данных между объектами и БД
    
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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public float getUserRating() {
        return userRating;
    }

    public void setUserRating(float userRating) {
        this.userRating = userRating;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }
}

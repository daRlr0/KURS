package com.example.cors.domain.model;

/**
 * Domain/UI модель курса - используется в слое представления (UI).
 * Эта модель изолирует UI от изменений в структуре БД или API.
 * 
 * Преимущества отдельной UI модели:
 * - UI не зависит от Room и Retrofit аннотаций
 * - Можно менять структуру БД без изменения UI
 * - Возможность добавить вычисляемые поля (например, форматированная длительность)
 */
public class Course {
    
    private int id;
    private String title;
    private String provider;
    private int duration;
    private String level;
    private String imageUrl;
    private String description;
    private String comment;
    private float userRating;
    private boolean isFavorite;

    /**
     * Конструктор по умолчанию
     */
    public Course() {
    }

    /**
     * Полный конструктор
     */
    public Course(int id, String title, String provider, int duration, 
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

    /**
     * Вспомогательный метод для получения форматированной строки длительности.
     * Используется в UI для отображения (например, "40 часов").
     * 
     * @return Форматированная строка с длительностью
     */
    public String getFormattedDuration() {
        if (duration == 1) {
            return duration + " час";
        } else if (duration < 5) {
            return duration + " часа";
        } else {
            return duration + " часов";
        }
    }

    /**
     * Вспомогательный метод для получения локализованного названия уровня.
     * 
     * @return Уровень на русском языке
     */
    public String getLocalizedLevel() {
        switch (level) {
            case "Beginner":
                return "Начальный";
            case "Intermediate":
                return "Средний";
            case "Advanced":
                return "Продвинутый";
            default:
                return level;
        }
    }

    // Геттеры и сеттеры
    
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
